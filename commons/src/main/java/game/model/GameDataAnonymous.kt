package game.model


interface IGameDataAnonymous : IGameDataInformation {
    var playercount: Int
}

data class GameDataAnonymous(override var id: Int) : IGameDataAnonymous {
    override var mode = 0
    override var playercount = 0
}