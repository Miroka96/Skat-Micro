package game.model

import cards.CardsetGenerator
import cards.model.Cardset
import player.model.PlayerData

interface IGameDataCommon {

    var id: Int

    var value: Int
    var latestBid: Int
    var latestPlayer: Int
}

interface IGameData : IGameDataCommon {
    var cardset: Cardset
}

data class GameData(
        override var id: Int,
        override var cardset: Cardset
) : IGameData {
    constructor() : this(-1, CardsetGenerator().generateShuffledCardset())

    var players = Array<PlayerData>(3) { _ ->
        PlayerData()
    }

    var mode = 0

    override var value = 0
    override var latestBid = -1
    override var latestPlayer = -1
}