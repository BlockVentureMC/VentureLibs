package net.blockventuremc.modules.general.commands.guests

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.toBlockUser
import net.blockventuremc.extensions.translate
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.milliseconds


@VentureCommand(
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
            val jreRunningSince = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().startTime
            val runTimeDuration = jreRunningSince.milliseconds
            getLogger().info("The server is running for $runTimeDuration")
            return true
        }

        val player = sender
        val onlinetime = player.toBlockUser().onlineTime

        player.sendMessagePrefixed(
            player.translate("onlinetime", mapOf("onlinetime" to onlinetime.toString()))?.message
                ?: "You have been online for $onlinetime"
        )
        return true
    }

}