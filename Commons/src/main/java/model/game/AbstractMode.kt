package model.game

import model.game.mode.*

enum class GameMode(val mode: AbstractMode) {
    NOT_STARTED(NotStartedMode()),
    AUCTION(AuctionMode()),
    DECLARING(DeclaringMode()),
    SUIT_GAME(SuitGameMode()),
    GRAND_GAME(GrandGameMode()),
    NULL_GAME(NullGameMode()),
    COUNTING(CountingMode()),
    FINISHED(FinishedMode())
}

abstract class AbstractMode protected constructor() {

}