package skat.player

import model.cards.CardColour
import model.cards.Cardset
import skat.history.Draw
import skat.history.Gamemode
import skat.state.GameStatus
import java.net.Socket
import java.util.*

/**
 * Created by mirko on 01.03.16.
 */
class Serverside(private val client: Socket, private val game: Game, val ownPlayerIndex: Int) : Runnable, Thread() {
    var errno = 0
        set(value) {
            field = value
            println("Game " + game.publicGameIndex.toString() + "; Player " + ownPlayerIndex.toString() + "; Error " + field.toString())
        }
    val reizwertTooLow = 1
    val invalidHandCard = 2
    val invalidDrawType = 3
    val invalidGameMode = 4
    val impossibleDraw = 5
    val notYourTurn = 6
    val permissionDenied = 7
    val switchIdenticalCards = 8
    val switchInvalidCards = 9
    val cardStillInGame = 10
    val invalidCard = 11

    val cardmin = 0
    val cardmax = 31
    val reizen = 32
    val wegSagen = 33
    val opening = 34
    val karteLegen = 35

    val karo = 0
    val herz = 1
    val pik = 2
    val kreuz = 3
    val nullspiel = 4
    val grand = 5
    val ramsch = 6

    val schneider = 100
    val schwarz = 200
    val ouvert = 400
    val hand = 800

    override fun run() {
        //communication listener

    }

    //Serverinterface
    fun turnNotification() {

    }

    //Helperfunctions
    // aktive Interaktion
    fun pushDraw(drawtype: Int, value: Int): Boolean {
        if (!isRecentPlayer()) {
            errno = notYourTurn
            return false
        }
        val draw: Draw
        when (drawtype) {
            in cardmin..cardmax -> {
                //Karten tauschen
                if (drawtype == value) {
                    errno = switchIdenticalCards
                    return false
                }
                var hand = handcards()
                var skat = skatcards(0) //silent
                if (drawtype !in hand && drawtype !in skat
                        || value !in hand && value !in skat) {
                    errno = switchInvalidCards
                    return false
                }
                draw = Draw(ownPlayerIndex, Cardset.std[drawtype]!!, Cardset.std[value]!!)
            }
            reizen -> {
                if (value <= recentReizwert()) {
                    errno = reizwertTooLow
                    return false
                }
                draw = Draw(ownPlayerIndex, value)
            }
            wegSagen -> draw = Draw(ownPlayerIndex, 0)
            opening -> {
                try {
                    draw = Draw(ownPlayerIndex, Gamemode.modeArray[value % schneider])
                } catch(ex: Exception) {
                    errno = invalidGameMode
                    return false
                }
                if (value % schwarz in schneider..schwarz - 1) draw.gamemode!!.setSchneider()
                if (value % ouvert in schwarz..ouvert - 1) draw.gamemode!!.setSchwarz()
                if (value % hand in ouvert..hand - 1) draw.gamemode!!.setOuvert()
                if (value >= hand) draw.gamemode!!.setHand()
            }
            karteLegen -> {
                if (!karteAufHand(value)) {
                    errno = invalidHandCard
                    return false
                }
                draw = Draw(ownPlayerIndex, Cardset.std[value]!!)
            }
            else -> {
                errno = invalidDrawType
                return false
            }
        }
        val ret = game.addDraw(draw)
        if (!ret) errno = impossibleDraw
        return ret
    }

    //passive Interaktionen
    fun recentReizwert() = game.recentReizwert

    fun recentPlayer() = game.recentPlayer
    fun isRecentPlayer() = (ownPlayerIndex == recentPlayer())

    fun gameStarted() = !(game.gameStatus == GameStatus.notStarted)
    fun gameFinished() = (game.gameStatus == GameStatus.finished)
    fun reizphase() = (game.gameStatus == GameStatus.reizen)
    fun openingphase() = (game.gameStatus == GameStatus.opening)
    fun spielphase() = (game.gameStatus == GameStatus.spiel)
    fun gamestatus() = GameStatus.stateArray.indexOf(game.gameStatus)
    fun gamemode() = Gamemode.modeArray.indexOf(game.gamemode)

    fun ansager() = game.ansager
    fun isAnsager() = (ownPlayerIndex == ansager())
    fun zuhoerer() = game.zuhoerer
    fun isZuhoerer() = (ownPlayerIndex == zuhoerer())
    fun aufspiel() = game.aufspiel
    fun hatAufspiel() = (ownPlayerIndex == aufspiel())
    fun reizwert(id: Int) = game.playerStats[id].gereizt
    fun ownReizwert() = reizwert(ownPlayerIndex)
    fun singleplayer() = game.singlePlayer
    fun isSingleplayer() = (singleplayer() == ownPlayerIndex)
    fun isOpposite(playerId: Int) = (isSingleplayer() == (singleplayer() == playerId))

    fun skatcards(): ArrayList<Int> {
        val skat = game.getSkat(ownPlayerIndex)
        if (skat != null) return Cardset.cardIdArrayList(skat)
        else {
            errno = permissionDenied
            return ArrayList<Int>(0)
        }
    }

    private fun skatcards(x: Int): ArrayList<Int> {
        //silent skatcards
        val skat = game.getSkat(ownPlayerIndex)
        if (skat != null) return Cardset.cardIdArrayList(skat)
        else {
            return ArrayList<Int>(0)
        }
    }

    fun karteAufHand(cardId: Int) = (handcards().contains(cardId))
    fun handcards() = Cardset.cardIdArrayList(game.playerStats[ownPlayerIndex].handkarten)
    fun stapelcards() = Cardset.cardIdArrayList(game.stapel)
    fun trumpf() = CardColour.colours.indexOf(game.trumpf)
    fun geforderteFarbe() = CardColour.colours.indexOf(game.geforderteFarbe)
    fun stichIndex() = game.stichIndex
    fun getStichCards(index: Int) = Cardset.cardIdArrayList(game.getCardsFromNthStich(index))
    fun getStichWinner(index: Int) = game.getWinnerFromNthStich(index)
    fun letzterStich() = getStichCards(stichIndex() - 1)
    fun getCardOwner(cardId: Int): Int {
        val ret: Int
        try {
            ret = game.getPlayedCardOwner(Cardset.std[cardId]!!)
        } catch (e: Exception) {
            e.printStackTrace()
            errno = invalidCard
            return -1
        }
        if (ret == -1) {
            errno = cardStillInGame
            return -1
        }
        return ret
    }

}