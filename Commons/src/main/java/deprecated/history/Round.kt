package skat.history

import java.util.*

class Round(private val game: Game) {
    val draws = ArrayList<Draw>(3)
    var isFinished = false
        set(value) {
            if (!value) return
            field = true
            game.calculateWinner(this)
        }
    var winner = -1
    var roundValue = 0

    fun addDraw(d: Draw) {
        if (isFinished) return
        draws.add(d)
        //////////roundValue erhöhen
    }


}