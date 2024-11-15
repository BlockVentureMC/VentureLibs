package net.blockventuremc.modules.structures.commands

import net.blockventuremc.VentureLibs
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendSuccess
import net.blockventuremc.modules.structures.*
import net.blockventuremc.modules.structures.vehicle.PacketHandler.setEntityHitbox
import net.blockventuremc.utils.itembuilder.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import org.bukkit.util.Vector

@VentureCommand(
    name = "customentity",
    description = "Spawn Custom Entities!",
    permission = "blockventure.customentity",
    permissionDefault = PermissionDefault.TRUE,
    usage = "/customentity",
    aliases = ["ce", "customentity", "custom"]
)
class CustomEntityCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false
        val player = sender

        val location = player.location
        var armorStand = location.world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand
        val width = args[0].toDouble()
        val height = args[1].toDouble()
        Bukkit.getScheduler().runTaskLater(VentureLibs.instance, Runnable {
            setEntityHitbox(armorStand, width, height)
        },2L)

        player.sendSuccess("Test Armorstand w=$width h=$height")
        return true
    }

}