package skat

import skat.cards.Cardset
import java.util.*

/*Multithreading und Reflection sind für die KIs verboten
*
* Author=Mirko Krause
* Mail=mirko.krause@student.hpi.de
* Januar 2016
*/
object Skat {
    private var started = false
    //ggfs mal in einer Datenklasse zusammenfassen
    private val games = ArrayList<Game>()
    private val masterKeys = ArrayList<Int>()
    private val valid = ArrayList<Boolean>()
    fun main(args: Array<String>?) {
        if (started)
            return
        started = true
    }

    private fun runGame(n: Int) {
        val g = games[n]
        g.startGame()
        games.removeAt(n)
    }

    fun generateKey() = Cardset.randInt(Integer.MAX_VALUE - 1)

    private val latestGame: Game?
        get() {
            return games[games.size - 1]
        }

    private fun remLatestGame() {
        val i = games.size - 1
        games.removeAt(i)
        masterKeys.remove(i)
        valid.removeAt(i)
    }
}