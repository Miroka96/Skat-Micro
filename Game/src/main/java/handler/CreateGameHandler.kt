package handler

import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.document.JsonDocument
import game.Game
import io.vertx.core.AsyncResult
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import service.AbstractRequestHandler
import service.RequestObject
import service.WebContentType.JSON
import service.WebStatusCode
import service.WebStatusCode.CREATED
import service.WebStatusCode.INTERNAL_ERROR


class CreateGameHandler : AbstractRequestHandler() {
    override var needsDatabaseConnection = true


    override fun handleRequest(requestObject: RequestObject) {
        val bucket = requestObject.bucket!!
        prepareFutures(requestObject, bucket)
    }

    fun prepareFutures(requestObject: RequestObject, bucket: AsyncBucket) {
        val routingContext = requestObject.routingContext
        val successfulReply = Future.future<String>()
        val database = Future.future<Void>()
        val reply = CompositeFuture.all(successfulReply, database)
                .setHandler { operation: AsyncResult<CompositeFuture> ->
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
        startOperation(requestObject, successfulReply, database)
    }

    fun startOperation(requestObject: RequestObject, successfulReply: Future<String>, database: Future<Void>) {
        val bucket = requestObject.bucket!!

        bucket.counter(Game.getLatestIdKey(), 1, 1)
                .map { doc -> doc.content().toInt() }
                .map { id ->
                    val game = Game()
                    game.id = id
                    return@map game
                }
                .doOnNext { game: Game ->
                    val anonymousData = game.createAnonymousGameDataJson()
                    successfulReply.complete(anonymousData)
                }
                .map { game ->
                    game.dataToJsonDocument()
                }
                .flatMap { gameDoc: JsonDocument ->
                    bucket.upsert(gameDoc)
                }
                .subscribe(
                        { it: JsonDocument -> println(it) },
                        { it: Throwable ->
                            it.printStackTrace()
                            database.fail(it)
                        },
                        {
                            bucket.close()
                            database.complete()
                        }
                )
    }

    fun replySuccessful(routingContext: RoutingContext, data: String) {
        reply(routingContext.response(), CREATED, data)
    }

    fun replyFailed(routingContext: RoutingContext, throwable: Throwable) {
        val error = JsonObject()
        error.put("message", throwable.message)
        throwable.printStackTrace()

        val data = JsonObject()
        data.put("exception", error)

        reply(routingContext.response(), INTERNAL_ERROR, data.encode())
    }

    fun reply(response: HttpServerResponse, statuscode: WebStatusCode, data: String) {
        response.setStatusCode(statuscode.code)
                .putHeader("content-type", JSON.type)
                .end(data)
    }

}