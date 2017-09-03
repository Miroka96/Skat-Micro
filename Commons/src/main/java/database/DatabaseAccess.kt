package database

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import rx.Observable

abstract class DatabaseAccess {

    abstract fun readJson(key: String): Observable<JsonDocument>

    abstract fun writeJson(key: String, json: String): Observable<JsonDocument>
    abstract fun writeJson(key: String, content: JsonObject): Observable<JsonDocument>
    abstract fun writeJson(doc: JsonDocument): Observable<JsonDocument>
    abstract fun deleteKey(key: String): Observable<JsonDocument>?
}