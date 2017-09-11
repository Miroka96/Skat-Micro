import database.CouchbaseAccess
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import service.WebStatusCode
import user.RegisterUserData
import user.TokenUserData


@RunWith(VertxUnitRunner::class)
class RegisterUserTest {

    @Rule
    @JvmField
    var rule = RunTestOnContext()

    lateinit var vertx: Vertx
    lateinit var client: WebClient
    lateinit var host: String
    var port = 0
    lateinit var uri: String

    @Before
    fun before(context: TestContext) {
        vertx = rule.vertx()
        client = WebClient.create(vertx)

        val service = UserService()
        host = "localhost"
        port = service.defaultPort
        uri = "/register"
        vertx.deployVerticle(service, context.asyncAssertSuccess<String>())
    }

    @After
    fun after(context: TestContext) {
        vertx.close(context.asyncAssertSuccess())
    }

    @Test
    fun testPost(context: TestContext) {
        val request = client.post(port, host, uri)
        val buffer = Buffer.buffer(RegisterUserData.correctDataJson)
        request.sendBuffer(buffer,
                context.asyncAssertSuccess { response: HttpResponse<Buffer> ->
                    context.assertEquals(response.statusCode(), WebStatusCode.CREATED.code)
                    try {
                        val tokenData = CouchbaseAccess.jsonmapper.readValue(response.bodyAsString(), TokenUserData::class.java)
                        context.assertEquals(RegisterUserData.correctData.username, tokenData.username)
                        context.assertTrue(true)
                    } catch (ex: NullPointerException) {
                        context.assertTrue(false)
                    }
                })
    }

    @Test
    fun testEmptyPost(context: TestContext) {
        val request = client.post(port, host, uri)
        request.send(context.asyncAssertSuccess { response: HttpResponse<Buffer> ->
            context.assertEquals(response.statusCode(), WebStatusCode.BAD_REQUEST.code)
        })
    }

    @Test
    fun testInvalidPost(context: TestContext) {
        val request = client.post(port, host, uri)
        val buffer = Buffer.buffer("Hi, I am invalid")
        request.sendBuffer(buffer,
                context.asyncAssertSuccess { response: HttpResponse<Buffer> ->
                    context.assertEquals(response.statusCode(), WebStatusCode.BAD_REQUEST.code)
                })
    }
}