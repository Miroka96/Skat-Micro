package user

interface ILoginUserData {
    var username: String
    var password: String
}

interface IRegisterUserData : ILoginUserData

open class LoginUserData {
    var username: String? = null
    var password: String? = null

    constructor()

    constructor(
            username: String,
            password: String
    ) : this() {
        this.username = username
        this.password = password
    }
}

open class LoggedInUserData : LoginUserData {
    var id: Int? = null

    constructor()
    constructor(
            username: String,
            password: String,
            id: Int
    ) : super(username, password) {
        this.id = id
    }
}

class UserData : LoggedInUserData {

    constructor()
    constructor(
            username: String,
            password: String,
            id: Int
    ) : super(username, password, id)
}