package model.logging

data class LogEntry(var ip: String, var port: Int) {
    var service
}