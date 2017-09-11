package service

import io.vertx.core.json.JsonObject

abstract class CorrectDataTemplate {

    abstract val correctData: Any

    val correctDataJsonObject: JsonObject by lazy {
        JsonObject.mapFrom(correctData)
    }

    val correctDataJson: String by lazy {
        correctDataJsonObject.toString()
    }
}