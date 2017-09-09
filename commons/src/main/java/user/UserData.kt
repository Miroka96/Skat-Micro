package user


interface IMinimalUserData {
    var username: String
}

interface ITokenUserData : IMinimalUserData {
    var id: Int
}

interface ILoginUserData : IMinimalUserData {
    var password: String
}

interface IRegisterUserData : ILoginUserData {
    var firstName: String
    var lastName: String
}

data class LoginUserData(
        override var username: String
) : ILoginUserData {
    override lateinit var password: String
}

data class TokenUserData(
        override var id: Int
) : ITokenUserData {
    override lateinit var username: String
}

data class UserData(
        override var id: Int
) : IRegisterUserData, ITokenUserData {
    override lateinit var username: String

    override lateinit var password: String

    override lateinit var firstName: String

    override lateinit var lastName: String

}