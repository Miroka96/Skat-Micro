package model.game

abstract class AbstractMode protected constructor() {
    abstract fun dispatch(dispatcher: ModeDispatcher)
}

