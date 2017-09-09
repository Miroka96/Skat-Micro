package service

import database.CouchbaseAccess
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler


abstract class AbstractService : AbstractVerticle() {
    val conf: JsonObject by lazy {
        config()
    }

    val router: Router by lazy {
        Router.router(vertx)
    }

    final override fun start(fut: Future<Void>) {

        db = CouchbaseAccess(config())
        router.route("/").handler(BodyHandler.create()) //This is really important if you use routing
        // -> read documentation. If not used the body wont be passed

        addRouting(router)

        customStart()

        vertx
                .createHttpServer()
                .requestHandler(router::accept)

                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        config().getInteger("http.port", 8080)
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
    //router.get("/").handler(wrapHandler(AbstractRequestHandler()))

    open fun customStart() {}
    open fun customStop() {}

    lateinit var db: CouchbaseAccess

    fun wrapHandler(requestHandler: AbstractRequestHandler)
            = RequestHandlerWrapper(requestHandler, db).wrapHandler()

}
