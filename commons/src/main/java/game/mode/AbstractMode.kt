package game.mode

import game.ModeDispatcher

abstract class AbstractMode protected constructor() {
    abstract fun dispatch(dispatcher: ModeDispatcher)
}

