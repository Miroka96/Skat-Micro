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

        val LOGIN_USER = RoutingPath("login")
        val REGISTER_USER = RoutingPath("register")

        val VERIFY_TOKEN = RoutingPath("verify")
        val REFRESH_TOKEN = RoutingPath("refresh")
        val PUBLIC_KEY = RoutingPath("pubkey")
    }
}