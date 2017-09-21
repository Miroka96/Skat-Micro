import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import io.vertx.ext.web.client.HttpResponse
import org.junit.Test
import service.AbstractService
import service.RoutingPath
import service.response.WebStatusCode
import service.user.LoginUserData

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
                        val res = JsonObject(response.bodyAsString())
                        println("Got Token: ${res.getString("token")}")
                        context.assertTrue(true)
                    } catch (ex: Exception) {
                        context.assertTrue(false)
                    }

                })
    }

    @Test
    fun testInvalidLogin(context: TestContext) {
        val request = client.post(port, host, uri)
        val loginData = LoginUserData.correctData.copy("")
        loginData.password = "invalid"
        val loginDataJson = JsonObject.mapFrom(loginData).toString()
        val buffer = Buffer.buffer(loginDataJson)
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