package database

import com.couchbase.client.core.BackpressureException
import com.couchbase.client.core.time.Delay
import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.CouchbaseAsyncCluster
import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment
import com.couchbase.client.java.error.DocumentDoesNotExistException
import com.couchbase.client.java.util.retry.RetryBuilder
import io.vertx.core.Future
import rx.Observable
import java.util.concurrent.TimeUnit


class CouchbaseAccess(var seedNodes: List<String>, var bucketName: String, var bucketPassword: String) : DatabaseAccess() {

    private var cluster: CouchbaseAsyncCluster
    @Volatile private var bucket: AsyncBucket? = null


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

    fun close(stopFuture: Future<Void>) {
        cluster.disconnect()
                .last()
                .doOnCompleted {
                    environment.shutdownAsync()
                    stopFuture.complete()
                }
                .doOnError {
                    stopFuture.fail(it)
                }
                .onErrorReturn { false }
    }

    fun checkBucket(): Observable<AsyncBucket> {
        if (bucket == null || bucket!!.isClosed) {
            return createBucket()
                    .doOnError { it ->
                        it.printStackTrace()
                        println("Could not open Bucket.")
                    }
        } else {
            return Observable.just(bucket!!)
        }
    }

    override fun readJson(key: String): Observable<JsonDocument> {
        return checkBucket()
                .flatMap { it -> it.get(key) }
                .doOnError { it ->
                    it.printStackTrace()
                    println("Key not found")
                }
                .onErrorReturn { JsonDocument.create("couldNotRead") }
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
                .onErrorReturn { JsonDocument.create("couldNotWrite") }
    }

    override fun deleteKey(key: String): Observable<JsonDocument> {
        return checkBucket()
                .flatMap {
                    it.remove(key).onErrorReturn { ex: Throwable ->
                        var msg = ""
                        if (ex is DocumentDoesNotExistException) {
                            msg = "DocumentDoesNotExistException"
                        } else {
                            ex.printStackTrace()
                            if (ex.message != null)
                                msg = ex.message!!
                            else {
                                msg = "UnknownException"
                            }
                        }
                        return@onErrorReturn JsonDocument.create("null")
                    }

                }

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
