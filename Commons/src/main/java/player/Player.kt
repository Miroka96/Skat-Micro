package player

import player.model.PlayerData

class Player(var playerData: PlayerData) {
    constructor() : this(PlayerData())

}