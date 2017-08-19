package game.model

import cards.model.Cardset
import player.model.PlayerData

data class GameData(
        var cardset: Cardset
) {

    var players = Array<PlayerData>(3) { _ ->
        PlayerData()
    }

    var mode = 0

    var gameValue = 0
    var latestBid = -1
    var latestPlayer = -1
}