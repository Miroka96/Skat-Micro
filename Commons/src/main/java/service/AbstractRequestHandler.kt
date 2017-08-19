package service

import service.model.RequestObject

abstract class AbstractRequestHandler {
    open var needsDatabaseConnection = false
    open var needsUser = false
    open var needsGame = false


    abstract fun handleRequest(requestObject: RequestObject)
}