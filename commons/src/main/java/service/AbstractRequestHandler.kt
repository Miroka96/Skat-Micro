package service

import io.vertx.core.AsyncResult
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext


abstract class AbstractRequestHandler {

    open val needsDatabaseConnection = false
    open val needsGame = false

    abstract val operationFutureCount: Int


    fun handleFailedInitialization(requestObject: RequestObject) {
        val causes = MutableList<Throwable>(0, { Throwable() })
        for (future in requestObject.futures) {
            try {
                val cause: Throwable
                cause = future.cause()
                causes.add(cause)
            } catch (ex: NullPointerException) {
            }
        }

        try {
            val cause: Throwable
            cause = requestObject.finishingFuture.cause()
            causes.add(cause)
        } catch (ex: NullPointerException) {
        }

        val throwable = FailingReplyThrowable(causes, WebStatusCode.INTERNAL_ERROR)
        replyFailed(requestObject, throwable)
    }

    fun handleRequest(requestObject: RequestObject) {
        val successfulReply = Future.future<String>()
        val successfulOperations = Array<Future<out Any>>(operationFutureCount) { Future.future<Unit>() }

        val allFutures = mergeFutures(successfulOperations, successfulReply)
        CompositeFuture.all(allFutures)
                .setHandler { operation: AsyncResult<CompositeFuture> ->
                    handleCompositeFuture(requestObject.routingContext, operation)
                }
        startOperation(requestObject, successfulReply, successfulOperations)
    }

    private fun mergeFutures(list: Array<Future<out Any>>, new: Future<out Any>): MutableList<Future<out Any>> {
        val allFutures: MutableList<Future<out Any>> = MutableList(1) { new }
        allFutures.addAll(list)
        return allFutures
    }

    private fun handleCompositeFuture(routingContext: RoutingContext, operation: AsyncResult<CompositeFuture>) {
        if (operation.failed()) {
            // operation has no CompositeFuture as result
            // instead the single futures as results
            replyFailed(routingContext, FailingReplyThrowable(operation.cause()))
        } else {
            val compositeFuture = operation.result()
            val responseData: String = compositeFuture.resultAt(0)
            replySuccessful(routingContext, responseData)
        }
    }

    abstract fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>)


    abstract val successfulResponseCode: WebStatusCode

    fun replySuccessful(requestObject: RequestObject, data: String) =
            replySuccessful(requestObject.routingContext, data)

    fun replySuccessful(routingContext: RoutingContext, data: String) =
            reply(routingContext.response(), successfulResponseCode, data)


    fun replyFailed(requestObject: RequestObject, throwable: FailingReplyThrowable) =
            replyFailed(requestObject.routingContext, throwable)

    fun replyFailed(routingContext: RoutingContext, throwable: FailingReplyThrowable) =
            replyFailedRootCause(routingContext, throwable.getRootOfCauses())

    fun replyFailedRootCause(routingContext: RoutingContext, throwable: FailingReplyThrowable) =
            reply(routingContext.response(), throwable.webStatusCode, throwable.getReply())


    private fun reply(response: HttpServerResponse, statuscode: WebStatusCode, data: String) {
        response.setStatusCode(statuscode.code)
                .putHeader("content-type", WebContentType.JSON.type)
                .end(data)
    }

}