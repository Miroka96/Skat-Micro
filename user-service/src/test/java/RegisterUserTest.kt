import com.fasterxml.jackson.databind.ObjectMapper
import database.CouchbaseAccess
import io.vertx.core.buffer.Buffer
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import org.junit.Test
import org.junit.runner.RunWith
import service.AbstractService
import service.RoutingPath
import service.WebStatusCode
import user.RegisterUserData
import user.TokenUserData


@RunWith(VertxUnitRunner::class)
class RegisterUserTest : AbstractServiceTest() {

    override val uri: String = RoutingPath.REGISTER_USER.toString()
    override val apiService: AbstractService by lazy {
        UserService()
    }

    @Test
    fun testPost(context: TestContext) {
        val request: HttpRequest<Buffer> = client.post(port, host, uri)
        val data = RegisterUserData.correctData
        data.username = data.username + (System.currentTimeMillis() / 1000)
        val buffer = Buffer.buffer(ObjectMapper().writeValueAsString(data))
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

                    testReRegistration(context, request, buffer)
                })
    }

    fun testReRegistration(context: TestContext, request: HttpRequest<Buffer>, buffer: Buffer) {
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