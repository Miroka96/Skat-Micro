package skat.mode

import skat.ModeDispatcher

abstract class AbstractMode protected constructor() {
    abstract fun dispatch(dispatcher: ModeDispatcher)
}

