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
            var cause: Throwable?
            try {
                cause = future.cause()
            } catch (ex: Exception) {
                ex.printStackTrace()
                cause = null
            }
            if (cause != null) {
                causes.add(cause)
                cause.printStackTrace()
            }
        }
        var cause: Throwable?
        try {
            cause = requestObject.finishingFuture.cause()
        } catch (ex: Exception) {
            ex.printStackTrace()
            cause = null
        }
        if (cause != null) {
            causes.add(cause)
            cause.printStackTrace()
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

    private fun getThrowables(future: CompositeFuture): FailingReplyThrowable {
        val causes = MutableList(0, { Throwable() })
        try {
            val cause = future.cause()
            causes.add(cause)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        for (causeIndex in 0..future.size()) {
            try {
                val cause = future.cause(causeIndex)
                if (cause is FailingReplyThrowable) {
                    return cause
                }
                causes.add(cause)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        if (causes.isEmpty()) {
            causes.add(Exception("Unknown Error"))
        }
        return FailingReplyThrowable(causes)
    }

    private fun handleCompositeFuture(routingContext: RoutingContext, operation: AsyncResult<CompositeFuture>) {
        val future = operation.result()
        if (future.failed()) {
            replyFailed(routingContext, getThrowables(future))
        } else {
            val responseData: String = future.resultAt(0)
            replySuccessful(routingContext, responseData)
        }
    }

    abstract fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>)


    open val successfulResponseCode = WebStatusCode.OK

    fun replySuccessful(requestObject: RequestObject, data: String) =
            replySuccessful(requestObject.routingContext, data)

    fun replySuccessful(routingContext: RoutingContext, data: String) {
        reply(routingContext.response(), successfulResponseCode, data)
    }

    fun replyFailed(requestObject: RequestObject, throwable: FailingReplyThrowable) =
            replyFailed(requestObject.routingContext, throwable)

    fun replyFailed(routingContext: RoutingContext, throwable: FailingReplyThrowable) {
        reply(routingContext.response(), throwable.webStatusCode, throwable.getString())
    }

    private fun reply(response: HttpServerResponse, statuscode: WebStatusCode, data: String) {
        response.setStatusCode(statuscode.code)
                .putHeader("content-type", WebContentType.JSON.type)
                .end(data)
    }

}