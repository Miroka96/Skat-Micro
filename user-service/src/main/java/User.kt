import service.database.AbstractDataWrapper
import service.database.DatabaseAccess
import service.user.IUserData
import service.user.TokenUserData
import service.user.UserData

class User(
        var userData: UserData
) : IUserData by userData, AbstractDataWrapper(), DatabaseAccess by Companion {
    override fun getData() = userData

    constructor() : this(UserData())

    fun createTokenUserData() = TokenUserData(userData)

    fun createTokenUserDataJson() = dataToJson(createTokenUserData())

    companion object : DatabaseAccess {
        override fun getDbKeyFromId(id: Int) = "user::$id"
        override fun latestIdKey() = "user::id"
    }
}