package service

class RoutingPath private constructor(private val path: String) {
    private val prefix = "/"

    override fun toString(): String {
        return prefix + path
    }

    companion object {
        val JOIN_GAME = RoutingPath("join")
        val LEAVE_GAME = RoutingPath("leave")
        val CREATE_GAME = RoutingPath("create")

    }
}