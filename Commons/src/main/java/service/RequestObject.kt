package service

import com.couchbase.client.java.AsyncBucket
import game.Game
import io.vertx.ext.web.RoutingContext
import io.vertx.rxjava.core.CompositeFuture
import io.vertx.rxjava.core.Future


data class RequestObject(
        var routingContext: RoutingContext,
        private var requestHandler: AbstractRequestHandler
) {

    var needsDatabaseConnectionFuture = Future.future<Unit>()
    var needsGameFuture = Future.future<Unit>()
    var chainFinishedFuture = Future.future<Unit>()

    var futures = mutableListOf(
            chainFinishedFuture
    )

    var finishingFuture: CompositeFuture

    init {
        if (requestHandler.needsGame) {
            futures.add(needsGameFuture)
        }
        if (requestHandler.needsDatabaseConnection) {
            futures.add(needsDatabaseConnectionFuture)
        }

        finishingFuture = CompositeFuture.all(futures.toList()).setHandler { future ->
            if (future.succeeded()) {
                requestHandler.handleRequest(this)
            } else {
                requestHandler.handleFailedInitialization(this)
            }
        }
    }


    var bucket: AsyncBucket? = null
        set(value) {
            field = value
            needsDatabaseConnectionFuture.complete()
        }

    var game: Game? = null
        set(value) {
            field = value
            needsGameFuture.complete()
        }

    fun getGameId(): Int {
        throw NotImplementedError()
    }
}