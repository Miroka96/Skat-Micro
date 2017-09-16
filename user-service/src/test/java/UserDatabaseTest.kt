import com.couchbase.client.java.query.AsyncN1qlQueryResult
import com.couchbase.client.java.query.AsyncN1qlQueryRow
import com.couchbase.client.java.query.Select
import com.couchbase.client.java.query.dsl.Expression
import org.junit.Test
import service.user.LoggedInUserData

class UserDatabaseTest : AbstractDatabaseTest() {

    val queries by lazy {
        UserQueries(bucketname)
    }

    @Test
    fun getAttributes() {
        for (attribute in queries.getAttributeList(LoggedInUserData.template)) {
            println(attribute)
        }
    }

    @Test
    fun buildQuery() {
        val query = Select.select(* queries.getAttributeList(LoggedInUserData.template))
                .from(Expression.i(bucketname))
                .where(Expression.x("username").eq(Expression.x("\$username")))
    }

    @Test
    fun createQuery() {
        val query = queries.getUserByUsername("USERNAME")
    }

    @Test
    fun readAllUsers() {
        db.checkBucket()
                .flatMap { bucket ->
                    bucket.query(queries.getUserByUsername("USERNAME"))

                }
                .flatMap { res: AsyncN1qlQueryResult ->
                    res.rows()
                }
                .toBlocking()
                .forEach { row: AsyncN1qlQueryRow ->
                    println(row.value())
                }
    }
}