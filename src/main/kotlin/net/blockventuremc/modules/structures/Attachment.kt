package net.blockventuremc.modules.structures

import io.papermc.paper.entity.TeleportFlag
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.Item
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BlockVector
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf

open class Attachment(
    val name: String,
    val localPosition: Vector,
    var localRotation: Vector
) {

    lateinit var root: CustomEntity

    var parent: Attachment? = null
    private val children: MutableMap<String, Attachment> = HashMap()

    var worldTransform = Matrix4f()

    fun addChild(child: Attachment) {
        if(this is CustomEntity) {
            child.root = this
        } else {
            child.root = this.root
        }
        child.parent = this;
        children[child.name] = child
    }

    open fun spawn() {}
    open fun despawn() {}
    open fun updateTransform() {}

    //Recurse

    fun spawnAttachmentsRecurse() {
        spawn()
        for(child in children.values) {
            child.spawnAttachmentsRecurse()
        }
    }

    fun despawnAttachmentsRecurse() {
        despawn()
        for(child in children.values) {
            child.despawnAttachmentsRecurse()
        }
    }

    fun updateTransformRecurse(transform: Matrix4f) {
        worldTransform.set(transform)
        worldTransform.mul(localTransform)

        for(child in children.values) {
            child.updateTransformRecurse(worldTransform.clone() as Matrix4f)
        }
    }

    fun tickAttachmentsRecurse() {
        updateTransform()
        for(child in children.values) {
            child.tickAttachmentsRecurse()
        }
    }

    val localTransform: Matrix4f
        get() {
            var matrix = Matrix4f().translate(localPosition.toVector3f())
            val yaw = Math.toRadians(localRotation.y).toFloat()
            val pitch = Math.toRadians(localRotation.x).toFloat()
            val roll = Math.toRadians(localRotation.z).toFloat()
            matrix.rotateY(-yaw)
            matrix.rotateX(pitch)
            matrix.rotateZ(roll)
            return matrix
        }

    val bukkitLocation: Location
        get() {
            val pos = worldPosition
            return Location(root.world, pos.x, pos.y, pos.z)
        }

    val worldPosition: Vector
        get() {
            return Vector(worldTransform.m30(), worldTransform.m31(), worldTransform.m32())
        }

    val worldRotation: Vector
        get() {
            return Vector(worldTransform.m30(), worldTransform.m31(), worldTransform.m32())
        }
}

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

class Seat(name: String, localPosition: Vector, localRotation: Vector) : Attachment(name, localPosition, localRotation) {

    var itemDisplay: ItemDisplay? = null
    var interaction: Interaction? = null
    var dynamic = false

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
            transform.scale.mul(0.1f)
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
        itemDisplay?.teleport(bukkitLocation, TeleportFlag.EntityState.RETAIN_PASSENGERS)
    }

    override fun despawn() {
        interaction?.remove()
        itemDisplay?.remove()
    }
}

class EmptyAttachment(name: String, localPosition: Vector, localRotation: Vector) : Attachment(name, localPosition, localRotation) {}

class CustomEntity(name: String, val world: World, var position: Vector, rotation: Vector) : Attachment(name,
    Vector(), rotation) {

    var animation: Animation? = null

    init {
        parent = this
        root = this
    }

    fun initialize() {
        val matrix = Matrix4f().translate(position.toVector3f())
        updateTransformRecurse(matrix)
        spawnAttachmentsRecurse()
    }

    fun update() {
        val matrix = Matrix4f().translate(position.toVector3f())
        animation?.animate()
        updateTransformRecurse(matrix)
        tickAttachmentsRecurse()
    }

    override fun spawn() {
    }

}