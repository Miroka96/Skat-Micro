package game

import game.model.GameData
import player.Player

class Game(var gameData: GameData) {
    constructor() : this(GameData())


    var gameMode = GameMode.fromId(gameData.mode)
    var players = Array<Player>(3) { i ->
        Player(gameData.players[i])
    }


}