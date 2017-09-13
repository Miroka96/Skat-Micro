package database

interface DatabaseAccess {
    fun getDbKeyFromId(id: Int): String
    fun latestIdKey(): String
}