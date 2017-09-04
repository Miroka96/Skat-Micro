package game

import cards.model.Hand
import game.model.GameData
import game.model.IGameData
import game.model.UserGameData
import player.Player

class Game(var gameData: GameData) : IGameData by gameData {
    constructor() : this(GameData())


    var gameMode = GameMode.fromId(gameData.mode)
    var players = Array<Player>(3) { i ->
        Player(gameData.players[i])
    }

    fun getPlayerIndex(userId: Int): Int {
        for (i in 0..2) {
            if (userId == players[i].userId)
                return i
        }
        return -1
    }

    fun isParticipating(userId: Int) = getPlayerIndex(userId) != -1

    fun getPlayerHand(userId: Int): Hand
            = cardset.hands[getPlayerIndex(userId)]


    fun createUserGameData(userId: Int): UserGameData {
        val hand = getPlayerHand(userId)
        val data = UserGameData(id, hand)
        with(data) {
            players = gameData.players
            mode = gameData.mode
            value = gameData.value
            latestBid = gameData.latestBid
            latestPlayer = gameData.latestPlayer
        }
        return data
    }

    companion object {
        fun getDbKeyFromId(gameId: Int) = "sg$gameId"
    }
}