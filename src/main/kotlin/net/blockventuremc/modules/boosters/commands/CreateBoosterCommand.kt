package net.blockventuremc.modules.boosters.commands

import dev.fruxz.ascend.extension.container.toUUID
import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.database.model.BitBoosters
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.modules.boosters.BoosterCategory
import net.blockventuremc.modules.boosters.BoosterManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.permissions.PermissionDefault
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours


@VentureCommand(
    name = "createbooster",
    description = "Create a booster",
    permissionDefault = PermissionDefault.OP,
    permission = "blockventure.boosters.create",
    usage = "/createbooster <player> [modifier] [duration] [userOnly]"
)
class CreateBoosterCommand : CommandExecutor, TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // duration, multiplier are optional
        if (args.isEmpty()) {
            sender.sendMessagePrefixed("Usage: /createbooster <player> [modifier] [duration] [userOnly]")
            return true
        }

        // check if uuid

        val name = args[0]

        val target = if (name.length > 24) {
            val uuid = name.toUUID()

            sender.server.getOfflinePlayer(uuid)
        } else {
            sender.server.getOfflinePlayer(name)
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
            owner = target.uniqueId,
            endTime = Calendar.now() + duration,
            modifier = mod,
            user = userOnly,
            category = BoosterCategory.CREW_ACTIVATED,
        )

        BoosterManager.addBooster(booster)

        sender.sendMessagePrefixed("Booster created for ${target.name} with a modifier of $mod, duration of $duration, and userOnly set to $userOnly.")

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
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