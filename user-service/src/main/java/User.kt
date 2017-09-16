import database.AbstractDataWrapper
import database.DatabaseAccess
import user.IUserData
import user.TokenUserData
import user.UserData

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