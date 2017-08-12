package skat.cards

class CardColour private constructor(val colourVal: Int) {
    companion object {
        val karo = CardColour(9)
        val herz = CardColour(10)
        val pik = CardColour(11)
        val kreuz = CardColour(12)
        val buben = CardColour(24)
        val colours = arrayOf(karo, herz, pik, kreuz, buben)


    }
}

