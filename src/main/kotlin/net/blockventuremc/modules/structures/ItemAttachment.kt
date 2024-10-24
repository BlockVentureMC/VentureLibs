package net.blockventuremc.modules.structures

import io.papermc.paper.entity.TeleportFlag
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf

class ItemAttachment(name: String, val item: ItemStack, localPosition: Vector, localRotation: Vector) : Attachment(name, localPosition, localRotation) {

    var itemDisplay: ItemDisplay? = null

    override fun spawn() {
        val location = bukkitLocation
        itemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
        itemDisplay?.apply {
            shadowStrength = 0.0f
            teleportDuration = 3
            interpolationDuration = 3
            itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD
            setItemStack(item)
            isCustomNameVisible = false
            var transform = transformation
            transform.scale.mul(0.617f)
            transformation = transform
        }
        itemDisplay?.customName = "root=$root, name=$name"
    }

    override fun updateTransform() {
        itemDisplay?.let { display ->
            display.teleport(bukkitLocation, TeleportFlag.EntityState.RETAIN_PASSENGERS)

            var transform = display.transformation
            var quaternion = Quaternionf()
            quaternion = worldTransform.getNormalizedRotation(quaternion)
            transform.leftRotation.set(quaternion)
            if(transform != display.transformation) {
                display.interpolationDelay = 0
                display.transformation = transform
            }
        }
    }

    override fun despawn() {
        itemDisplay?.remove()
    }
}