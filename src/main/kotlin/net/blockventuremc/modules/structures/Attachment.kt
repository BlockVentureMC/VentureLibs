package net.blockventuremc.modules.structures

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BlockVector
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf

open class Attachment(
    val name: String,
    val localPosition: Vector,
    val localRotation: Vector
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
        worldTransform = transform.clone() as Matrix4f

        //hier animation stuff position and rotation kann sich hier ändern

        worldTransform.mul(localTransform)

        for(child in children.values) {
            child.updateTransformRecurse(worldTransform)
        }
    }


    val localTransform: Matrix4f
        get() {
            var matrix = Matrix4f().translate(localPosition.toVector3f())
            //yaw pitch roll
            val quaternion = Quaternionf().rotationXYZ(localRotation.x.toFloat(), localRotation.y.toFloat(), localRotation.z.toFloat())
            matrix.rotate(quaternion)
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
            teleportDuration = 2
            interpolationDuration = 2
            itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD
           setItemStack(item)
            isCustomNameVisible = true
        }
        itemDisplay?.customName = "root=$root, name=$name"
    }

    override fun updateTransform() {
        itemDisplay?.teleport(bukkitLocation)
    }

    override fun despawn() {
        itemDisplay?.remove()
    }
}


class CustomEntity(name: String, val world: World, position: Vector, rotation: Vector) : Attachment(name,
    Vector(), rotation) {

    init {
        parent = this
        root = this
    }

    fun initialize() {
        val matrix = Matrix4f().translate(localPosition.toVector3f())
        updateTransformRecurse(matrix)
        spawnAttachmentsRecurse()
    }

    override fun spawn() {
    }

}