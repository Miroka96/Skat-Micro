package skat.history

import java.util.*

class GameHistory(val game: Game) {
    val rounds = ArrayList<Round>(15)
    var firstStichIndex = -1

    fun addDraw(d: Draw) {
        if (latestRound == null) addRound()
        latestRound!!.addDraw(d)
        if (d.type == Draw.wegSagen) {
            addRound()
        } else if (firstStichIndex == -1 &&
                d.type == Draw.karteLegen) {
            firstStichIndex = rounds.size - 1
        }

    }

    fun getNthStich(n: Int): Round? {
        if (firstStichIndex != -1) {
            try {
                return rounds[firstStichIndex + n]
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    val latestRound: Round?
        get() {
            val l = rounds.size
            if (l == 0) return null
            return rounds[l - 1]
        }

    fun getNthLatestRound(n: Int): Round? {
        try {
            return rounds[rounds.size - 1 - n]
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun addRound() {
        latestRound!!.isFinished = true
        rounds.add(Round(game))
    }
}