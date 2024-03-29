import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.ext.web.client.WebClient
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import service.AbstractService

@RunWith(VertxUnitRunner::class)
abstract class AbstractServiceTest : AbstractVertxTest() {

    val client: WebClient by lazy {
        WebClient.create(vertx)
    }
    val host: String = "localhost"
    abstract val uri: String
    var port = 0
    abstract val apiService: AbstractService

    @Before
    override fun before(context: TestContext) {
        port = apiService.defaultPort

        vertx.deployVerticle(apiService, context.asyncAssertSuccess<String>())
    }

    @After
    override fun after(context: TestContext) {
        vertx.close(context.asyncAssertSuccess())
    }
}