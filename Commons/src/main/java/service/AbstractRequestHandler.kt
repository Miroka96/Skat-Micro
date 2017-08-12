package service

abstract class AbstractRequestHandler {
    open var needsUser = false
    open var needsDatabaseConnection = false

    abstract fun handleRequest(requestObject: RequestObject)
}