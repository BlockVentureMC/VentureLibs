package net.blockventuremc.modules.general.commands.crew

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.hasBuildTag
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.toBlockUser
import net.blockventuremc.extensions.translate
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault


@VentureCommand(
    name = "build",
    description = "Change your build mode",
    permission = "blockventure.build",
    permissionDefault = PermissionDefault.OP,
    usage = "/build",
)
class BuildCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        sender.hasBuildTag = !sender.hasBuildTag

        sender.sendMessagePrefixed(
            sender.toBlockUser().translate(
                "build_mode_toggled",
                mapOf("enabled" to (if (sender.hasBuildTag) "enabled" else "disabled"))
            )
                ?.message ?: "Build mode is now ${if (sender.hasBuildTag) "enabled" else "disabled"}"
        )

        return true
    }
}