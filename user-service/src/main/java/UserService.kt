import database.AbstractQueries
import handler.LoginUserHandler
import handler.RegisterUserHandler
import handler.VerifyUserHandler
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTOptions
import io.vertx.ext.web.Router
import jwt.KeyStoreManager
import service.AbstractService
import service.RoutingPath
import java.security.UnrecoverableKeyException


class UserService : AbstractService() {

    override val defaultPort = 8090

    override fun addRouting(router: Router) {
        router.post(RoutingPath.LOGIN_USER.toString()).handler(wrapHandler(LoginUserHandler()))
        router.post(RoutingPath.REGISTER_USER.toString()).handler(wrapHandler(RegisterUserHandler()))
        router.get(RoutingPath.VERIFY_USER.toString()).handler(wrapHandler(VerifyUserHandler()))
    }

    override val queries: AbstractQueries by lazy { UserService.queries }

    lateinit var authProvider: JWTAuth


    override fun customStart(continueFuture: Future<Unit>) {
        vertx.executeBlocking<Unit>({ future ->
            UserService.queries = UserQueries(db.bucketName)
            try {
                authProvider = KeyStoreManager(vertx, conf).getJWTAuthProvider()
                checkAuthProvider(future)
            } catch (keyEx: UnrecoverableKeyException) {
                future.fail(keyEx)
            } catch (ex: Exception) {
                println("Unknown Exception")
                ex.printStackTrace()
                future.fail(ex)
            }
        }, { res: AsyncResult<Unit> ->
            continueFuture.handle(res)
        })
    }

    fun checkAuthProvider(continueFuture: Future<Unit>) {
        val token: String = authProvider.generateToken(
                JsonObject().put("key", "test")
                , JWTOptions()
        )

        authProvider.authenticate(
                JsonObject().put("jwt", token)
        ) { res: AsyncResult<User> ->
            if (res.succeeded()) {
                continueFuture.complete()
            } else {
                println("JWT Test failed")
                continueFuture.fail(res.cause())
            }
        }
    }

    companion object {
        lateinit var queries: UserQueries
    }
}