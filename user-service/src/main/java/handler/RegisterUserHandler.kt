package handler

import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.document.JsonDocument
import com.fasterxml.jackson.databind.ObjectMapper
import database.CouchbaseAccess
import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import service.AbstractRequestHandler
import service.FailingReplyThrowable
import service.RequestObject
import user.RegisterUserData
import user.User
import user.UserData

class RegisterUserHandler : AbstractRequestHandler() {
    override val operationFutureCount: Int = 1
    override val needsDatabaseConnection = true

    override fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>) {
        startOperation(requestObject.routingContext, replyFuture, operationFutures[0], requestObject.bucket!!, CouchbaseAccess.jsonmapper)
    }

    fun startOperation(routingContext: RoutingContext, replyFuture: Future<String>, database: Future<out Any>, bucket: AsyncBucket, jsonMapper: ObjectMapper) {
        bucket.counter(User.getLatestIdKey(), 1, 1)
                .map { doc -> doc.content().toInt() }
                .map { id ->
                    val user: User
                    try {
                        val register = jsonMapper.readValue(routingContext.bodyAsString, RegisterUserData::class.java)
                        user = User(UserData(register))
                        user.id = id
                        return@map user
                    } catch (ex: NullPointerException) {
                        throw FailingReplyThrowable.malformedRequest(ex, RegisterUserData.correctDataJson)
                    }
                }
                .doOnNext { user: User ->
                    val tokenUserData = user.createTokenUserDataJson()
                    replyFuture.complete(tokenUserData)
                }
                .map { user ->
                    user.dataToJsonDocument()
                }
                .flatMap { userDoc ->
                    bucket.upsert(userDoc).doOnError { it ->
                        database.fail(FailingReplyThrowable.databaseError(it))
                    }
                }
                .doOnNext {
                    database.complete()
                }
                .doOnCompleted {
                    bucket.close()
                }
                .subscribe(
                        { it: JsonDocument ->
                            print("new User registered: ")
                            println(it)
                        },
                        { ex: Throwable ->
                            val failingReply: FailingReplyThrowable
                            if (ex is FailingReplyThrowable) {
                                failingReply = ex
                            } else {
                                failingReply = FailingReplyThrowable.unknownError(ex)
                                ex.printStackTrace()
                            }
                            replyFuture.fail(failingReply)
                        })
    }
}