package player.model

data class PlayerData(var userID: Int) {
    var trickCount = 0
    var score = 0

    constructor() : this(0)
}