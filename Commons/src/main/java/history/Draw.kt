package skat.history

import skat.cards.Card

class Draw {
    val type: Draw?
    val reizwert: Int
    val gamemode: Gamemode?
    val karte: Card?
    val karte2: Card?
    val playerId: Int

    private constructor() {
        type = this
        reizwert = 0
        gamemode = null
        karte = null
        karte2 = null
        playerId = -1
    }

    constructor(p: Int, v: Int) {
        reizwert = v
        if (v < 18)
            type = wegSagen
        else
            type = reizen
        gamemode = null
        karte = null
        karte2 = null
        playerId = p
    }

    constructor(p: Int, m: Gamemode) {
        reizwert = 0
        type = opening
        gamemode = m
        karte = null
        karte2 = null
        playerId = p
    }

    constructor(p: Int, c: Card) {
        reizwert = 0
        type = karteLegen
        gamemode = null
        karte = c
        karte2 = null
        playerId = p
    }

    constructor(p: Int, c1: Card, c2: Card) {
        reizwert = 0
        type = kartenTauschen
        gamemode = null
        karte = c1
        karte2 = c2
        playerId = p
    }

    fun getTypeIndex(d: Draw): Int {
        for (i in statesArray.indices) {
            if (statesArray[i] === d) return i
        }
        return -1
    }

    companion object {
        val reizen: Draw = Draw()
        val wegSagen: Draw = Draw()
        val opening: Draw = Draw()
        val karteLegen: Draw = Draw()
        val kartenTauschen: Draw = Draw()
        val statesArray = arrayOf(reizen, wegSagen, opening, karteLegen, kartenTauschen)
    }
}