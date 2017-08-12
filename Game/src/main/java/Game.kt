package skat

import model.cards.Card
import model.cards.CardColour
import skat.history.Draw
import skat.history.GameHistory
import skat.history.Gamemode
import skat.history.Round
import skat.player.Serverside
import skat.state.GameStatus
import skat.state.Playerstat
import java.util.*

class Game(val port: Int, val publicGameIndex: Int) {

    val cards = cards.Cardset(cards.Cardset.randomCardArray())
    val players = ArrayList<Serverside>(3)
    val playerStats = arrayOf<Playerstat>(
            Playerstat(cards.getPlayerCards(0)),
            Playerstat(cards.getPlayerCards(1)),
            Playerstat(cards.getPlayerCards(2)))
    val history = GameHistory(this)
    var recentReizwert = 0
    var recentPlayer = 0
    var gameStatus: GameStatus = GameStatus.notStarted
    var singlePlayer = -1
    var ansager = 2     //sagen
    var zuhoerer = 1    //hören
    var aufspiel = 0    //geben
    var gamemode: Gamemode? = null
    var stapel = ArrayList<Card>(3)
    var trumpf: CardColour? = null
    var geforderteFarbe: CardColour? = null
    var stichIndex = 0

    fun readyForStart() = (3 <= server.clients.size)
    fun startGame() {
        if (gameStatus !== GameStatus.notStarted || !readyForStart()) return
        gameStatus = GameStatus.reizen
        for (i in 0..2) players.add(Serverside(server.pullClient(), this, i))
        players[recentPlayer].turnNotification()
    }

    private fun nextPlayer() {
        recentPlayer = (++recentPlayer) % 3
    }

    fun calculateWinner(round: Round) {

    }

    fun getCardsFromNthStich(n: Int): ArrayList<Card> {
        val nround = history.getNthStich(n)
        val ret = ArrayList<Card>(3)
        if (nround == null) return ret
        for (d in nround.draws) {
            if (d.type == Draw.karteLegen) ret.add(d.karte!!)
        }
        return ret
    }

    fun getWinnerFromNthStich(n: Int): Int {
        val nround = history.getNthStich(n)
        val ret = -1
        if (nround == null) return ret
        return nround.winner
    }

    fun getSkat(playerId: Int): ArrayList<Card>? {
        if (singlePlayer != playerId) return null
        return cards.skat
    }

    //zugehörigen Spieler zu gelegter Karte erfragen
    fun getCardOwner(c: Card): Int {
        if (c in cards.originalSkat) return singlePlayer
        for (p in 0..2) {
            if (c in cards.getPlayerCards(p)) return p
        }
        return -1
    }

    fun getPlayedCardOwner(c: Card): Int {
        val owner = getCardOwner(c)
        if (owner == -1) return -1
        if (c in playerStats[owner].handkarten) return -1
        if (c in cards.skat) return -1
        return owner
    }

    //Punkte der einzelnen Spieler erfragen


    //////////////////////////////////////////////Spielerinteraktion

    fun addDraw(draw: Draw): Boolean {
        //Draw anwenden
        return true
    }

    //eigenen Reizwert ansagen (skat.Draw übertragen)

    //skat.Skat weglegen

    //skat.Spielmodus ansagen

    //Karte legen
}