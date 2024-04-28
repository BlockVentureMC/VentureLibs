package net.blockventuremc.modules.boosters.commands

import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.modules.boosters.gui.BoosterGUI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@BlockCommand(
    name = "boosters",
    description = "List all boosters",
    permissionDefault = PermissionDefault.TRUE,
)
class BoosterCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender as? Player ?: run {
            sender.sendMessagePrefixed("This command is only available to players.")
            return true
        }

        BoosterGUI.openBoosterGUI(player)

        return true
    }
}