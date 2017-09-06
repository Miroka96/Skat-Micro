package game.model

import cards.model.Hand
import player.model.PlayerData

interface IGameDataInformation : IGameDataMinimal {
    var mode: Int
}

interface IGameDataUser : IGameDataCommon, IGameDataInformation {
    var hand: Hand
    var players: Array<PlayerData>
}

data class GameDataUser(
        override var id: Int,
        override var hand: Hand
) : IGameDataUser {

    override var players = Array<PlayerData>(3) {
        PlayerData()
    }

    override var mode = 0

    override var value = 0
    override var latestBid = -1
    override var latestPlayer = -1
}