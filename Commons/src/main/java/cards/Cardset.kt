package skat.cards

import java.util.*

class Cardset(c: Array<Card?>) {
    private val playerCards: Array<Array<Card>>
    val originalSkat: Array<Card>
    var skat: ArrayList<Card>

    init {
        playerCards = arrayOf<Array<Card>>(Arrays.copyOfRange(c, 0, 9), Arrays.copyOfRange(c, 10, 19), Arrays.copyOfRange(c, 20, 29))
        originalSkat = Arrays.copyOfRange(c, 30, 31)
        skat = cardArrayList(originalSkat)
    }

    fun getPlayerCards(player: Int) = cardArrayList(playerCards[player] as Array<Card?>)

    companion object {
        var std = stdCardArray()
        fun stdCardArray(): Array<Card?> {
            val cards = arrayOfNulls<Card>(32)
            for (i in 0..31)
            //i = colours * 8 + values
                cards[i] = Card(CardColour.colours[i / 8], CardValue.values[i % 8])
            return cards
        }

        fun randInt(max: Int): Int {
            // Usually this can be a field rather than a method variable
            val rand = Random()
            // nextInt is normally exclusive of the top value,
            // so add 1 to make it inclusive
            val randomNum = rand.nextInt(max + 1)
            return randomNum
        }

        fun cardArrayList(ca: Array<Card?>): ArrayList<Card> {
            val set = ArrayList<Card>(ca.size)
            for (i in ca.indices)
                set.add(ca[i]!!)
            return set
        }

        fun cardIdArrayList(cal: ArrayList<Card>): ArrayList<Int> {
            val ret = ArrayList<Int>(10)
            for (i in cal.indices) {
                var found = false
                var k = 0
                while (!found) {
                    if (cal[i] == std[k]) {
                        found = true
                        ret.add(k)
                    }
                }
            }
            return ret
        }

        fun randomCardArray(): Array<Card?> {
            val set = cardArrayList(std)
            val cards = arrayOfNulls<Card>(32)
            for (i in 31 downTo 0) {
                val r = randInt(i)
                cards[i] = set[r]
                set.removeAt(r)
            }
            return cards
        }
    }
}