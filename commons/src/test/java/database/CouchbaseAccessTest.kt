package database

import com.couchbase.client.java.CouchbaseCluster
import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import game.model.GameData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test


class CouchbaseAccessTest {
    var host = "172.17.0.2"
    var bucketname = "default"
    var bucketpassword = ""

    var db = CouchbaseAccess(host, bucketname, bucketpassword)
    var testKey = "CouchbaseAccessTest"
    var testJson = "{\"name\":\"Mirko\",\"age\":20}"

    @Test
    fun accessCouchbaseServer() {
        val cluster = CouchbaseCluster.create(host)
        val bucket = cluster.openBucket()
        bucket.upsert(JsonDocument.create(testKey, JsonObject.fromJson(testJson)))
        bucket.close()
        cluster.disconnect()
    }

    @Test
    fun accessCouchbaseServerAsync() {
        val content = JsonObject.fromJson(testJson)
        var doc = JsonDocument.create(testKey, content)

        for (item in db.writeJson(testKey, testJson)
                .doOnCompleted { assertTrue(true) }
                .toBlocking()
                .iterator) {
            println(item)
        }
    }

    @Test
    fun storeJson() {
        db.writeJson(testKey, testJson)
                .doOnCompleted {
                    println("Stored JSON")
                    readJson()
                }
                .toBlocking()
                .last()
    }

    fun readJson() {
        db.readJson(testKey)
                .doOnNext { result: JsonDocument -> assertEquals(testJson, result.content().toString()) }
                .doOnCompleted {
                    println("Read JSON")
                    removeKey(testKey)

                }
                .toBlocking()
                .last()
    }

    @Test
    fun removeInvalidKey() {
        removeKey("notExisting")
    }

    fun removeKey(key: String) {
        db.deleteKey(key)
                .doOnCompleted {
                    println("Removed Entry")
                    assertTrue(true)
                }
                .toBlocking().last()
    }

    @Test
    fun storeGameData() {
        val game = GameData()
        val jsonMapper = jacksonObjectMapper()

        val gameJson = jsonMapper.writeValueAsString(game)
        db.writeJson("testGame", gameJson)
                .doOnCompleted {
                    println("Wrote Game")
                    db.readJson("testGame")
                            .doOnNext { it ->
                                println("Got Game")
                                val dbJson = it.content().toString()
                                val dbGame = jsonMapper.readValue(dbJson, GameData::class.java)
                                val dbGameJson = jsonMapper.writeValueAsString(dbGame)
                                assertEquals(gameJson, dbGameJson)
                            }
                            .toBlocking().last()
                }
                .toBlocking().last()
    }
}