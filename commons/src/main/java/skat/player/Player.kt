package skat.player

import skat.player.model.IPlayerData
import skat.player.model.PlayerData

class Player(var playerData: PlayerData) : IPlayerData by playerData {
    constructor() : this(PlayerData())

    fun isEmpty() = userId == 0
}