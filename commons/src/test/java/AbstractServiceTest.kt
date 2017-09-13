import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.ext.web.client.WebClient
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import service.AbstractService

@RunWith(VertxUnitRunner::class)
abstract class AbstractServiceTest {
    @Rule
    @JvmField
    var rule = RunTestOnContext()

    val vertx: Vertx by lazy {
        rule.vertx()
    }
    val client: WebClient by lazy {
        WebClient.create(vertx)
    }
    val host: String = "localhost"
    abstract val uri: String
    var port = 0
    abstract val apiService: AbstractService

    @Before
    fun before(context: TestContext) {

        port = apiService.defaultPort

        vertx.deployVerticle(apiService, context.asyncAssertSuccess<String>())
    }

    @After
    fun after(context: TestContext) {
        vertx.close(context.asyncAssertSuccess())
    }
}