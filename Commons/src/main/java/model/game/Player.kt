package model.game

data class Player(var userID: Int) {
    var trickCount = 0
    var score = 0

    constructor() : this(0)
}