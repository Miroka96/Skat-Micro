package service.model

import game.Game
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.RoutingContext
import service.AbstractRequestHandler
import user.User

data class RequestObject(
        val jdbc: JDBCClient,
        var abstractRequestHandler: AbstractRequestHandler
) {

    var databaseConnection: SQLConnection? = null

    var routingContext: RoutingContext? = null

    var user: User? = null

    var game: Game? = null


    fun handleRequest() {
        abstractRequestHandler.handleRequest(this)
    }

}