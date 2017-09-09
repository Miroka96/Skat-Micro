package game

class UnallowedModeException(message: String) : Exception("Action does not match actual Game Mode - " + message) {
    constructor() : this("")
}