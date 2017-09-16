package service

import database.AbstractQueries
import database.CouchbaseAccess
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler


abstract class AbstractService : AbstractVerticle() {
    open val defaultPort = 8080

    val conf: JsonObject by lazy {
        config()
    }

    val router: Router by lazy {
        Router.router(vertx)
    }

    lateinit var db: CouchbaseAccess
    abstract val queries: AbstractQueries

    final override fun start(future: Future<Void>) {
        val initialized = Future.future<Unit>()
        initialized.setHandler { res: AsyncResult<Unit> ->
            if (res.succeeded()) {
                future.complete()
            } else {
                res.cause().printStackTrace()
                println("shutting service down")
                vertx.close()
            }
        }
        initialize(initialized)
    }

    fun initialize(future: Future<Unit>) {
        val databaseFinished = Future.future<Unit>()
        val routingFinished = Future.future<Unit>()
        val customStartFinished = Future.future<Unit>()
        val initializationFinished = Future.future<Unit>()

        databaseFinished.setHandler { res: AsyncResult<Unit> ->
            if (res.succeeded()) {
                initializeRouting(routingFinished)
            } else {
                future.fail(res.cause())
            }
        }
        routingFinished.setHandler { res: AsyncResult<Unit> ->
            if (res.succeeded()) {
                customStart(customStartFinished)
            } else {
                future.fail(res.cause())
            }
        }
        customStartFinished.setHandler { res: AsyncResult<Unit> ->
            if (res.succeeded()) {
                initializeWebServer(initializationFinished)
            } else {
                future.fail(res.cause())
            }
        }
        initializationFinished.setHandler { res: AsyncResult<Unit> ->
            future.handle(res)
        }
        this.
                checkCreateDatabaseIndex(databaseFinished)
    }


    private fun checkCreateDatabaseIndex(continueFuture: Future<Unit>) {
        vertx.executeBlocking<Unit>({ future: Future<Unit> ->
            db = CouchbaseAccess(config())
            db.checkBucket()
                    .flatMap { bucket ->
                        bucket.bucketManager()
                    }
                    .flatMap { mgr ->
                        mgr.createN1qlPrimaryIndex(true, true)
                        mgr.buildN1qlDeferredIndexes()
                    }
                    .toBlocking()
                    .last()
            future.complete()
        }, { res: AsyncResult<Unit> ->
            continueFuture.complete()
        })
    }

    private fun initializeRouting(continueFuture: Future<Unit>) {
        router.route().handler(BodyHandler.create()) //This is really important if you use routing
        // -> read documentation. If not used the body wont be passed

        addRouting(router)

        continueFuture.complete()
    }

    private fun initializeWebServer(continueFuture: Future<Unit>) {
        vertx
                .createHttpServer()
                .requestHandler(router::accept)

                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080
                        config().getInteger("http.port", defaultPort)
                ) { result ->
                    if (result.succeeded()) {
                        continueFuture.complete()
                    } else {
                        continueFuture.fail(result.cause())
                    }
                }
    }

    final override fun stop() {
        db.closeBlocking(true)
        customStop()
    }

    abstract fun addRouting(router: Router)

    open fun customStart(continueFuture: Future<Unit>) {
        continueFuture.complete()
    }
    open fun customStop() {}

    fun wrapHandler(requestHandler: AbstractRequestHandler): Handler<RoutingContext>
            = RequestHandlerWrapper(requestHandler, db).wrapHandler()


}
