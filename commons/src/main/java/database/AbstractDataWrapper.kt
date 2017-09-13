package database

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject

abstract class AbstractDataWrapper : DatabaseAccess {
    abstract var id: Int

    fun dataToJson(data: Any): String {
        return CouchbaseAccess.jsonmapper.writeValueAsString(data)
    }

    fun dataToJson() = dataToJson(getData())
    fun dataToJsonObject() = JsonObject.fromJson(dataToJson())
    fun dataToJsonDocument() = JsonDocument.create(getDbKeyFromId(), dataToJsonObject())

    abstract fun getData(): Any

    abstract override fun getDbKeyFromId(id: Int): String
    fun getDbKeyFromId() = getDbKeyFromId(id)
    abstract override fun latestIdKey(): String

    companion object : DatabaseAccess {
        override fun getDbKeyFromId(id: Int): String {
            throw NotImplementedError()
        }

        override fun latestIdKey(): String {
            throw NotImplementedError()
        }
    }
}