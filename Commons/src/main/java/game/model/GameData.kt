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

}