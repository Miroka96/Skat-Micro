package game.model

import cards.CardsetGenerator
import cards.model.Cardset
import player.model.PlayerData

interface IGameDataMinimal {
    var id: Int
}

interface IGameDataCommon : IGameDataMinimal {
    var value: Int
    var latestBid: Int
    var latestPlayer: Int
}

interface IGameData : IGameDataCommon {
    var cardset: Cardset
}

interface IGameDataDatabase : IGameData {
    var players: Array<PlayerData>
    var mode: Int
}

data class GameData(
        override var id: Int,
        override var cardset: Cardset
) : IGameDataDatabase {
    constructor() : this(-1, CardsetGenerator().generateShuffledCardset())

    override var players = Array<PlayerData>(3) { _ ->
        PlayerData()
    }

    override var mode = 0

    override var value = 0
    override var latestBid = -1
    override var latestPlayer = -1
}