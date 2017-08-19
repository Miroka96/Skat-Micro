package model.cards

import java.util.*

class CardsetGenerator {
    val SORTED_CARDS: Array<Card> by lazy {
        createCardsSorted()
    }

    fun createCardsSorted(): Array<Card> {
        return Array<Card>(32) { i ->
            // i = colours * 8 + values
            Card(CardColour.values()[i / 8], CardValue.values()[i % 8])
        }
    }

    fun createShuffledCards(cards: Array<Card>): Array<Card> {
        val rand = Random()
        val stack = cards.toMutableList()
        return Array<Card>(32) { _ ->
            stack.removeAt(rand.nextInt(stack.size))
        }
    }

    fun createCardsShuffled() = createShuffledCards(SORTED_CARDS)

    fun generateCardset(initialCardOrder: Array<Card>): Cardset {
        var hands = Array<Hand>(3) { player ->
            Hand(initialCardOrder.copyOfRange(player * 10, player * 10 + 9))
        }
        var skat = initialCardOrder.copyOfRange(30, 31)
        return Cardset(hands, skat)
    }

    fun generateShuffledCardset() = generateCardset(createCardsShuffled())
}