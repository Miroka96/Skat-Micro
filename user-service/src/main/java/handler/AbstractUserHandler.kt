package handler

import User
import UserService
import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.query.AsyncN1qlQueryResult
import com.couchbase.client.java.query.AsyncN1qlQueryRow
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import rx.Observable
import service.AbstractRequestHandler
import service.FailingReplyThrowable
import user.LoggedInUserData
import user.LoginUserData
import user.UserData

abstract class AbstractUserHandler : AbstractRequestHandler() {

    fun queryLoggedInUserData(
            bucket: AsyncBucket,
            userData: LoginUserData,
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
                .doOnError { ex ->
                    if (ex is IllegalArgumentException) {
                        println("multiple Database Entries for ${userData.username}")
                        database.fail(FailingReplyThrowable.corruptedDatabase(ex))
                    }
                }
                .map { row: AsyncN1qlQueryRow? ->
                    try {
                        JsonObject(row!!.value().toMap()).mapTo(LoggedInUserData::class.java)
                    } catch (ex: NullPointerException) {
                        throw FailingReplyThrowable.invalidUsername()
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