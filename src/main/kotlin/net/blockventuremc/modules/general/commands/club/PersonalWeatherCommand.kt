package net.blockventuremc.modules.general.commands.club

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.translate
import org.bukkit.WeatherType
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@VentureCommand(
    name = "personaweather",
    description = "Toggle personal weather",
    permission = "blockventure.personaweather",
    permissionDefault = PermissionDefault.OP,
    usage = "/personaweather [reset|clear|rain]",
    aliases = ["pw", "pweather"]
)
class PersonalWeatherCommand : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (args.isEmpty()) {
            sender.sendMessagePrefixed(
                sender.translate(
                    "commands.personaweather.current",
                    mapOf("weather" to (sender.playerWeather?.name ?: "None"))
                )?.message
                    ?: "Personal current weather: <color:#feca57>${sender.playerWeather?.name ?: "None"}</color>. Use <color:#c8d6e5>/personaweather <reset|clear|rain></color> to change it."
            )
            return true
        }

        if (args[0] == "reset") {
            sender.resetPlayerWeather()
            sender.sendMessagePrefixed(
                sender.translate(
                    "commands.personaweather.reset",
                    mapOf()
                )?.message ?: "Personal weather reset."
            )
            return true
        }

        val weather = when (args[0]) {
            "clear" -> WeatherType.CLEAR
            "rain" -> WeatherType.DOWNFALL
            else -> {
                sender.sendMessagePrefixed(
                    sender.translate(
                        "commands.personaweather.invalid",
                        mapOf()
                    )?.message
                        ?: "Invalid weather. Use <color:#c8d6e5>/personalweather <reset|clear|rain></color> to change it."
                )
                return true
            }
        }

        sender.setPlayerWeather(weather)

        sender.sendMessagePrefixed(
            sender.translate(
                "commands.personaweather.set",
                mapOf("weather" to weather.name)
            )?.message ?: "Personal weather set to <color:#feca57>${weather.name}</color>."
        )

        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("reset", "clear", "rain").filter { it.startsWith(args[0]) }
            else -> emptyList()
        }
    }
}