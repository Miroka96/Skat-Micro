package model

import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext

class RequestObject {
    var routingContext: RoutingContext? = null
        set(value) {
            field = value
            if (value != null) decodeRoutingContextBody()
        }
    var user: User? = null
    var databaseConnection: Connection = null

    constructor()

    constructor(routingContext: RoutingContext) {
        this.routingContext = routingContext
    }

    fun decodeRoutingContextBody(routingContext: RoutingContext) {
        user = Json.decodeValue(routingContext.bodyAsString, User::class.java)
    }

    private fun decodeRoutingContextBody() {
        if (routingContext == null) {
            NullPointerException("RoutingContext not yet initialized").printStackTrace()
            return
        }
        decodeRoutingContextBody(routingContext!!)
    }

}