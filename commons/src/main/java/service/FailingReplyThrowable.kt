package service

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject


class FailingReplyThrowable(
        override val cause: Throwable,
        var webStatusCode: WebStatusCode =
        if (cause is FailingReplyThrowable) {
            cause.webStatusCode
        } else {
            WebStatusCode.INTERNAL_ERROR
        }

) : Throwable() {
    private val causes = HashSet<Throwable>(1)

    init {
        addCause(cause)
    }

    constructor(causes: List<Throwable>, webStatusCode: WebStatusCode) : this(causes.first(), webStatusCode) {
        for (cause in causes) addCause(cause)
    }

    fun addCause(cause: Throwable) = causes.add(cause)

    override var message: String? = null
    var replyJson: JsonObject? = null

    fun getReply(): String = getRootOfCauses().getReplyAsRootCause()

    fun getReplyAsRootCause(): String {
        if (message != null) {
            return message!!
        } else {
            return getAsJson().encode()
        }
    }

    private fun getAsJson(): JsonObject {
        if (replyJson != null) {
            return replyJson!!
        } else {
            return getCausesAsJson()
        }
    }

    private fun getCausesAsJson(): JsonObject {
        val exceptions = JsonArray()
        for (cause in causes) {
            val exception = JsonObject()
            if (cause.message != null) {
                exception.put("message", cause.message)
            }
            exception.put("stacktrace", cause.toString())
            exceptions.add(exception)
        }

        val result = JsonObject()
        result.put("exceptions", exceptions)
        return result
    }

    fun getRootOfCauses(): FailingReplyThrowable {
        if (causes.size == 1) {
            causes.forEach {
                if (it is FailingReplyThrowable) {
                    return it.getRootOfCauses()
                }
            }
        }
        return this
    }

    companion object {
        fun databaseError(throwable: Throwable) = FailingReplyThrowable(throwable, WebStatusCode.SERVICE_UNAVAILABLE)

        fun malformedRequest(throwable: Throwable, correctDataJson: String): FailingReplyThrowable {
            val failingReply = FailingReplyThrowable(throwable, WebStatusCode.BAD_REQUEST)
            failingReply.message = correctDataJson
            return failingReply
        }

        fun unknownError(throwable: Throwable) = FailingReplyThrowable(throwable, WebStatusCode.INTERNAL_ERROR)
    }
}