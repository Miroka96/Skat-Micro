package model.cards

data class Card(val colour: CardColour, val value: CardValue)

enum class CardColour {
    CLUB,
    SPADE,
    HEART,
    DIAMOND,
}

enum class CardValue {
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING,
    ACE
}


