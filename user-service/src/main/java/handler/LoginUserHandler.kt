package handler

import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.document.JsonDocument
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.vertx.core.Future
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import service.AbstractRequestHandler
import service.FailingReplyThrowable
import service.RequestObject
import service.WebStatusCode
import user.LoginUserData
import user.User
import user.UserData

class LoginUserHandler : AbstractRequestHandler() {
    override val successfulResponseCode = WebStatusCode.OK

    override val operationFutureCount: Int = 1

    override val needsDatabaseConnection = true

    override fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>) {
        loginUser(requestObject.routingContext, replyFuture, operationFutures[0], requestObject.bucket!!)
    }

    fun loginUser(routingContext: RoutingContext, replyFuture: Future<String>, database: Future<out Any>, bucket: AsyncBucket) {

        bucket.counter(User.getLatestIdKey(), 1, 1)
                .map { doc -> doc.content().toInt() }
                .map { id ->
                    val body = routingContext.bodyAsString
                    try {
                        val user: User
                        val login = JsonObject(body).mapTo(LoginUserData::class.java)
                        user = User(UserData(login))
                        user.id = id
                        return@map user
                    } catch (ex: NullPointerException) {
                        throw FailingReplyThrowable.emptyBody(ex, LoginUserData.correctDataJsonObject)
                    } catch (ex: DecodeException) {
                        throw FailingReplyThrowable.emptyBody(ex, LoginUserData.correctDataJsonObject)
                    } catch (ex: MismatchedInputException) {
                        throw FailingReplyThrowable.malformedRequest(ex, LoginUserData.correctDataJsonObject, body)
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