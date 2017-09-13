package database

import io.vertx.core.json.JsonObject

abstract class AbstractQueries(bucketname: String) {

    protected fun getAttributeList(item: Any): Array<String> {
        return JsonObject.mapFrom(
                item
        ).fieldNames().toTypedArray()
    }
}