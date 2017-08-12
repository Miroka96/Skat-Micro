import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import model.RequestObject
import model.User


class GameService : AbstractVerticle() {
    val conf: JsonObject by lazy { config() }

    val jdbc: JDBCClient by lazy {
        JDBCClient.createShared(vertx, conf, "User")
    }

    val router: Router by lazy { Router.router(vertx) }


    override fun start(fut: Future<Void>) {
        router.route("/").handler(BodyHandler.create()) //This is really important if you use routing
        // -> read documentation. If not used the body wont be passed

        addRouting(router)

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

    fun addRouting(router: Router) {
        router.get("/").handler(wrapHandler(this::getOne))
    }


    override fun stop() {
        // Close the JDBC client.
        if (jdbc != null) jdbc.close()
    }

    private fun wrapHandler(handler: (requestObject: RequestObject) -> Any): Handler<RoutingContext> {
        return Handler<RoutingContext> { routingContext: RoutingContext -> handler(RequestObject(routingContext)) }
    }

    private fun getOne(request: RequestObject) {
        jdbc.getConnection({ connectionFuture: AsyncResult<SQLConnection> ->
            if (connectionFuture.failed())
                NullPointerException("Could not get Database Connection").printStackTrace()
            request.databaseConnection = connectionFuture.result()
            getOneWithDb()
        })
    }

    private fun getOne(request: RequestObject) {
        jdbc.getConnection({ connectionFuture: AsyncResult<SQLConnection> ->
            if (connectionFuture.failed())
                getOneWithDb()
        })
    }

    private fun getOneWithDb() {
        // Read the request's content and create an instance of Whisky.
        val connection = ar.result()
        select(loginUser, connection, result {
            if (result.succeeded()) {
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(result.result()))
            } else {
                routingContext.response()
                        .setStatusCode(404).end()
            }
            connection.close()
        })
    }


    private fun select(loginUser: User, connection: SQLConnection, resultHandler: Handler<AsyncResult<User>>) {
        //Querry stolen from login() in main and adapted to new style?
        System.out.println(loginUser)
        connection.queryWithParams("SELECT * FROM USER WHERE username = ? AND hashPassword = ? AND revoked = false",
                JsonArray().add(loginUser.getUsername()).add(loginUser.getHashPassword()), { ar ->
            if (ar.failed()) {
                System.out.println(ar.cause())
                resultHandler.handle(Future.failedFuture<T>("User not found"))
            } else {
                if (ar.result().numRows >= 1) {
                    println("success logging in")
                    System.out.println(ar.result())
                    val userValues = ar.result().rows.get(0)
                    println(userValues)
                    resultHandler.handle(Future.succeededFuture<T>(User(userValues)))
                } else {
                    resultHandler.handle(Future.failedFuture<T>("User not found"))
                }
            }
        })
    }
}
}