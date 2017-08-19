package logging

data class LogEntry(var ip: String, var port: Int) {
    var serviceName = service.Service.GAME
}