package service.database

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import io.vertx.core.json.JsonArray
import rx.Observable

abstract class AbstractDatabaseAccess {

    abstract fun readJson(key: String): Observable<JsonDocument>

    abstract fun writeJson(key: String, json: String): Observable<JsonDocument>
    abstract fun writeJson(key: String, content: JsonObject): Observable<JsonDocument>
    abstract fun writeJson(doc: JsonDocument): Observable<JsonDocument>
    abstract fun deleteKey(key: String): Observable<JsonDocument>

    companion object {
        fun jsonArrayToStringList(array: JsonArray): List<String> {
            return MutableList<String>(array.size()) { i ->
                array.getString(i)
            }
        }

        fun joinToString(strings: List<String>, delimeter: String): String {
            if (strings.size < 1) return ""
            val iter = strings.iterator()
            val builder = StringBuilder(iter.next())
            while (iter.hasNext()) {
                builder.append(delimeter)
                builder.append(iter.next())
            }
            return builder.toString()
        }
    }
}