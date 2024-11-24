package net.blockventuremc.modules.rides.flat

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.lerp
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.modules.structures.Animation
import net.blockventuremc.modules.structures.Attachment
import net.blockventuremc.modules.structures.ItemAttachment
import net.blockventuremc.modules.structures.RootAttachment
import net.blockventuremc.modules.structures.StructureManager
import net.blockventuremc.modules.structures.commands.StructureCommand
import net.blockventuremc.modules.structures.commands.selectedStructure
import net.blockventuremc.modules.structures.deltaTime
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

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

       if (args.isEmpty()) {
            sender.sendMessagePrefixed("Usage: /flatride <ride>")
            return true
        }

        val ride = args[0]

        if (ride == "pinball") {
            generatePinball(sender.location)
            sender.sendMessagePrefixed("FlatRide initialized")
            return true
        }
        return true
    }

    private fun generatePinball(location: Location) {
        val rootAttachment = RootAttachment("flat", location.toVector(), Vector())
        rootAttachment.world = location.world

        // Add tilt shift attachment
        val tiltShift = Attachment("tilt_shift", Vector(), Vector())
        tiltShift.animate = true
        rootAttachment.addChild(tiltShift)

        // Add rotator attachment
        val rotator = Attachment("rotator", Vector(0.0, 0.0, 10.0), Vector())
        rotator.animate = true
        tiltShift.addChild(rotator)
        rotator.addChild(ItemAttachment("modell", ItemBuilder(Material.DIAMOND_SWORD).customModelData(70).build(), Vector(0.0, 0.0, 0.0), Vector()).setScale(1.1f))

        val cartRotator = Attachment("cartrotator", Vector(0.0, 0.0, 0.0), Vector())
        rotator.addChild(cartRotator)
        // Generate the carts

        val radius = 5.0
        var totalCarts = 5
        for (n in 0..totalCarts) {
            val angle = 2 * Math.PI * n / totalCarts
            val x = cos(angle) * radius
            val z = sin(angle) * radius

            generateCart(cartRotator, n, Vector(x, 0.0, z), angle)
        }

        rootAttachment.animation = object : Animation() {
            var time = 0.0
            var armRotation = 0.0f

            init {
                animationMap["speed"] = 1.5f
                animationMap["tiltangle"] = 42.0f
                animationMap["cartspeed"] = 1.5f
                animationMap["armspeed"] = 0.2f
            }
            override fun animate() {
                time++

                val speed = animationMap["speed"] as Float
                val tiltAngleTarget = animationMap["tiltangle"] as Float
                val cartspeed = animationMap["cartspeed"] as Float
                val armSpeed = animationMap["armspeed"] as Float

                rotator.localRotation.add(Vector(0.0f, speed, 0.0f))

                cartRotator.children.forEach { name, child ->
                    child.localRotation.add(Vector(0.0f, cartspeed, 0.0f))
                }

                armRotation = lerp(armRotation, tiltAngleTarget, deltaTime, armSpeed)

                //val armRotation = (sin(time * 0.05) * tiltAngle) + (tiltAngle * 0.5)
                tiltShift.localRotation = Vector(armRotation, 0.0f, 0.0f)
            }
        }

        selectedStructure = rootAttachment
        rootAttachment.initialize()
        StructureManager.structures[rootAttachment.uuid] = rootAttachment
    }

    private fun generateCart(rotator: Attachment, n: Int, offset: Vector, angle: Double) {
        val cartRotator = Attachment("rotator$n", offset, Vector(0.0, Math.toDegrees(angle), 0.0))
        cartRotator.animate = true
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
            1 -> listOf("pinball").filter { it.startsWith(args[0]) }
            else -> emptyList()
        }
    }

}