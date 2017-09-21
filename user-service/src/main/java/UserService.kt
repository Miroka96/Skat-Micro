import handler.*
import io.vertx.core.Future
import io.vertx.ext.web.Router
import service.AbstractService
import service.RoutingPath
import service.database.AbstractQueries
import java.util.*


class UserService : AbstractService() {

    override val serviceName: String = "user"

    override val defaultPort = defaultJWTPort

    override fun addRouting(router: Router) {
        router.post(RoutingPath.LOGIN_USER.toString()).handler(wrapHandler(LoginUserHandler()))
        router.post(RoutingPath.REGISTER_USER.toString()).handler(wrapHandler(RegisterUserHandler()))
        router.get(RoutingPath.VERIFY_TOKEN.toString()).handler(wrapHandler(VerifyTokenHandler()))
        router.get(RoutingPath.REFRESH_TOKEN.toString()).handler(wrapHandler(RefreshTokenHandler()))
        router.get(RoutingPath.PUBLIC_KEY.toString()).handler(wrapHandler(PublicKeyHandler()))
    }

    override val queries: AbstractQueries by lazy { UserService.queries }

    override val createKeyStorePermission = true

    override fun customStart(continueFuture: Future<Unit>) {
        UserService.queries = UserQueries(db.bucketName)
        continueFuture.complete()
    }

    val keyStore by lazy {
        keyStoreManager.loadKeyStore()
    }

    val publicKey by lazy {
        keyStoreManager.getPublicKey(keyStore, tokenOptions.algorithm)
    }

    val publicKey64 by lazy {
        Base64.getEncoder().encodeToString(publicKey.encoded)
    }

    companion object {
        lateinit var queries: UserQueries
    }
}