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
        causes.forEach { cause ->
            exceptions.add(getSingleExceptionJsonObject(cause))
        }
        return getExceptionsJsonObject(exceptions)
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
        private fun getSingleExceptionJsonObject(cause: Throwable) = getSingleExceptionJsonObject(cause.message, cause.toString())
        private fun getSingleExceptionJsonObject(message: String? = null, stacktrace: String? = null): JsonObject {
            val exception = JsonObject()
            if (message != null) {
                exception.put("message", message)
            }
            if (stacktrace != null) {
                exception.put("stacktrace", stacktrace)
            }
            return exception
        }

        private fun getExceptionsJsonObject(exceptions: JsonArray): JsonObject {
            val result = JsonObject()
            result.put("exceptions", exceptions)
            return result
        }


        fun databaseError(throwable: Throwable) = FailingReplyThrowable(throwable, WebStatusCode.SERVICE_UNAVAILABLE)
        fun corruptedDatabase(throwable: Throwable) = FailingReplyThrowable(throwable, WebStatusCode.INTERNAL_ERROR)

        fun emptyBody(throwable: Throwable, correctData: JsonObject): FailingReplyThrowable =
                malformedRequest(throwable, correctData, null)

        fun malformedRequest(throwable: Throwable, correctData: JsonObject, received: String? = null): FailingReplyThrowable {
            val failingReply = FailingReplyThrowable(throwable, WebStatusCode.BAD_REQUEST)
            val reply = JsonObject()
            reply.put("correct", correctData)
            reply.put("received", received)
            failingReply.replyJson = reply
            return failingReply
        }

        fun unknownError(throwable: Throwable) = FailingReplyThrowable(throwable, WebStatusCode.INTERNAL_ERROR)

        fun invalidUsername(): FailingReplyThrowable {
            val failingReply = FailingReplyThrowable(NullPointerException("Username does not exist"), WebStatusCode.UNAUTHORIZED)
            return failingReply
        }

        fun invalidPassword(): FailingReplyThrowable {
            val failingReply = FailingReplyThrowable(AssertionError("Invalid Password Supplied"), WebStatusCode.UNAUTHORIZED)
            return failingReply
        }
    }
}