package game.model

import cards.model.Hand
import player.model.PlayerData

interface IUserGameData : IGameDataCommon {
    var hand: Hand
    var mode: Int
    var players: Array<PlayerData>
}

data class UserGameData(
        override var id: Int,
        override var hand: Hand
) : IUserGameData {

    override var players = Array<PlayerData>(3) {
        PlayerData()
    }

    override var mode = 0

    override var value = 0
    override var latestBid = -1
    override var latestPlayer = -1
}