package service.request

import com.couchbase.client.java.document.JsonDocument
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import service.database.CouchbaseAccess
import skat.Game
import skat.model.GameData

class RequestHandlerWrapper(
        var requestHandler: AbstractRequestHandler,
        var db: CouchbaseAccess
) {

    fun wrapHandler() = Handler<RoutingContext>
    { routingContext ->
        //TODO fill all needed arguments

        val request = RequestObject(routingContext, requestHandler)

        createDatabaseConnection(
                getGameFromDatabase(
                        handleRequestObject()
                )
        ).handle(request)
    }

    private fun createDatabaseConnection(
            next: Handler<RequestObject>
    ): Handler<RequestObject> {
        if (!requestHandler.needsDatabaseConnection) return next

        return Handler<RequestObject> { request: RequestObject ->
            db.checkBucket()
                    .subscribe(
                            { bucket -> request.bucket = bucket },
                            { request.needsDatabaseConnectionFuture.fail(it) }
                    )
            next.handle(request)
        }
    }

    private fun getGameFromDatabase(
            next: Handler<RequestObject>
    ): Handler<RequestObject> {
        if (requestHandler.needsGame == false) return next

        return Handler<RequestObject> { request: RequestObject ->
            db
                    .readJson(Game.getDbKeyFromId(request.getGameId()))
                    .map { doc: JsonDocument -> db.gameDataFrom(doc) }
                    .map { gameData: GameData -> Game(gameData) }
                    .subscribe(
                            { game -> request.game = game },
                            { request.needsGameFuture.fail(it) }
                    )
            next.handle(request)
        }
    }

    private fun handleRequestObject(): Handler<RequestObject> {
        return Handler<RequestObject> { request ->
            request.chainFinishedFuture.complete()
        }
    }
}