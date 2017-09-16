package handler

import io.vertx.core.Future
import service.request.AbstractRequestHandler
import service.request.RequestObject
import service.response.WebStatusCode

class VerifyUserHandler : AbstractRequestHandler() {
    override val successfulResponseCode = WebStatusCode.OK

    override val operationFutureCount: Int = 0

    override val needsDatabaseConnection = true

    override fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>) {

    }

}