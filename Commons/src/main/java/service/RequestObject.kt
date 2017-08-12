package service

import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.RoutingContext
import model.User

class RequestObject(
        val jdbc: JDBCClient,
        var abstractRequestHandler: AbstractRequestHandler
) {
    var routingContext: RoutingContext? = null
        set(value) {
            field = value
            if (value != null) decodeRoutingContextBody()
        }
    var user: User? = null
    var databaseConnection: SQLConnection? = null


    constructor(
            jdbc: JDBCClient,
            abstractRequestHandler: AbstractRequestHandler,
            routingContext: RoutingContext
    ) : this(jdbc, abstractRequestHandler) {
        this.routingContext = routingContext
    }

    fun decodeRoutingContextBody(routingContext: RoutingContext) {
        //user = Json.decodeValue(routingContext.bodyAsString, User::class.java)
    }

    private fun decodeRoutingContextBody() {
        if (routingContext == null) {
            NullPointerException("RoutingContext not yet initialized").printStackTrace()
            return
        }
        decodeRoutingContextBody(routingContext!!)
    }

    fun handleRequest() {
        abstractRequestHandler.handleRequest(this)
    }

}