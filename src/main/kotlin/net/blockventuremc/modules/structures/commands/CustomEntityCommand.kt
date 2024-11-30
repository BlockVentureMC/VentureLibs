package net.blockventuremc.modules.structures.commands

import dev.fruxz.ascend.extension.container.second
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
import kotlin.math.PI

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
        val world = player.world

        val length = args.getOrNull(0)?.toDoubleOrNull() ?: 100.0
        val mass = args.getOrNull(1)?.toDoubleOrNull() ?: 1.0

/*
        for (i in 0..20) {
            val pointPosition = location.toVector().add(Vector(0.0f, -0.6f, 0.0f).multiply(i))

            val point = Point(pointPosition)

            if(i == 0) point.locked = true

            rope.points.add(point)
        }

        for (index in 0.. rope.points.size - 2) {
            rope.sticks.add(Stick(rope.points[index], rope.points[index + 1]))
        }

 */

        val pendulum = SimplePendulum(player.world, player.location.toVector(), length, mass, PI / 4)
        pendulum.spawn()
        interval(0, 1) {
            pendulum.origin = player.eyeLocation.toVector().add(player.location.direction.multiply(7))
            pendulum.update()
        }

        player.sendSuccess("Test Rope length=$length mass=$mass")
        return true
    }

}