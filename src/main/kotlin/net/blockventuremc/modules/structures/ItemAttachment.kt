package net.blockventuremc.modules.structures

import io.papermc.paper.entity.TeleportFlag
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf

class ItemAttachment(name: String, val item: ItemStack, localPosition: Vector, localRotation: Vector) :
    Attachment(name, localPosition, localRotation) {

    var itemDisplay: ItemDisplay? = null
    private var scale = 0.617f

    override fun spawn() {
        val location = bukkitLocation
        itemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
        itemDisplay?.apply {
            shadowStrength = 0.0f
            teleportDuration = root.smoothFactor
            interpolationDuration = root.smoothFactor
            itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD
            setItemStack(item)
            isCustomNameVisible = false
            var transform = transformation
            transform.scale.mul(scale)
            transformation = transform
            setCustomType(StructureType.GENERIC)
            isPersistent = true
        }
        itemDisplay?.customName = "root=$root, name=$name"
    }

    override fun updateTransform() {
        itemDisplay?.let { display ->
            display.teleport(bukkitLocation, TeleportFlag.EntityState.RETAIN_PASSENGERS)

            var transform = display.transformation

            transform.leftRotation.set(worldTransform.getNormalizedRotation(Quaternionf()))
            if (transform != display.transformation) {
                display.interpolationDelay = 0
                display.transformation = transform
            }
        }
    }

    fun setScale(scale: Float): ItemAttachment {
        this.scale = scale
        return this
    }

    override fun despawn() {
        itemDisplay?.remove()
    }
}