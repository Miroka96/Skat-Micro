package service

import database.CouchbaseAccess

abstract class CorrectDataTemplate {

    abstract val correctData: Any

    val correctDataJson: String by lazy {
        CouchbaseAccess.jsonmapper.writeValueAsString(correctData)
    }
}