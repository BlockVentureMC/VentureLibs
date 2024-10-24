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
import java.util.UUID

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

        updateTransform()

        for(child in children.values) {
            child.updateTransformRecurse(worldTransform.clone() as Matrix4f)
        }
    }

    open val localTransform: Matrix4f
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