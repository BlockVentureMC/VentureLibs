package net.blockventuremc.modules.rides.flat

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.modules.structures.Animation
import net.blockventuremc.modules.structures.Attachment
import net.blockventuremc.modules.structures.ItemAttachment
import net.blockventuremc.modules.structures.RootAttachment
import net.blockventuremc.modules.structures.StructureManager
import net.blockventuremc.modules.structures.impl.Seat
import net.blockventuremc.utils.itembuilder.ItemBuilder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

@VentureCommand(
    name = "flatride",
    description = "Create a flatride",
    permission = "blockventure.build",
    permissionDefault = PermissionDefault.OP,
    usage = "/flatride <ride>",
)
class FlatRideCommand : CommandExecutor, TabExecutor {

    var speed = 1.5
    var tiltAngle = 42.0
    var currentTiltAngle = 0.0

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

       if (args.isEmpty()) {
            sender.sendMessagePrefixed("Usage: /flatride <ride>")
            return true
        }

        val ride = args[0]

        if (ride == "pinball") {
            generatePinball(sender.location)
            return true
        }

        if (ride == "speed") {
            if (args.size < 2) {
                sender.sendMessagePrefixed("Usage: /flatride speed <speed>")
                return true
            }
            val newSpeed = args[1].toDouble()
            speed = newSpeed
            sender.sendMessagePrefixed("Speed set to $newSpeed")
            return true
        }

        if (ride == "tilt") {
            if (args.size < 2) {
                sender.sendMessagePrefixed("Usage: /flatride tilt <tiltAngle>")
                return true
            }
            val newTiltAngle = args[1].toDouble()
            tiltAngle = newTiltAngle
            sender.sendMessagePrefixed("Tilt Angle set to $newTiltAngle")
            return true
        }

        sender.sendMessagePrefixed("FlatRide initialized")
        return true
    }

    private fun generatePinball(location: Location) {
        val rootAttachment = RootAttachment("flat", location.toVector(), Vector())
        rootAttachment.world = location.world


        // Add tilt shift attachment
        val tiltShift = Attachment("tilt_shift", Vector(), Vector())
        rootAttachment.addChild(tiltShift)


        // Add rotator attachment
        val rotator = Attachment("rotator", Vector(0.0, 0.0, 10.0), Vector())
        tiltShift.addChild(rotator)

        // Generate the carts

        val radius = 5.0
        var totalCarts = 5
        for (n in 0..totalCarts) {
            val angle = 2 * Math.PI * n / totalCarts
            val x = cos(angle) * radius
            val z = sin(angle) * radius

            generateCart(rotator, n, Vector(x, 0.0, z), angle)
        }

        rootAttachment.animation = object : Animation() {
            var time = 0.0
            override fun animate() {
                time++

                rotator.localRotation.add(Vector(0.0, speed, 0.0))

                rotator.children.forEach { name, child ->
                    child.localRotation.add(Vector(0.0, speed * 2, 0.0))
                }

                if (tiltAngle == currentTiltAngle) {
                    return
                }

                if (tiltAngle > currentTiltAngle) {
                    currentTiltAngle += 0.1
                } else {
                    currentTiltAngle -= 0.1
                }

                val armRotation = sin(time * 0.05) * currentTiltAngle - currentTiltAngle
                tiltShift.localRotation = Vector(armRotation, 0.0, 0.0)
            }
        }

        rootAttachment.initialize()
        StructureManager.structures[rootAttachment.uuid] = rootAttachment
    }

    private fun generateCart(rotator: Attachment, n: Int, offset: Vector, angle: Double) {
        val cartRotator = Attachment("rotator$n", offset, Vector(0.0, Math.toDegrees(angle), 0.0))
        rotator.addChild(cartRotator)

        cartRotator.addChild(Seat("seat1", Vector(0.39, 0.6, 0.3), Vector()))
        cartRotator.addChild(Seat("seat2", Vector(-0.39, 0.6, 0.3), Vector()))
        cartRotator.addChild(Seat("seat3", Vector(0.39, 0.6, -0.3), Vector()))
        cartRotator.addChild(Seat("seat4", Vector(-0.39, 0.6, -0.3), Vector()))

        cartRotator.addChild(
            ItemAttachment(
                "model",
                ItemBuilder(Material.DIAMOND_SWORD).customModelData(99).build(),
                Vector(0.0, 1.0, 0.0),
                Vector()
            )
        )
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("pinball", "speed", "tilt").filter { it.startsWith(args[0]) }
            else -> emptyList()
        }
    }

}