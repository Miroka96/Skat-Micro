import database.AbstractQueries
import handler.LoginUserHandler
import handler.RegisterUserHandler
import handler.VerifyUserHandler
import io.vertx.ext.web.Router
import service.AbstractService
import service.RoutingPath

class UserService : AbstractService() {

    override val defaultPort = 8090

    override fun addRouting(router: Router) {
        router.post(RoutingPath.LOGIN_USER.toString()).handler(wrapHandler(LoginUserHandler()))
        router.post(RoutingPath.REGISTER_USER.toString()).handler(wrapHandler(RegisterUserHandler()))
        router.get(RoutingPath.VERIFY_USER.toString()).handler(wrapHandler(VerifyUserHandler()))
    }

    override val queries: AbstractQueries by lazy { UserService.queries }

    override fun customStart() {
        UserService.queries = UserQueries(db.bucketName)
    }

    companion object {
        lateinit var queries: UserQueries
    }
}