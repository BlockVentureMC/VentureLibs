package net.blockventuremc.modules.general.commands.guests

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendTeleportSound
import net.blockventuremc.extensions.teleportAsyncWithPassengers
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@VentureCommand(
    name = "spawn",
    description = "Teleport to spawn",
    permissionDefault = PermissionDefault.TRUE,
    usage = "/spawn",
)
class SpawnCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        sender.teleportAsyncWithPassengers(sender.world.spawnLocation.add(0.5, 0.0, 0.5))
        sender.sendTeleportSound()
        return true
    }

}