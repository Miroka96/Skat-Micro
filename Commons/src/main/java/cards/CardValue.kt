package skat.cards

class CardValue private constructor(val countVal: Int) {
    companion object {
        val v7 = CardValue(0)
        val v8 = CardValue(0)
        val v9 = CardValue(0)
        val v10 = CardValue(10)
        val vb = CardValue(2)
        val vd = CardValue(3)
        val vk = CardValue(4)
        val va = CardValue(11)
        val values = arrayOf(v7, v8, v9, v10, vb, vd, vk, va)
    }
}