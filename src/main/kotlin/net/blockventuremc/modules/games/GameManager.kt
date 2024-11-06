package net.blockventuremc.modules.games

object GameManager {

    val games = mutableListOf<Game>()

    fun initilizeGames() {
        games.add(JetskiGame())
    }

}