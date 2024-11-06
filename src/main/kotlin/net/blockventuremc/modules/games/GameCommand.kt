package net.blockventuremc.modules.games

import net.blockventuremc.annotations.VentureCommand
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
        if(GameManager.inGame(player)) {
            GameManager.games.firstOrNull()?.leave(player)
            return true
        }
        GameManager.games.firstOrNull()?.join(player)
        return true
    }

}