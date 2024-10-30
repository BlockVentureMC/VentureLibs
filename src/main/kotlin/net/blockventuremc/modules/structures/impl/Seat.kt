package net.blockventuremc.modules.structures.impl

import io.papermc.paper.entity.TeleportFlag
import net.blockventuremc.VentureLibs
import net.blockventuremc.modules.structures.Attachment
import net.blockventuremc.modules.structures.StructureType
import net.blockventuremc.modules.structures.setCustomType
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f

class Seat(name: String, localPosition: Vector, localRotation: Vector) :
    Attachment(name, localPosition, localRotation) {

    var itemDisplay: ItemDisplay? = null
    var interaction: Interaction? = null
    var dynamic = false

    val offset = 0.8//0.53

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
            isCustomNameVisible = false
            customName = "seat"
            var transform = transformation
            transform.scale.mul(0.03f)
            transformation = transform
            setCustomType(StructureType.SEAT)
        }

        interaction = location.world.spawnEntity(location, EntityType.INTERACTION) as Interaction
        interaction?.apply {
            interactionHeight = 1.0f
            interactionWidth = 0.45f
            isCustomNameVisible = false
            itemDisplay?.addPassenger(this)
            setCustomType(StructureType.SEAT)
        }
    }

    override fun updateTransform() {
        var rotation = Quaternionf()
        rotation = worldTransform.getNormalizedRotation(rotation)

        val upVector = (rotation.clone() as Quaternionf).transform(Vector3f(0.0f, 1.0f, 0.0f)).normalize()

        val loopingOffset = upVector.dot(Vector3f(0.0f, -1.0f, 0.0f)).coerceIn(0.0f, 1.0f)
        upVector.mul(loopingOffset).mul(0.0f)

        itemDisplay?.teleport(
            bukkitLocation.add(Vector(0.0, -offset, 0.0))
                .add(upVector.x.toDouble(), upVector.y.toDouble(), upVector.z.toDouble()),
            TeleportFlag.EntityState.RETAIN_PASSENGERS
        )

        passenger?.let { player ->
            VentureLibs.instance.smoothCoastersAPI.setRotation(
                VentureLibs.instance.networkInterface,
                player,
                rotation.x,
                rotation.y,
                rotation.z,
                rotation.w,
                3
            )
        }
    }

    override fun despawn() {

        passenger?.let { player ->
            VentureLibs.instance.smoothCoastersAPI.resetRotation(VentureLibs.instance.networkInterface, player)
        }

        interaction?.remove()
        itemDisplay?.remove()
    }

    val passenger: Player?
        get() {
            return itemDisplay?.passengers?.getOrNull(0) as? Player
        }
}