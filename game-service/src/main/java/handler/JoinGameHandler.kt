package handler

import io.vertx.core.Future
import service.request.AbstractRequestHandler
import service.request.RequestObject
import service.response.WebStatusCode

class JoinGameHandler : AbstractRequestHandler() {
    override val successfulResponseCode: WebStatusCode
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val operationFutureCount: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}