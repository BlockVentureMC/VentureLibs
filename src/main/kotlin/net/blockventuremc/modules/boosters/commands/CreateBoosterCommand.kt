package net.blockventuremc.modules.boosters.commands

import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.database.model.BitBoosters
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.modules.boosters.BoosterCategory
import net.blockventuremc.modules.boosters.BoosterManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours


@BlockCommand(
    name = "createbooster",
    description = "Create a booster",
    permissionDefault = PermissionDefault.OP,
    permission = "blockventure.boosters.create",
    usage = "/createbooster <player> [modifier] [duration] [userOnly]"
)
class CreateBoosterCommand: CommandExecutor, TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender as? Player ?: run {
            sender.sendMessagePrefixed("This command is only available to players.")
            return true
        }
        // duration, multiplier are optional
        if (args.isEmpty()) {
            sender.sendMessagePrefixed("Usage: /createbooster <player> [modifier] [duration] [userOnly]")
            return true
        }

        val target = player.server.getPlayerExact(args[0])
        if (target == null) {
            sender.sendMessagePrefixed("Player not found.")
            return true
        }

        val mod: Long = args.getOrNull(1)?.toLongOrNull() ?: 1

        var duration = 2.hours // default duration

        if (args.size > 2) {
            val durationString = args[2]
            duration = Duration.parseOrNull(durationString) ?: run {
                sender.sendMessagePrefixed("Invalid duration.")
                return true
            }
        }

        val userOnly = args.getOrNull(3)?.toBoolean() ?: false

        val booster = BitBoosters(
            player.uniqueId,
            endTime = Calendar.now() + duration,
            modifier = mod,
            user = userOnly,
            category = BoosterCategory.CREW_ACTIVATED,
        )

        BoosterManager.addBooster(booster)

        sender.sendMessagePrefixed("Booster created for ${target.name} with a modifier of $mod, duration of $duration, and userOnly set to $userOnly.")

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        // return users, true, false
        return when (args.size) {
            1 -> sender.server.onlinePlayers.map { it.name }
            2 -> listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
            3 -> listOf("2h", "1d", "10m", "10s")
            4 -> listOf("true", "false")
            else -> emptyList()
        }
    }
}