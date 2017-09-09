import handler.LoginUserHandler
import handler.RegisterUserHandler
import handler.VerifyUserHandler
import io.vertx.ext.web.Router
import service.AbstractService
import service.RoutingPath

class UserService : AbstractService() {

    override fun addRouting(router: Router) {
        router.get(RoutingPath.LOGIN_USER.toString()).handler(wrapHandler(LoginUserHandler()))
        router.get(RoutingPath.REGISTER_USER.toString()).handler(wrapHandler(RegisterUserHandler()))
        router.get(RoutingPath.VERIFY_USER.toString()).handler(wrapHandler(VerifyUserHandler()))
    }

}