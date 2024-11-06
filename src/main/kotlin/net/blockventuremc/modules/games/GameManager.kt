package net.blockventuremc.modules.games

import net.blockventuremc.modules.structures.interval
import org.bukkit.entity.Player

object GameManager {

    val games = mutableListOf<Game>()

    fun initilizeGames() {
        games.add(JetskiGame())

        interval(0, 1) {
            games.forEach { game ->
                game.tick()
            }
        }
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