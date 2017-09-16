package skat.action

import skat.ModeDispatcher
import skat.UnallowedModeException
import skat.mode.*

abstract class AbstractAction : ModeDispatcher {
    override fun reactOnMode(mode: AuctionMode) {
        // Override if needed
        throw UnallowedModeException("Auction Mode")
    }

    override fun reactOnMode(mode: CountingMode) {
        // Override if needed
        throw UnallowedModeException("Counting Mode")
    }

    override fun reactOnMode(mode: DeclaringMode) {
        // Override if needed
        throw UnallowedModeException("Declaring Mode")
    }

    override fun reactOnMode(mode: FinishedMode) {
        // Override if needed
        throw UnallowedModeException("Finished Mode")
    }

    override fun reactOnMode(mode: GrandGameMode) {
        // Override if needed
        throw UnallowedModeException("Grand Game Mode")
    }

    override fun reactOnMode(mode: NotStartedMode) {
        // Override if needed
        throw UnallowedModeException("Game-not-started Mode")
    }

    override fun reactOnMode(mode: NullGameMode) {
        // Override if needed
        throw UnallowedModeException("Null Game Mode")
    }

    override fun reactOnMode(mode: SuitGameMode) {
        // Override if needed
        throw UnallowedModeException("Suit Game Mode")
    }

    fun doIt(mode: AbstractMode): Boolean {
        var success = false
        try {
            mode.dispatch(this)
            success = true
        } catch (ex: UnallowedModeException) {
            ex.printStackTrace()
        }
        return success
    }
}