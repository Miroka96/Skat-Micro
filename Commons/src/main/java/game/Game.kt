package game

import cards.model.Hand
import database.AbstractDataWrapper
import database.DatabaseKeyAccess
import game.model.GameData
import game.model.GameDataAnonymous
import game.model.GameDataUser
import game.model.IGameData
import player.Player

class Game(var gameData: GameData) : IGameData by gameData, AbstractDataWrapper() {
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

    fun countPlayers(): Int {
        var count = 0
        for (player in players) {
            if (!player.isEmpty()) count++
        }
        return count
    }

    fun createUserGameData(userId: Int): GameDataUser {
        val hand = getPlayerHand(userId)
        val data = GameDataUser(id, hand)
        with(data) {
            players = gameData.players
            mode = gameData.mode
            value = gameData.value
            latestBid = gameData.latestBid
            latestPlayer = gameData.latestPlayer
        }
        return data
    }

    fun createUserGameDataJson(userId: Int) = dataToJson(createUserGameData(userId))

    fun createAnonymousGameData(): GameDataAnonymous {
        val data = GameDataAnonymous(id)
        data.mode = gameData.mode
        data.playercount = countPlayers()
        return data
    }

    fun createAnonymousGameDataJson() = dataToJson(createAnonymousGameData())

    override fun getData() = gameData

    companion object : DatabaseKeyAccess {
        override fun getDbKeyFromId(id: Int) = "skat::game::$id"
        override fun getLatestIdKey() = "skat::game::id"
    }
}