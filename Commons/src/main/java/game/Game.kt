package game

import cards.CardsetGenerator
import game.model.GameData

class Game(var gameData: GameData) {

    constructor() : this(GameData(CardsetGenerator().generateShuffledCardset()))


    var gameMode: GameMode = GameMode.NOT_STARTED


}