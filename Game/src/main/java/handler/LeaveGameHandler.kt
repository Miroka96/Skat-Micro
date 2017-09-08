package handler

import io.vertx.core.Future
import service.AbstractRequestHandler
import service.RequestObject

class LeaveGameHandler : AbstractRequestHandler() {
    override val operationFutureCount: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}