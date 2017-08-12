package skat.cards

class Card(c: CardColour, v: CardValue) {
    val colour = c
    val value = v
    val colourVal = colour.colourVal
    val countVal = value.countVal
}

