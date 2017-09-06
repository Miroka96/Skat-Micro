package handler

import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.document.JsonDocument
import game.Game
import io.vertx.ext.web.RoutingContext
import service.AbstractRequestHandler
import service.RequestObject
import service.WebContentType.JSON
import service.WebStatusCode.OK


class CreateGameHandler : AbstractRequestHandler() {
    override var needsDatabaseConnection = true


    override fun handleRequest(requestObject: RequestObject) {
        val bucket = requestObject.bucket!!
        requestObject.routingContext
        createNewGame(bucket, requestObject.routingContext)
    }

    fun createNewGame(bucket: AsyncBucket, routingContext: RoutingContext) {
        bucket.counter(Game.getLatestIdKey(), 1, 1)
                .map { doc -> doc.content().toInt() }
                .map { id ->
                    val game = Game()
                    game.id = id
                    return@map game
                }
                .doOnNext { game: Game ->
                    val anonymousData = game.createAnonymousGameDataJson()
                    routingContext.response()
                            .setStatusCode(OK.code)
                            .putHeader("content-type", JSON.type)
                            .end(anonymousData)
                }
                .map { game ->
                    game.dataToJsonDocument()
                }
                .flatMap { gameDoc: JsonDocument ->
                    bucket.upsert(gameDoc)
                }
                .subscribe(
                        { it: JsonDocument -> println(it) },
                        { it: Throwable -> it.printStackTrace() },
                        { bucket.close() }
                )
    }

}