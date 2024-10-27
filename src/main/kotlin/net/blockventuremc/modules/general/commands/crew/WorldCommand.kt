package net.blockventuremc.modules.general.commands.crew

import io.papermc.paper.entity.TeleportFlag
import net.blockventuremc.VentureLibs
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.translate
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.permissions.PermissionDefault


@VentureCommand(
    name = "world",
    description = "Teleport to a world",
    usage = "/world <world>",
    aliases = ["w"],
    permission = "blockventure.world",
    permissionDefault = PermissionDefault.OP
)
class WorldCommand : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessagePrefixed(sender.translate("commands.world.usage")?.message ?: "Usage: /world <world>")
            return true
        }

        val worldName = args[0]
        var world = Bukkit.getWorld(worldName)

        if (world == null) {
            world = Bukkit.createWorld(WorldCreator(worldName))
            sender.sendMessagePrefixed(
                sender.translate("commands.world.created", mapOf("world" to worldName))?.message
                    ?: "World $worldName created successfully"
            )
        }

        if (sender !is Player) {
            sender.sendMessagePrefixed("<red>Only players can use this command")
            return true
        }

        Bukkit.getScheduler().runTaskLater(VentureLibs.instance, Runnable {
            sender.teleportAsync(world!!.spawnLocation, PlayerTeleportEvent.TeleportCause.COMMAND, TeleportFlag.EntityState.RETAIN_PASSENGERS)
            sender.sendMessagePrefixed(
                sender.translate(
                    "commands.world.teleported",
                    mapOf("world" to worldName)
                )?.message ?: "Teleported to world $worldName"
            )
        }, 1L)

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): List<String> {
        return when (args?.size) {
            1 -> {
                val worlds = getAllBukkitWorlds()
                worlds.filter { it.startsWith(args[0]) }
            }

            else -> {
                emptyList()
            }
        }
    }

    private fun getAllBukkitWorlds(): List<String> {
        val worlds = mutableListOf<String>()
        for (world in Bukkit.getWorlds()) {
            worlds.add(world.name)
        }
        worlds.addAll(getAllFolderWorlds())
        return worlds.distinct()
    }

    private fun getAllFolderWorlds(): MutableList<String> {
        val worlds = mutableListOf<String>()
        val worldFolder = Bukkit.getWorldContainer()
        if (worldFolder.listFiles() == null) {
            return worlds
        }
        for (world in worldFolder.listFiles()!!) {
            if (world.isDirectory && world.listFiles()?.any { it.name == "level.dat" } == true) {
                worlds.add(world.name)
            }
        }
        return worlds
    }
}