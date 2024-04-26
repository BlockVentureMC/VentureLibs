package net.blockventuremc.modules.general.commands.crew

import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.database.model.DatabaseUser
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.modules.general.model.Ranks
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@BlockCommand(
    name = "test",
    description = "Test command",
    permissionDefault = PermissionDefault.FALSE,
)
class TestCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        val u = DatabaseUser(sender.uniqueId, sender.name)

        if (!u.rank.isHigherOrEqual(Ranks.Crew)) {
            sender.sendMessagePrefixed("You do not have permission to use this command.")
            return true
        }
        sender.sendMessage("Test command")
        return true
    }
}