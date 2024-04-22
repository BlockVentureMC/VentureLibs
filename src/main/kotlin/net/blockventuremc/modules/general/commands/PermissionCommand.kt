package net.blockventuremc.modules.general.commands

import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.consts.ADMIN_PERMISSIONS
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
        TODO("Not yet implemented")
    }

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
        TODO("Not yet implemented")
    }
}