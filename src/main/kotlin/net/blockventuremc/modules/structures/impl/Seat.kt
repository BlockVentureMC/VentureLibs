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
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

class Seat(name: String, localPosition: Vector, localRotation: Vector) :
    Attachment(name, localPosition, localRotation, Matrix4f(), true) {

    var itemDisplay: ItemDisplay? = null
    var interaction: Interaction? = null
    var dynamic = false
    var smoothCoaster = true

    val offset = 0.8//0.53

    init {
        localPosition.add(Vector(0.0, offset, 0.0))
        localTransformChance = false
    }

    override fun spawn() {
        val location = bukkitLocation
        itemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
        itemDisplay?.apply {
            shadowStrength = 0.0f
            teleportDuration = root.smoothFactor
            interpolationDuration = root.smoothFactor
            itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD
            isCustomNameVisible = false
            customName = "seat"
            var transform = transformation
            transform.scale.mul(0.03f)
            transformation = transform
            setCustomType(StructureType.SEAT, root.uuid.toString())
            isPersistent = true
        }
        interaction = location.world.spawnEntity(location, EntityType.INTERACTION) as Interaction
        interaction?.apply {
            interactionHeight = 1.2f
            interactionWidth = 0.65f
            isCustomNameVisible = false
            itemDisplay?.addPassenger(this)
            setCustomType(StructureType.SEAT, root.uuid.toString())
            isPersistent = true
        }
    }

    override fun updateTransform() {
        itemDisplay?.let { display->

            display.teleport(bukkitLocation.add(Vector(0.0, -offset, 0.0)), TeleportFlag.EntityState.RETAIN_PASSENGERS)
        if(smoothCoaster) {
            var quaternion = Quaternionf()
            quaternion = worldTransform.getNormalizedRotation(quaternion)
            passenger?.let { player ->
                VentureLibs.instance.smoothCoastersAPI.setRotation(
                    VentureLibs.instance.networkInterface,
                    player,
                    quaternion.x,
                    quaternion.y,
                    quaternion.z,
                    quaternion.w,
                    4
                )
            }
            }
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