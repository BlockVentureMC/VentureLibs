package net.blockventuremc.modules.general.commands

import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.toDatabaseUser
import net.blockventuremc.extensions.translate
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import kotlin.time.Duration.Companion.milliseconds


@BlockCommand(
    name = "onlinetime",
    description = "Check your onlinetime",
    permission = "blockventure.onlinetime",
    permissionDefault = PermissionDefault.TRUE,
    usage = "/onlinetime",
    aliases = ["ot"]
)
class OnlinetimeCommand : CommandExecutor {


    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            val jreRunningSince = System.currentTimeMillis() - java.lang.management.ManagementFactory.getRuntimeMXBean().startTime
            val runTimeDuration = jreRunningSince.milliseconds
            getLogger().info("The server is running for $runTimeDuration")
            return true
        }

        val player = sender
        val onlinetime = player.toDatabaseUser().onlineTime

        player.sendMessagePrefixed(player.translate("onlinetime", mapOf("onlinetime" to onlinetime.toString()))?.message ?: "You have been online for $onlinetime")
        return true
    }

}