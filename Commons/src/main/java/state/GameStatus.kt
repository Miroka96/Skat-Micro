package skat.state

class GameStatus {
    companion object {
        val notStarted: GameStatus = GameStatus()
        val finished: GameStatus = GameStatus()
        val reizen: GameStatus = GameStatus()
        val opening: GameStatus = GameStatus()
        val spiel: GameStatus = GameStatus()
        val stateArray = arrayOf(notStarted, finished, reizen, opening, spiel)
    }
}