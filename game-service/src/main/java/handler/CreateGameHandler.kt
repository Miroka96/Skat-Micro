package handler

import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.document.JsonDocument
import game.Game
import io.vertx.core.Future
import service.AbstractRequestHandler
import service.RequestObject
import service.WebStatusCode


class CreateGameHandler : AbstractRequestHandler() {
    override var needsDatabaseConnection = true

    override val successfulResponseCode = WebStatusCode.CREATED

    override val operationFutureCount = 1

    override fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>) {
        startOperation(replyFuture, operationFutures[0], requestObject.bucket!!)
    }

    fun startOperation(replyFuture: Future<String>, database: Future<out Any>, bucket: AsyncBucket) {
        bucket.counter(Game.latestIdKey(), 1, 1)
                .map { doc -> doc.content().toInt() }
                .map { id ->
                    val game = Game()
                    game.id = id
                    return@map game
                }
                .doOnNext { game: Game ->
                    val anonymousData = game.createAnonymousGameDataJson()
                    replyFuture.complete(anonymousData)
                }
                .map { game ->
                    game.dataToJsonDocument()
                }
                .flatMap { gameDoc: JsonDocument ->
                    bucket.upsert(gameDoc)
                }
                .subscribe(
                        { it: JsonDocument ->
                            print("new Game created: ")
                            println(it)
                        },
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
}