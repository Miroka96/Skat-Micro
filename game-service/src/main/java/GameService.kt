import database.AbstractQueries
import handler.CreateGameHandler
import handler.JoinGameHandler
import handler.LeaveGameHandler
import io.vertx.ext.web.Router
import service.AbstractService
import service.RoutingPath

class GameService : AbstractService() {

    override val defaultPort = 8091

    override fun addRouting(router: Router) {
        router.get(RoutingPath.JOIN_GAME.toString()).handler(wrapHandler(JoinGameHandler()))
        router.get(RoutingPath.LEAVE_GAME.toString()).handler(wrapHandler(LeaveGameHandler()))
        router.get(RoutingPath.CREATE_GAME.toString()).handler(wrapHandler(CreateGameHandler()))
    }

    override val queries: AbstractQueries by lazy { GameService.queries }

    override fun customStart() {
        GameService.queries = GameQueries(db.bucketName)
    }

    companion object {
        lateinit var queries: GameQueries
    }

}