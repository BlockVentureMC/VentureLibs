package net.blockventuremc.modules.general.commands

import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.consts.*
import net.blockventuremc.extensions.*
import org.bukkit.command.CommandExecutor
import org.bukkit.permissions.PermissionDefault
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


@BlockCommand(
    name = "build",
    description = "Change your build mode",
    permission = BUILD_PERMISSIONS,
    permissionDefault = PermissionDefault.OP,
    usage = "/build",
)
class BuildCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        sender.hasBuildTag = !sender.hasBuildTag

        sender.sendMessagePrefixed("Build mode is now ${if (sender.hasBuildTag) "enabled" else "disabled"}")

        return true
    }
}