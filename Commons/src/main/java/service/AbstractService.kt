package service

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import service.model.RequestObject


abstract class AbstractService : AbstractVerticle() {
    val conf: JsonObject by lazy {
        config()
    }

    val jdbc: JDBCClient by lazy {
        JDBCClient.createShared(vertx, conf, this.javaClass.name)
    }

    val router: Router by lazy {
        Router.router(vertx)
    }

    final override fun start(fut: Future<Void>) {
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
                        config().getInteger("http.port", 8080)!!
                ) { result ->
                    if (result.succeeded()) {
                        fut.complete()
                    } else {
                        fut.fail(result.cause())
                    }
                }
    }

    final override fun stop() {
        jdbc.close()
        customStop()
    }

    abstract fun addRouting(router: Router)
    //router.get("/").handler(wrapHandler(AbstractRequestHandler()))

    open fun customStart() {}
    open fun customStop() {}


    fun wrapHandler(requestHandler: AbstractRequestHandler) = Handler<RoutingContext>
    { routingContext: RoutingContext ->
        //TODO fill all needed arguments
        var request = RequestObject(jdbc, requestHandler)
        request.routingContext = routingContext

        createSQLConnection(requestHandler,
                handleRequestObject()
        ).handle(request)
    }


    private fun createSQLConnection(
            requestHandler: AbstractRequestHandler,
            next: Handler<RequestObject>
    ): Handler<RequestObject> {
        if (!requestHandler.needsDatabaseConnection) return next

        return Handler<RequestObject> { request: RequestObject ->
            request.jdbc.getConnection({ connectionFuture: AsyncResult<SQLConnection> ->
                if (connectionFuture.failed()) {
                    connectionFuture.cause().printStackTrace()
                    return@getConnection
                }
                request.databaseConnection = connectionFuture.result()
                next.handle(request)
            })
        }
    }

    private fun handleRequestObject(): Handler<RequestObject> {
        return Handler<RequestObject> { requestObject ->
            requestObject.handleRequest()
        }
    }

}
