package service


abstract class AbstractRequestHandler {

    open var needsDatabaseConnection = false
    open var needsGame = false


    abstract fun handleRequest(requestObject: RequestObject)

    open fun handleFailedInitialization(requestObject: RequestObject) {
        for (future in requestObject.futures) {
            if (future.cause() != null) {
                future.cause().printStackTrace()
            }
        }
        if (requestObject.finishingFuture.cause() != null) {
            requestObject.finishingFuture.cause().printStackTrace()
        }
    }

}