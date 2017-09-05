package handler

import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.error.DocumentDoesNotExistException
import database.CouchbaseAccess
import datamodel.LatestGameId
import game.Game
import rx.Observable
import service.AbstractRequestHandler
import service.RequestObject

class CreateGameHandler : AbstractRequestHandler() {
    override var needsDatabaseConnection = true


    override fun handleRequest(requestObject: RequestObject) {
        var bucket = requestObject.bucket!!
        createNewGame(bucket)
        bucket.close()
    }

    fun createNewGame(bucket: AsyncBucket) {
        val latestIdDoc = getLatestGameId(bucket)
        val latestId = parseLatestGameId(latestIdDoc)
        val nextGame = createNextGame(latestId)

    }

    fun getLatestGameId(bucket: AsyncBucket): Observable<JsonDocument> {
        val latestIdKey = Game.getLatestIdKey()
        return bucket.get(latestIdKey)
                .onErrorReturn {
                    if (it is DocumentDoesNotExistException) {
                        return@onErrorReturn JsonDocument.create(
                                latestIdKey,
                                JsonObject.fromJson(
                                        CouchbaseAccess.jsonmapper.writeValueAsString(
                                                LatestGameId(0)
                                        )
                                ))
                    }
                    throw it
                }
    }

    fun parseLatestGameId(latestId: Observable<JsonDocument>): Observable<LatestGameId> {
        return latestId.map { doc ->
            CouchbaseAccess.jsonmapper.readValue(doc.content().toString(), LatestGameId::class.java)
        }
    }

    fun createNextGame(latestId: Observable<LatestGameId>): Observable<Game> {
        return latestId.map { latestGameId ->
            val game = Game()
            game.id = latestGameId.latestId + 1
            return@map game
        }
    }

    fun createNextGameDoc(nextGame: Observable<Game>): Observable<JsonDocument> {
        return nextGame.map { game ->
            game.dataToJsonDocument()
        }
    }
}