package net.blockventuremc.modules.games

import net.blockventuremc.extensions.sendError
import net.blockventuremc.extensions.sendSuccess
import org.bukkit.entity.Player

open class Game (val data: GameData) {

    var state = GameState.WAITING

    var players = mutableListOf<Player>()

    fun isInGame(player: Player): Boolean {
        return players.contains(player)
    }

    open fun join(player: Player) {
        if (players.contains(player)) {
            player.sendError("You are already in this game!")
            return
        }
        if (state != GameState.WAITING || state != GameState.ENDED) {
            player.sendError("Game is already started!")
            return
        }
        players.add(player)
        player.sendSuccess("You joined the ${data.name} game!")

        checkEnoughPlayers()
    }

    open fun leave(player: Player) {
        if (!players.contains(player)) {
            player.sendError("You are not in this game!")
            return
        }
        players.remove(player)
        player.sendSuccess("You left the ${data.name} game!")

        checkEnoughPlayers()
    }

    fun checkEnoughPlayers() {
        if (players.size < data.minPlayers) {
            state = GameState.WAITING
            players.forEach { it.sendError("Not enough players!") }
            return
        }
        if (state == GameState.WAITING) {
            start()
        }
    }

    open fun start() {
        state = GameState.STARTING
        players.forEach { it.sendError("Game is starting!") }
    }

}

enum class GameState {
    WAITING,
    STARTING,
    RUNNING,
    ENDED
}

data class GameData(val name: String, val description: String, val maxPlayers: Int, val minPlayers: Int)