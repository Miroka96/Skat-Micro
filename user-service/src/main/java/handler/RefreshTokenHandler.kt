package handler

import io.vertx.core.Future
import service.request.AbstractRequestHandler
import service.request.RequestObject
import service.response.WebStatusCode

class RefreshTokenHandler : AbstractRequestHandler() {
    override val operationFutureCount: Int = 0

    override val successfulResponseCode = WebStatusCode.CREATED

    override fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}