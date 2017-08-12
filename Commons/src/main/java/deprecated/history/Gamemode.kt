package skat.history

import model.cards.Card
import model.cards.Cardset

class Gamemode {
    val mode: Gamemode?
    var isSchwarz = false
    var isSchneider = false
    var isOuvert = false
    var isHand = false
    val cardorder: Array<Card?>?

    private constructor() {
        mode = null
        cardorder = null
    }

    constructor(m: Gamemode) {
        this.mode = m
        cardorder = arrayOfNulls(32)
        var i = 0
        when (m) {
            karo -> {
                for (c in 1..3) {
                    // 7 bis 9
                    for (v in 0..2) cardorder[i++] = Cardset.std[c * 8 + v]
                    // D,K
                    for (v in 5..6) cardorder[i++] = Cardset.std[c * 8 + v]
                    // 10
                    cardorder[i++] = Cardset.std[c * 8 + 3]
                    // A
                    cardorder[i++] = Cardset.std[c * 8 + 7]
                }
                // 7 bis 9
                for (v in 0..2) cardorder[i++] = Cardset.std[0 * 8 + v]
                // D,K
                for (v in 5..6) cardorder[i++] = Cardset.std[0 * 8 + v]
                // 10
                cardorder[i++] = Cardset.std[0 * 8 + 3]
                // A
                cardorder[i++] = Cardset.std[0 * 8 + 7]
                // Buben
                for (c in 0..3) cardorder[i++] = Cardset.std[c * 8 + 4]
            }
            herz -> {
                // 7 bis 9
                for (v in 0..2) cardorder[i++] = Cardset.std[0 * 8 + v]
                // D,K
                for (v in 5..6) cardorder[i++] = Cardset.std[0 * 8 + v]
                // 10
                cardorder[i++] = Cardset.std[0 * 8 + 3]
                // A
                cardorder[i++] = Cardset.std[0 * 8 + 7]
                for (c in 2..3) {
                    // 7 bis 9
                    for (v in 0..2) cardorder[i++] = Cardset.std[c * 8 + v]
                    // D,K
                    for (v in 5..6) cardorder[i++] = Cardset.std[c * 8 + v]
                    // 10
                    cardorder[i++] = Cardset.std[c * 8 + 3]
                    // A
                    cardorder[i++] = Cardset.std[c * 8 + 7]
                }
                // 7 bis 9
                for (v in 0..2) cardorder[i++] = Cardset.std[1 * 8 + v]
                // D,K
                for (v in 5..6) cardorder[i++] = Cardset.std[1 * 8 + v]
                // 10
                cardorder[i++] = Cardset.std[1 * 8 + 3]
                // A
                cardorder[i++] = Cardset.std[1 * 8 + 7]
                // Buben
                for (c in 0..3) cardorder[i++] = Cardset.std[c * 8 + 4]
            }
            pik -> {
                for (c in 0..1) {
                    // 7 bis 9
                    for (v in 0..2) cardorder[i++] = Cardset.std[c * 8 + v]
                    // D,K
                    for (v in 5..6) cardorder[i++] = Cardset.std[c * 8 + v]
                    // 10
                    cardorder[i++] = Cardset.std[c * 8 + 3]
                    // A
                    cardorder[i++] = Cardset.std[c * 8 + 7]
                }
                // 7 bis 9
                for (v in 0..2) cardorder[i++] = Cardset.std[3 * 8 + v]
                // D,K
                for (v in 5..6) cardorder[i++] = Cardset.std[3 * 8 + v]
                // 10
                cardorder[i++] = Cardset.std[3 * 8 + 3]
                // A
                cardorder[i++] = Cardset.std[3 * 8 + 7]

                // 7 bis 9
                for (v in 0..2) cardorder[i++] = Cardset.std[2 * 8 + v]
                // D,K
                for (v in 5..6) cardorder[i++] = Cardset.std[2 * 8 + v]
                // 10
                cardorder[i++] = Cardset.std[2 * 8 + 3]
                // A
                cardorder[i++] = Cardset.std[2 * 8 + 7]
                // Buben
                for (c in 0..3) cardorder[i++] = Cardset.std[c * 8 + 4]
            }
            kreuz, grand, ramsch -> {
                for (c in 0..3) {
                    // 7 bis 9
                    for (v in 0..2) cardorder[i++] = Cardset.std[c * 8 + v]
                    // D,K
                    for (v in 5..6) cardorder[i++] = Cardset.std[c * 8 + v]
                    // 10
                    cardorder[i++] = Cardset.std[c * 8 + 3]
                    // A
                    cardorder[i++] = Cardset.std[c * 8 + 7]
                }
                // Buben
                for (c in 0..3) cardorder[i++] = Cardset.std[c * 8 + 4]
            }
            nullspiel -> for (i in 0..31) cardorder[i] = Cardset.std[i]
        }

    }



    companion object {
        val kreuz: Gamemode = Gamemode()
        val pik: Gamemode = Gamemode()
        val herz: Gamemode = Gamemode()
        val karo: Gamemode = Gamemode()
        val nullspiel = Gamemode()
        val grand = Gamemode()
        val ramsch = Gamemode()
    }
}