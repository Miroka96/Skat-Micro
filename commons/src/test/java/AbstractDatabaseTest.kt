import service.database.CouchbaseAccess

abstract class AbstractDatabaseTest {
    val host = "172.17.0.2"
    val bucketname = "default"
    val bucketpassword = ""
    val db by lazy {
        CouchbaseAccess(host, bucketname, bucketpassword)
    }

}