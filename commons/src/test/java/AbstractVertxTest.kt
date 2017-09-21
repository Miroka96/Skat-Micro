import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(VertxUnitRunner::class)
abstract class AbstractVertxTest {
    @Rule
    @JvmField
    var rule = RunTestOnContext()

    val vertx: Vertx by lazy {
        rule.vertx()
    }

    @Before
    open fun before(context: TestContext) {
        context.assertTrue(true)
    }

    @After
    open fun after(context: TestContext) {
        context.assertTrue(true)
    }
}