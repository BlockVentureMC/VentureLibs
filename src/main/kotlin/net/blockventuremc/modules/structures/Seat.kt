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
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

class Seat(name: String, localPosition: Vector, localRotation: Vector) : Attachment(name, localPosition, localRotation) {

    var itemDisplay: ItemDisplay? = null
    var interaction: Interaction? = null
    var dynamic = false

    val offset = 0.53//0.53

    init {
        localPosition.add(Vector(0.0, offset, 0.0))
    }

    override fun spawn() {
        val location = bukkitLocation
        itemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
        itemDisplay?.apply {
            shadowStrength = 0.0f
            teleportDuration = 2
            interpolationDuration = 2
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
        var rotation = Quaternionf()
        rotation = worldTransform.getNormalizedRotation(rotation)

        val upVector =  (rotation.clone() as Quaternionf).transform(Vector3f(0.0f, 1.0f, 0.0f)).normalize()

        val loopingOffset = upVector.dot(Vector3f(0.0f, -1.0f, 0.0f)).coerceIn(0.0f,1.0f)
        upVector.mul(loopingOffset).mul(0.5f)

        itemDisplay?.teleport(bukkitLocation.add(Vector(0.0, -offset, 0.0)).add(upVector.x.toDouble(),upVector.y.toDouble(),upVector.z.toDouble()), TeleportFlag.EntityState.RETAIN_PASSENGERS)

        val player = itemDisplay?.passengers?.getOrNull(1) as? Player
        if(player == null) return

           // VentureLibs.instance.smoothCoastersAPI.setRotation(null, player, forwardVector.x.toFloat(), forwardVector.y.toFloat(), forwardVector.z.toFloat(), forwardVector.lengthSquared().toFloat(), 3)

    }

    override fun despawn() {
            //VentureLibs.instance.smoothCoastersAPI.resetRotation(null, player)


        interaction?.remove()
        itemDisplay?.remove()
    }
}