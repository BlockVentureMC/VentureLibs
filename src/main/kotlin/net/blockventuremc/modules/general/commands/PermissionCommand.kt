package net.blockventuremc.modules.general.commands

import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.consts.ADMIN_PERMISSIONS
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.permissions.PermissionDefault

@BlockCommand(
    name = "permission",
    description = "Change permissiosn",
    permission = ADMIN_PERMISSIONS,
    permissionDefault = PermissionDefault.OP,
    usage = "/permission <add/remove> <player|group> <permission>",
)
class PermissionCommand: CommandExecutor, TabExecutor {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {

        return when(args.size) {
            1 -> listOf("add", "remove")
            2 -> {
                val players = Bukkit.getOnlinePlayers().map { it.name }
                val groups = emptyList<String>()

                listOf(players, groups).flatten()
            }

            else -> emptyList()
        }

    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args.size < 3) {
            sender.sendMessage("Usage: ${command.usage}")
            return true
        }

        val action = args[0]
        val target = args[1]
        val permission = args[2]

        return true
    }
}