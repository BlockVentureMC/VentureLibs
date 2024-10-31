package net.blockventuremc.modules.general.commands.guests

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault


@VentureCommand(
    name = "money",
    description = "Check your balance",
    permission = "blockventure.balance",
    permissionDefault = PermissionDefault.TRUE,
    usage = "/money",
    aliases = ["bal", "balance", "venturebits", "vb"]
)
class MoneyCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false

        val player = sender
        val dbUser = player.toBlockUser()

        player.sendInfo(
            player.translate(
                "venturebits.self", mapOf(
                    "venturebits" to dbUser.ventureBits.toString(),
                    "bitsPerMinute" to player.bitsPerMinute.toString()
                )
            )?.message ?: ("You have ${dbUser.ventureBits} VentureBits.\n" +
                    "You currently earn ${player.bitsPerMinute} VentureBits per minute.")
        )
        return true
    }

}