package handler

import User
import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.document.JsonDocument
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.vertx.core.Future
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import rx.Observable
import service.request.RequestObject
import service.response.FailingReplyThrowable
import service.response.WebStatusCode
import service.user.LoggedInUserData
import service.user.RegisterUserData
import service.user.UserData

class RegisterUserHandler : AbstractUserHandler() {
    override val operationFutureCount: Int = 1
    override val needsDatabaseConnection = true

    override val successfulResponseCode = WebStatusCode.CREATED

    override fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>) {
        registerUser(requestObject.routingContext, replyFuture, operationFutures[0], requestObject.bucket!!)
    }

    fun registerUser(routingContext: RoutingContext, replyFuture: Future<String>, database: Future<out Any>, bucket: AsyncBucket) {
        Observable.just(routingContext.bodyAsString)
                .map { body ->
                    try {
                        User(
                                UserData(
                                        JsonObject(body)
                                                .mapTo(RegisterUserData::class.java)))
                    } catch (ex: NullPointerException) {
                        throw FailingReplyThrowable.emptyBody(ex, RegisterUserData.correctDataJsonObject)
                    } catch (ex: DecodeException) {
                        throw FailingReplyThrowable.emptyBody(ex, RegisterUserData.correctDataJsonObject)
                    } catch (ex: MismatchedInputException) {
                        throw FailingReplyThrowable.malformedRequest(ex, RegisterUserData.correctDataJsonObject, body)
                    }
                }
                .flatMap { user: User ->
                    queryLoggedInUserData(bucket, user, database)
                            .onErrorReturn { ex ->
                                if (ex !is FailingReplyThrowable || ex.cause !is NullPointerException) {
                                    throw ex
                                }
                                null
                            }
                            .map { databaseUser: LoggedInUserData? ->
                                if (databaseUser != null) {
                                    throw FailingReplyThrowable.invalidUsername(IllegalArgumentException("Username already taken"))
                                }
                                user
                            }
                }
                .flatMap { user ->
                    bucket.counter(User.latestIdKey(), 1, 1)
                            .map { doc -> doc.content().toInt() }
                            .map { id ->
                                user.id = id
                                user
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
                            .doOnCompleted {
                                bucket.close()
                            }
                }
                .subscribe(
                        { it: JsonDocument ->
                            print("new User registered: ")
                            println(it)
                            database.complete()
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