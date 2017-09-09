package handler

import io.vertx.core.Future
import service.AbstractRequestHandler
import service.RequestObject

class LoginUserHandler : AbstractRequestHandler() {
    override val operationFutureCount: Int = 0

    override val needsDatabaseConnection = true

    override fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>) {

    }

}