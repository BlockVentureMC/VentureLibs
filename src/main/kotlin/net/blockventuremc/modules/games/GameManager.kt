package net.blockventuremc.modules.games

import org.bukkit.entity.Player

object GameManager {

    val games = mutableListOf<Game>()

    fun initilizeGames() {
        games.add(JetskiGame())
    }

    fun getGame(player: Player): Game? {
        games.forEach { game ->
            if (game.isInGame(player)) {
                return game
            }
        }
        return null
    }

    fun inGame(player: Player): Boolean {
        return games.any { it.isInGame(player) }
    }

}