package net.blockventuremc.modules.games

import org.bukkit.entity.Player

open class Game (val data: GameData) {

    var players = mutableListOf<Player>()

    open fun startGame() {}

}

data class GameData(val name: String, val description: String, val maxPlayers: Int, val minPlayers: Int)