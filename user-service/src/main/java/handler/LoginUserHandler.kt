package handler

import User
import com.couchbase.client.java.AsyncBucket
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.vertx.core.Future
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import rx.Observable
import service.request.RequestObject
import service.response.FailingReplyThrowable
import service.response.WebStatusCode
import service.user.LoginUserData

class LoginUserHandler : AbstractUserHandler() {
    override val successfulResponseCode = WebStatusCode.OK

    override val operationFutureCount: Int = 1

    override val needsDatabaseConnection = true


    override fun startOperation(requestObject: RequestObject, replyFuture: Future<String>, operationFutures: Array<Future<out Any>>) {
        loginUser(requestObject.routingContext, replyFuture, operationFutures[0], requestObject.bucket!!)
    }

    fun loginUser(routingContext: RoutingContext, replyFuture: Future<String>, database: Future<out Any>, bucket: AsyncBucket) {
        Observable.just(routingContext.bodyAsString)
                .map { body ->
                    try {
                        JsonObject(body).mapTo(LoginUserData::class.java)
                    } catch (ex: NullPointerException) {
                        throw FailingReplyThrowable.emptyBody(ex, LoginUserData.correctDataJsonObject)
                    } catch (ex: DecodeException) {
                        throw FailingReplyThrowable.malformedRequest(ex, LoginUserData.correctDataJsonObject, body)
                    } catch (ex: MismatchedInputException) {
                        throw FailingReplyThrowable.malformedRequest(ex, LoginUserData.correctDataJsonObject, body)
                    }
                }
                .flatMap { userData: LoginUserData ->
                    checkLoginData(bucket, userData, database)
                }
                .doOnNext { user: User ->
                    val tokenUserData = user.createTokenUserDataJson()

                    replyFuture.complete(tokenUserData)
                }
                .subscribe(
                        { user: User ->
                            println("User logged in: ${user.username}")
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
                        },
                        {
                            bucket.close()
                        })
    }

}