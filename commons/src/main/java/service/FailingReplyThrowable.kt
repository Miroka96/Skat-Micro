package service

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject


class FailingReplyThrowable(
        val causes: MutableList<Throwable>,
        var webStatusCode: WebStatusCode = WebStatusCode.INTERNAL_ERROR
) : Throwable() {
    constructor(
            cause: Throwable,
            webStatusCode: WebStatusCode = WebStatusCode.INTERNAL_ERROR
    ) : this(MutableList(1, { cause }), webStatusCode)


    var reply: String? = null
    var replyJson: JsonObject? = null

    fun addCause(cause: Throwable): FailingReplyThrowable {
        causes.add(cause)
        return this
    }

    fun getString(): String {
        if (reply != null) {
            return reply!!
        } else {
            return getAsJson().encode()
        }
    }

    fun getAsJson(): JsonObject {
        if (replyJson != null) {
            return replyJson!!
        } else {
            return getCausesAsJson()
        }
    }

    fun getCausesAsJson(): JsonObject {
        val exceptions = JsonArray()
        for (cause in causes) {
            val exception = JsonObject()
            exception.put("message", cause.message)
            exception.put("stacktrace", cause.toString())
            exceptions.add(exception)
        }
        val result = JsonObject()
        result.put("exceptions", exceptions)
        return result
    }
}