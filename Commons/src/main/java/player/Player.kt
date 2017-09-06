package player

import player.model.IPlayerData
import player.model.PlayerData

class Player(var playerData: PlayerData) : IPlayerData by playerData {
    constructor() : this(PlayerData())

    fun isEmpty() = userId == 0
}