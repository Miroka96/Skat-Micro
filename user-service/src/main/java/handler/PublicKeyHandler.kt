package handler

import UserService
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import service.request.AbstractRequestHandler
import service.request.RequestObject
import service.response.WebStatusCode

class PublicKeyHandler : AbstractRequestHandler() {
    override val operationFutureCount: Int = 0

    override val successfulResponseCode = WebStatusCode.OK

    override fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>) {
        replyJWTPublicKey(replyFuture, (requestObject.service as UserService).publicKey64)
    }

    fun replyJWTPublicKey(replyFuture: Future<String>, publicKey64: String) {
        val response = JsonObject()
                .put("public-key", publicKey64)
        replyFuture.complete(response.encode())
    }
}