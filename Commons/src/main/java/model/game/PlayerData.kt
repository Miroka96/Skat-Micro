package model.game

data class PlayerData(var userID: Int) {
    var trickCount = 0
    var score = 0

    constructor() : this(0)
}