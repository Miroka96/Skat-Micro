import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.query.N1qlParams
import com.couchbase.client.java.query.N1qlQuery
import com.couchbase.client.java.query.ParameterizedN1qlQuery
import com.couchbase.client.java.query.Select.select
import com.couchbase.client.java.query.consistency.ScanConsistency
import com.couchbase.client.java.query.dsl.Expression.i
import com.couchbase.client.java.query.dsl.Expression.x
import database.AbstractQueries
import user.LoggedInUserData

class UserQueries(bucketname: String) : AbstractQueries(bucketname) {
    private val prepared = N1qlParams.build().adhoc(false)
    private val preparedConsistent = N1qlParams.build().adhoc(false).consistency(ScanConsistency.REQUEST_PLUS)

    private val userByUsername = select(* getAttributeList(LoggedInUserData.template))
            .from(i(bucketname))
            .where(x("username").eq(x("\$username")))

    fun getUserByUsername(username: String): ParameterizedN1qlQuery {
        return N1qlQuery.parameterized(userByUsername,
                JsonObject.create().put("username", username),
                preparedConsistent)
    }
}