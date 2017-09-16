package handler

import User
import UserService
import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.query.AsyncN1qlQueryResult
import com.couchbase.client.java.query.AsyncN1qlQueryRow
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import rx.Observable
import service.request.AbstractRequestHandler
import service.response.FailingReplyThrowable
import service.user.IMinimalUserData
import service.user.LoggedInUserData
import service.user.LoginUserData
import service.user.UserData

abstract class AbstractUserHandler : AbstractRequestHandler() {

    fun queryLoggedInUserData(
            bucket: AsyncBucket,
            userData: IMinimalUserData,
            database: Future<out Any>
    ): Observable<LoggedInUserData> {
        return bucket.query(UserService.queries.getUserByUsername(userData.username))
                .doOnError { it ->
                    database.fail(FailingReplyThrowable.databaseError(it))
                }
                .flatMap { queryResult: AsyncN1qlQueryResult ->
                    queryResult.rows()
                }
                .singleOrDefault(null)
                .onErrorReturn { ex ->
                    if (ex is IllegalArgumentException) {
                        println("multiple Database Entries for ${userData.username}")
                        val failingReply = FailingReplyThrowable.corruptedDatabase(ex)
                        database.fail(failingReply)
                        throw failingReply
                    }
                    throw ex
                }
                .map { row: AsyncN1qlQueryRow? ->
                    try {
                        JsonObject(row!!.value().toMap()).mapTo(LoggedInUserData::class.java)
                    } catch (ex: NullPointerException) {
                        throw FailingReplyThrowable.invalidUsername(ex)
                    }
                }
    }

    fun checkLoginData(
            bucket: AsyncBucket,
            userData: LoginUserData,
            database: Future<out Any>
    ): Observable<User> {
        return queryLoggedInUserData(bucket, userData, database)
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
}