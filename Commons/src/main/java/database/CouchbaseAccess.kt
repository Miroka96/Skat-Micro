package database

import com.couchbase.client.core.BackpressureException
import com.couchbase.client.core.time.Delay
import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.CouchbaseAsyncCluster
import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment
import com.couchbase.client.java.util.retry.RetryBuilder
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import rx.Observable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class CouchbaseAccess(
        var seedNodes: List<String>,
        var bucketName: String,
        var bucketPassword: String
) : DatabaseAccess() {

    private var cluster: CouchbaseAsyncCluster
    @Volatile private var bucket: AsyncBucket? = null
    @Volatile private var creationObservable: Observable<AsyncBucket>? = null


    init {
        cluster = CouchbaseAsyncCluster.create(environment, seedNodes)
    }

    constructor(seedNode: String) : this(
            MutableList(1) { i -> seedNode },
            "default",
            ""
    )

    constructor(seedNode: String, bucketname: String, bucketpassword: String) : this(
            MutableList(1) { i -> seedNode },
            bucketname,
            bucketpassword
    )

    constructor() : this("localhost")

    constructor(config: io.vertx.core.json.JsonObject) : this(
            jsonArrayToStringList(config.getJsonArray("couchbase.seeds", JsonArray().add("localhost"))),
            config.getString("couchbase.bucket", "default"),
            config.getString("couchbase.password", "")
    )



    private fun createBucket(): Observable<AsyncBucket> {
        return cluster.openBucket()
                .last()
                .doOnNext {
                    bucket = it
                }
                .retryWhen(
                        RetryBuilder
                                .anyOf(BackpressureException::class.java)
                                .delay(Delay.exponential(TimeUnit.MILLISECONDS, 100))
                                .max(10)
                                .build())
    }

    fun close(stopFuture: Future<Void>, shutdownAll: Boolean = false) {
        cluster.disconnect()
                .last()
                .doOnCompleted {
                    if (shutdownAll) environment.shutdownAsync()
                    stopFuture.complete()
                }
                .doOnError {
                    stopFuture.fail(it)
                }
                .onErrorReturn { false }
    }

    fun closeBlocking(shutdownAll: Boolean = false) {
        var latch = CountDownLatch(1)
        var future = Future.future<Void> { it: Future<Void> ->
            latch.countDown()
        }
        close(future, shutdownAll)
        latch.await()
    }

    fun checkBucket(): Observable<AsyncBucket> {
        if (bucket == null || bucket!!.isClosed) {
            // catch further calls during creation
            if (creationObservable != null) return creationObservable!!
            var creation = createBucket()
                    .doOnError { it ->
                        println("Could not open Bucket ${joinToString(seedNodes, ",")}:$bucketName")
                    }
                    .doOnCompleted {
                        creationObservable = null
                    }
            creationObservable = creation
            return creation
        } else {
            return Observable.just(bucket!!)
        }
    }

    override fun readJson(key: String): Observable<JsonDocument> {
        return checkBucket()
                .flatMap { it -> it.get(key) }
                .doOnError { println("Key '$key' not found") }
    }


    override fun writeJson(key: String, json: String): Observable<JsonDocument> {
        val content = JsonObject.fromJson(json)
        return writeJson(key, content)
    }

    override fun writeJson(key: String, content: JsonObject): Observable<JsonDocument> {
        val doc = JsonDocument.create(key, content)
        return writeJson(doc)
    }

    override fun writeJson(doc: JsonDocument): Observable<JsonDocument> {
        return checkBucket()
                .flatMap { it.upsert(doc) }
                .doOnError { it ->
                    it.printStackTrace()
                    println("Could not write Content")
                }
    }

    override fun deleteKey(key: String): Observable<JsonDocument> {
        return checkBucket()
                .flatMap { it.remove(key) }
                .doOnError { println("Could not delete $key") }
    }

    companion object {
        var environment = DefaultCouchbaseEnvironment
                .builder()
                //.mutationTokensEnabled(true)
                //.computationPoolSize(5)
                .build()
            private set
    }
}
