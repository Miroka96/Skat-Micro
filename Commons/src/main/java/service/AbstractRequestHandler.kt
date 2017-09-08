package service

import io.vertx.core.AsyncResult
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext


abstract class AbstractRequestHandler {

    open val needsDatabaseConnection = false
    open val needsGame = false

    abstract val operationFutureCount: Int


    open fun handleFailedInitialization(requestObject: RequestObject) {
        for (future in requestObject.futures) {
            if (future.cause() != null) {
                future.cause().printStackTrace()
            }
        }
        if (requestObject.finishingFuture.cause() != null) {
            requestObject.finishingFuture.cause().printStackTrace()
        }
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
        val future = operation.result()
        if (future.failed()) {
            try {
                val cause = future.cause()
                replyFailed(routingContext, cause)
            } catch (ex: Exception) {
                ex.printStackTrace()
                replyFailed(routingContext, Exception("Unknown Error"))
            }
        } else {
            val responseData: String = future.resultAt(0)
            replySuccessful(routingContext, responseData)
        }
    }

    abstract fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>)

    open val successfulResponseCode = WebStatusCode.OK

    open fun replySuccessful(routingContext: RoutingContext, data: String) {
        reply(routingContext.response(), successfulResponseCode, data)
    }

    open fun replyFailed(routingContext: RoutingContext, throwable: Throwable) {
        val error = JsonObject()
        error.put("message", throwable.message)
        throwable.printStackTrace()

        val data = JsonObject()
        data.put("exception", error)

        reply(routingContext.response(), WebStatusCode.INTERNAL_ERROR, data.encode())
    }

    open fun reply(response: HttpServerResponse, statuscode: WebStatusCode, data: String) {
        response.setStatusCode(statuscode.code)
                .putHeader("content-type", WebContentType.JSON.type)
                .end(data)
    }

}