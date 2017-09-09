package database

interface DatabaseKeyAccess {
    fun getDbKeyFromId(id: Int): String

    fun getLatestIdKey(): String
}