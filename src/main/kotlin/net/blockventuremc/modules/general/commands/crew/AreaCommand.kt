package net.blockventuremc.modules.general.commands.crew

import net.blockventuremc.VentureLibs
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.sendSuccessSound
import net.blockventuremc.modules.general.cache.AreaCache
import net.blockventuremc.modules.general.events.custom.getArea
import net.blockventuremc.modules.general.events.custom.toSimpleString
import net.blockventuremc.modules.general.events.custom.toVentureLocation
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import java.util.*

@VentureCommand(
    name = "area",
    description = "Create or modify areas",
    permission = "blockventure.area.build",
    permissionDefault = PermissionDefault.OP,
    usage = "/area",
)
class AreaCommand : CommandExecutor, TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (args.size < 2) {
            val area = sender.location.getArea()
            if (area != null) {
                sender.sendMessagePrefixed("You are in area ${area.name}")
            }

            sender.sendMessagePrefixed("Usage: /area <p1/p2> <area>")
            return true
        }

        val area = args.drop(1).joinToString(" ")
        val savableName = area.replace(" ", "_").lowercase(Locale.getDefault())

        if (args[0].equals("p1", true)) {
            val old = VentureLibs.instance.config.get("areas.$savableName.p1")
            VentureLibs.instance.config.set(
                "areas.$savableName.p1",
                sender.location.toVentureLocation().toSimpleString()
            )
            sender.sendMessagePrefixed(
                "Set first point to ${
                    sender.location.toVentureLocation().toSimpleString()
                } for area $area ${if (old != null) "(old: $old)" else ""}"
            )
        } else if (args[0].equals("p2", true)) {
            val old = VentureLibs.instance.config.get("areas.$savableName.p2")
            VentureLibs.instance.config.set(
                "areas.$savableName.p2",
                sender.location.toVentureLocation().toSimpleString()
            )
            sender.sendMessagePrefixed(
                "Set second point to ${
                    sender.location.toVentureLocation().toSimpleString()
                } for area $area ${if (old != null) "(old: $old)" else ""}"
            )
        } else {
            sender.sendMessagePrefixed("Usage: /area <p1/p2> <area>")
            return true
        }
        VentureLibs.instance.config.set("areas.$savableName.name", area)
        VentureLibs.instance.saveConfig()
        AreaCache.reloadAreas()
        sender.sendSuccessSound()

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("p1", "p2")
            2 -> VentureLibs.instance.config.getConfigurationSection("areas")?.getKeys(false)
                ?.map { VentureLibs.instance.config.getString("areas.$it.name") ?: "" }?.toList() ?: listOf()

            else -> listOf()
        }
    }
}