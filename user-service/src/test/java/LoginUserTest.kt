import database.CouchbaseAccess
import io.vertx.core.buffer.Buffer
import io.vertx.ext.unit.TestContext
import io.vertx.ext.web.client.HttpResponse
import org.junit.Test
import service.AbstractService
import service.RoutingPath
import service.WebStatusCode
import user.LoginUserData
import user.TokenUserData

class LoginUserTest : AbstractServiceTest() {
    override val uri: String = RoutingPath.LOGIN_USER.toString()

    override val apiService: AbstractService by lazy {
        UserService()
    }

    @Test
    fun testLogin(context: TestContext) {
        val request = client.post(port, host, uri)
        val buffer = Buffer.buffer(LoginUserData.correctDataJson)
        request.sendBuffer(buffer,
                context.asyncAssertSuccess { response: HttpResponse<Buffer> ->
                    context.assertEquals(response.statusCode(), WebStatusCode.OK.code)
                    try {
                        val tokenData = CouchbaseAccess.jsonmapper.readValue(response.bodyAsString(), TokenUserData::class.java)
                        context.assertEquals(LoginUserData.correctData.username, tokenData.username)
                        context.assertTrue(true)
                    } catch (ex: NullPointerException) {
                        context.assertTrue(false)
                    }
                })
    }

    @Test
    fun testInvalidLogin(context: TestContext) {
        val request = client.post(port, host, uri)
        val loginData = LoginUserData.correctData.copy("")
        loginData.password = "invalid"
        val buffer = Buffer.buffer()
        request.sendBuffer(buffer,
                context.asyncAssertSuccess { response: HttpResponse<Buffer> ->
                    context.assertEquals(response.statusCode(), WebStatusCode.UNAUTHORIZED.code)
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