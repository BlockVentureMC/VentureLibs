package net.blockventuremc.modules.games

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendSuccess
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
@VentureCommand(
    name = "game",
    description = "Join games!",
    permission = "blockventure.game",
    permissionDefault = PermissionDefault.NOT_OP,
    usage = "/game",
    aliases = ["game", "games", "spiel"]
)
class GameCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false
        val player = sender


        player.sendSuccess("Game Test!")
        return true
    }

}