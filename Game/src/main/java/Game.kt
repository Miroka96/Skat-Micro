package skat

import model.cards.CardsetGenerator
import model.game.GameMode

class Game(var gameData: GameData) {

    constructor() : this(GameData(CardsetGenerator().generateShuffledCardset()))


    var gameMode: GameMode = GameMode.NOT_STARTED


}