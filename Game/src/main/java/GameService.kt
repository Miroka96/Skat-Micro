import database.CouchbaseAccess
import handler.CreateGameHandler
import handler.JoinGameHandler
import handler.LeaveGameHandler
import io.vertx.ext.web.Router
import service.AbstractService
import service.RoutingPath

class GameService : AbstractService() {

    lateinit var db: CouchbaseAccess

    override fun addRouting(router: Router) {
        router.get(RoutingPath.JOIN_GAME.toString()).handler(wrapHandler(JoinGameHandler()))
        router.get(RoutingPath.LEAVE_GAME.toString()).handler(wrapHandler(LeaveGameHandler()))
        router.get(RoutingPath.CREATE_GAME.toString()).handler(wrapHandler(CreateGameHandler()))
    }

    override fun customStart() {
        db = CouchbaseAccess(config())
    }

    override fun customStop() {
        db.closeBlocking(true)
    }
}