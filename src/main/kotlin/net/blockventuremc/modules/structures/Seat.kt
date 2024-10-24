package net.blockventuremc.modules.structures

import io.papermc.paper.entity.TeleportFlag
import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.toEulerAngles
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class Seat(name: String, localPosition: Vector, localRotation: Vector) : Attachment(name, localPosition, localRotation) {

    var itemDisplay: ItemDisplay? = null
    var interaction: Interaction? = null
    var dynamic = false

    val offset = 0.53

    init {
        localPosition.add(Vector(0.0, offset, 0.0))
    }

    override fun spawn() {
        val location = bukkitLocation
        itemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
        itemDisplay?.apply {
            shadowStrength = 0.0f
            teleportDuration = 3
            interpolationDuration = 3
            itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD
            setItemStack(ItemStack(Material.ACACIA_WOOD))
            isCustomNameVisible = false
            customName = "seat"
            var transform = transformation
            transform.scale.mul(0.03f)
            transformation = transform
        }

        interaction = location.world.spawnEntity(location, EntityType.INTERACTION) as Interaction
        interaction?.apply {
            interactionHeight = 1.0f
            interactionWidth = 0.45f
            isCustomNameVisible = false
            itemDisplay?.addPassenger(this)
        }
    }

    override fun updateTransform() {
        itemDisplay?.teleport(bukkitLocation.add(Vector(0.0, -offset, 0.0)), TeleportFlag.EntityState.RETAIN_PASSENGERS)

        //forward Vector (direction where the player should be looking)
        //itemDisplay?.passegner[1]-> player

        val player = itemDisplay?.passengers?.firstOrNull() as? Player
        if(player == null) return

        // The forward vector is a vector pointing from the seat to a point 1m in front of you
        val transformation = itemDisplay?.transformation ?: return

        // Extract the rotation quaternion from the transformation
        val rotationQuaternion = transformation.leftRotation

        // Convert the quaternion to Euler angles (yaw and pitch)
        val eulerAngles = rotationQuaternion.toEulerAngles()

        // Extract yaw and pitch from Euler angles
        val yawRad = eulerAngles.x
        val pitchRad = eulerAngles.y

        // Calculate the forward vector components
        val x = -cos(pitchRad) * sin(yawRad)
        val y = -sin(pitchRad)
        val z = cos(pitchRad) * cos(yawRad)

        val forwardVector = Vector(x, y, z).normalize()

        VentureLibs.instance.smoothCoastersAPI.setRotation(null, player,
            forwardVector.x.toFloat(), forwardVector.y.toFloat(),
            forwardVector.z.toFloat(), forwardVector.lengthSquared().toFloat(), 3)
    }

    override fun despawn() {
        val player = itemDisplay?.passengers?.firstOrNull() as? Player
        if(player != null) {
            VentureLibs.instance.smoothCoastersAPI.resetRotation(null, player)
        }

        interaction?.remove()
        itemDisplay?.remove()
    }
}