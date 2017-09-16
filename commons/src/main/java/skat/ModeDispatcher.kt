package skat

import skat.mode.*

interface ModeDispatcher {
    fun reactOnMode(mode: AuctionMode)
    fun reactOnMode(mode: CountingMode)
    fun reactOnMode(mode: DeclaringMode)
    fun reactOnMode(mode: FinishedMode)
    fun reactOnMode(mode: GrandGameMode)
    fun reactOnMode(mode: NotStartedMode)
    fun reactOnMode(mode: NullGameMode)
    fun reactOnMode(mode: SuitGameMode)
}