package skat.player.model

interface IPlayerData {
    var userId: Int
    var trickCount: Int
    var score: Int
}

data class PlayerData(override var userId: Int) : IPlayerData {
    override var trickCount = 0
    override var score = 0

    constructor() : this(0)
}