package net.blockventuremc.modules.general.commands.club

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.translate
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@VentureCommand(
    name = "personaltime",
    description = "Toggle personal time",
    permission = "blockventure.personaltime",
    permissionDefault = PermissionDefault.OP,
    usage = "/personaltime [reset|noon|midnight|day|night|<0-24000>]",
    aliases = ["pt", "ptime"]
)
class PersonalTimeCommand : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (args.isEmpty()) {
            sender.sendMessagePrefixed(
                sender.translate(
                    "commands.personaltime.current",
                    mapOf("time" to sender.playerTime)
                )?.message ?: "Personal current time: <color:#feca57>${sender.playerTime}</color>. Use <color:#c8d6e5>/personaltime <reset|noon|midnight|day|night|<0-24000>></color> to change it."
            )
            return true
        }

        val time = when (args[0]) {
            "reset" -> -1L
            "noon" -> 6000L
            "midnight" -> 18000L
            "day" -> 1000L
            "night" -> 13000L
            else -> try {
                args[0].toLong()
            } catch (e: NumberFormatException) {
                sender.sendMessagePrefixed(
                    sender.translate(
                        "commands.personaltime.invalid",
                        mapOf()
                    )?.message ?: "Invalid time. Use <color:#c8d6e5>/personaltime <reset|noon|midnight|day|night|<0-24000>></color> to change it."
                )
                return true
            }
        }

        if (time !in 0..24000) {
            sender.resetPlayerTime()
            sender.sendMessagePrefixed(
                sender.translate(
                    "commands.personaltime.reset",
                    mapOf("time" to sender.world.time)
                )?.message ?: "Personal time reset to <color:#feca57>${sender.world.time}</color>."
            )
            return true
        }

        sender.setPlayerTime(time, false)
        sender.sendMessagePrefixed(
            sender.translate(
                "commands.personaltime.set",
                mapOf("time" to time)
            )?.message ?: "Personal time set to <color:#feca57>$time</color>."
        )
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("reset", "noon", "midnight", "day", "night").filter { it.startsWith(args[0]) }
            else -> emptyList()
        }
    }
}