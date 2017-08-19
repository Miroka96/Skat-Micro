package game

import game.mode.*

enum class GameMode(val mode: AbstractMode, val id: Int) {
    NOT_STARTED(NotStartedMode(), 1),
    AUCTION(AuctionMode(), 2),
    DECLARING(DeclaringMode(), 3),
    SUIT_GAME(SuitGameMode(), 4),
    GRAND_GAME(GrandGameMode(), 5),
    NULL_GAME(NullGameMode(), 6),
    COUNTING(CountingMode(), 7),
    FINISHED(FinishedMode(), 8);

    override fun toString() = id.toString()
    fun toInt() = id

    companion object {
        private val map = GameMode.values().associateBy(GameMode::id)
        fun fromId(id: Int): GameMode {
            var mode = map[id]
            if (mode != null) {
                return mode
            } else {
                return NOT_STARTED
            }
        }
    }
}