package handler

import User
import UserService
import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.query.AsyncN1qlQueryResult
import com.couchbase.client.java.query.AsyncN1qlQueryRow
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.vertx.core.Future
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import rx.Observable
import service.AbstractRequestHandler
import service.FailingReplyThrowable
import service.RequestObject
import service.WebStatusCode
import user.LoggedInUserData
import user.LoginUserData
import user.UserData

class LoginUserHandler : AbstractRequestHandler() {
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
                        println(body)
                        val login = JsonObject(body).mapTo(LoginUserData::class.java)
                        return@map login
                    } catch (ex: NullPointerException) {
                        throw FailingReplyThrowable.emptyBody(ex, LoginUserData.correctDataJsonObject)
                    } catch (ex: DecodeException) {
                        throw FailingReplyThrowable.malformedRequest(ex, LoginUserData.correctDataJsonObject, body)
                    } catch (ex: MismatchedInputException) {
                        throw FailingReplyThrowable.malformedRequest(ex, LoginUserData.correctDataJsonObject, body)
                    }
                }
                .flatMap { userData ->
                    bucket.query(UserService.queries.getUserByUsername(userData.username))
                            .doOnError { it ->
                                database.fail(FailingReplyThrowable.databaseError(it))
                            }
                            .flatMap { queryResult: AsyncN1qlQueryResult ->
                                queryResult.rows()
                            }
                            .singleOrDefault(null)
                            .map { row: AsyncN1qlQueryRow ->
                                try {
                                    JsonObject(row.value().toMap()).mapTo(LoggedInUserData::class.java)
                                } catch (ex: NullPointerException) {
                                    throw FailingReplyThrowable.invalidUsername()
                                }
                            }
                            .doOnNext { validData ->
                                if (!validData.password.equals(userData.password)) {
                                    throw FailingReplyThrowable.invalidPassword()
                                }
                                database.complete()
                            }
                            .map { validData: LoggedInUserData ->
                                User(UserData(validData))
                            }
                }
                .doOnNext { user: User ->
                    val tokenUserData = user.createTokenUserDataJson()
                    replyFuture.complete(tokenUserData)
                }
                .subscribe(
                        { user: User ->
                            print("User logged in: ")
                            println(user)
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