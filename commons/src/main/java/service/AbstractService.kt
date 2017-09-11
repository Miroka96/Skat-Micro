package service

import database.CouchbaseAccess
import io.vertx.core.AbstractVerticle
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

    final override fun start(fut: Future<Void>) {

        db = CouchbaseAccess(config())
        router.route().handler(BodyHandler.create()) //This is really important if you use routing
        // -> read documentation. If not used the body wont be passed

        addRouting(router)

        customStart()

        vertx
                .createHttpServer()
                .requestHandler(router::accept)

                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        config().getInteger("http.port", defaultPort)
                ) { result ->
                    if (result.succeeded()) {
                        fut.complete()
                    } else {
                        fut.fail(result.cause())
                    }
                }
    }

    final override fun stop() {
        db.closeBlocking(true)
        customStop()
    }

    abstract fun addRouting(router: Router)

    open fun customStart() {}
    open fun customStop() {}

    fun wrapHandler(requestHandler: AbstractRequestHandler): Handler<RoutingContext>
            = RequestHandlerWrapper(requestHandler, db).wrapHandler()


}
