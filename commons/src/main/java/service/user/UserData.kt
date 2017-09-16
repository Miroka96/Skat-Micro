package service.user

import service.CorrectDataTemplate


interface IMinimalUserData {
    var username: String
}

interface ITokenUserData : IMinimalUserData {
    var id: Int
}

interface ILoginUserData : IMinimalUserData {
    var password: String
}

interface ILoggedInUserData : ITokenUserData, ILoginUserData

interface IRegisterUserData : ILoginUserData {
    var firstName: String
    var lastName: String
}

interface IUserData : IRegisterUserData, ITokenUserData

data class LoginUserData(
        override var username: String = "",
        override var password: String = ""
) : ILoginUserData {

    companion object : CorrectDataTemplate() {
        override val correctData: LoginUserData by lazy {
            val login = LoginUserData("USERNAME")
            login.password = "PASSWORD"
            return@lazy login
        }
    }
}

data class LoggedInUserData(
        override var id: Int = 0,
        override var username: String = "",
        override var password: String = ""
) : ILoggedInUserData {

    companion object {
        val template by lazy {
            LoggedInUserData()
        }
    }
}

data class RegisterUserData(
        override var username: String = ""
) : IRegisterUserData {

    override lateinit var password: String
    override lateinit var firstName: String
    override lateinit var lastName: String

    companion object : CorrectDataTemplate() {
        override val correctData: RegisterUserData by lazy {
            val registration = RegisterUserData("USERNAME")
            registration.password = "PASSWORD"
            registration.firstName = "FIRST_NAME"
            registration.lastName = "LAST_NAME"
            return@lazy registration
        }
    }
}

data class TokenUserData(
        override var id: Int = 0
) : ITokenUserData {

    override lateinit var username: String

    constructor(userData: UserData) : this(userData.id) {
        this.username = userData.username
    }
}

data class UserData(
        override var id: Int = 0
) : IUserData {

    override lateinit var username: String
    override lateinit var password: String
    override lateinit var firstName: String
    override lateinit var lastName: String

    constructor(newUser: IRegisterUserData) : this() {
        this.username = newUser.username
        this.password = newUser.password
        this.firstName = newUser.firstName
        this.lastName = newUser.lastName
    }

    constructor(userLogin: ILoggedInUserData) : this() {
        this.id = userLogin.id
        this.username = userLogin.username
        this.password = userLogin.password
    }
}