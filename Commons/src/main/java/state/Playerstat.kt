package skat.state

import skat.cards.Card
import java.util.*

class Playerstat(val handkarten: ArrayList<Card>) {
    private var points = 0
    var gereizt = 0

    fun addPoints(p: Int) {
        this.points += p
    }

    fun getPoints() = this.points
}