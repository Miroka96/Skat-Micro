package skat

import service.database.AbstractDataWrapper
import service.database.DatabaseAccess
import skat.cards.model.Hand
import skat.model.GameData
import skat.model.GameDataAnonymous
import skat.model.GameDataUser
import skat.model.IGameData
import skat.player.Player

class Game(var gameData: GameData) : IGameData by gameData, AbstractDataWrapper(), DatabaseAccess by Companion {
    constructor() : this(GameData())

    var gameMode = GameMode.fromId(gameData.mode)
    var players: List<Player> = gameData.players.map { playerData -> Player(playerData) }

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

    companion object : DatabaseAccess {
        override fun getDbKeyFromId(id: Int) = "skat::game::$id"
        override fun latestIdKey() = "skat::game::id"
    }
}