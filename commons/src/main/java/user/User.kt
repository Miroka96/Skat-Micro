package user

import database.AbstractDataWrapper
import database.DatabaseKeyAccess

class User(
        var userData: UserData
) : IUserData by userData, AbstractDataWrapper(), DatabaseKeyAccess by Companion {
    override fun getData() = userData

    constructor() : this(UserData())

    fun createTokenUserData() = TokenUserData(userData)

    fun createTokenUserDataJson() = dataToJson(createTokenUserData())

    companion object : DatabaseKeyAccess {
        override fun getDbKeyFromId(id: Int) = "skat::user::$id"
        override fun getLatestIdKey() = "skat::user::id"
    }
}