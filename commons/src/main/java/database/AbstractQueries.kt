package database

import io.vertx.core.json.JsonObject

abstract class AbstractQueries(bucketname: String) {

    fun getAttributeList(item: Any): Array<String> {
        return JsonObject.mapFrom(
                item
        ).fieldNames().map { keyword ->
            "`$keyword`"
        }.toTypedArray()
    }
}