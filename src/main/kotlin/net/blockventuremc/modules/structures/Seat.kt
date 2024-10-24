package net.blockventuremc.modules.structures

import io.papermc.paper.entity.TeleportFlag
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

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

        //forward Vector
        //itemDisplay?.passegner[1]-> player
    }

    override fun despawn() {
        interaction?.remove()
        itemDisplay?.remove()
    }
}