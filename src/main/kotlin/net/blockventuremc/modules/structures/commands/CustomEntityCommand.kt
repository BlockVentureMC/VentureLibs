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
        val world = player.world

        val mass = args.getOrNull(0)?.toFloatOrNull() ?: 20.0f

        val pointA = Point(location.toVector())
        val pointB = Point(location.toVector().add(Vector(0.0f, -2.0f, 0.0f)))
        val stick = Stick(pointA, pointB)

        val pendulum = Pendulum(stick, mass, world)
        pendulum.spawn()

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


        interval(0, 1) {
            if(!player.isOnline || player.inventory.itemInMainHand.type == Material.DIAMOND_BLOCK) {
                pendulum.despawn()
                return@interval
            }
            pendulum.pendulum.point1.position = player.location.toVector()
            pendulum.simulate()

        }

        player.sendSuccess("Test Rope g=$gravity")
        return true
    }

}