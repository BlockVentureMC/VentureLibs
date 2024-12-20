package net.blockventuremc.modules.structures

import org.bukkit.Location
import org.bukkit.util.Vector
import org.joml.Matrix4f

open class Attachment(
    val name: String,
    var localPosition: Vector,
    var localRotation: Vector,
    var transform: Matrix4f = Matrix4f(),
    var localTransformChance: Boolean = false
) {

    init {
        if(!localTransformChance) transform = calculateLocalTransform
    }

    lateinit var root: RootAttachment

    var parent: Attachment? = null
    val children: MutableMap<String, Attachment> = HashMap()

    var worldTransform = Matrix4f()

    fun addChild(child: Attachment) {
        if (this is RootAttachment) {
            child.root = this
        } else {
            child.root = this.root
        }
        child.parent = this
        children[child.name] = child
    }

    open fun spawn() {}
    open fun despawn() {}
    open fun updateTransform() {}

    fun spawnAttachmentsRecurse() {
        spawn()
        for (child in children.values) {
            child.spawnAttachmentsRecurse()
        }
    }

    fun despawnAttachmentsRecurse() {
        despawn()
        for (child in children.values) {
            child.despawnAttachmentsRecurse()
        }
    }

    fun updateTransformRecurse(transform: Matrix4f) {
        worldTransform.set(transform)
        worldTransform.mul(localTransform)

        updateTransform()

        for (child in children.values) {
            child.updateTransformRecurse(worldTransform.clone() as Matrix4f)
        }
    }

    private val calculateLocalTransform: Matrix4f
        get() {
        var matrix = Matrix4f().translate(localPosition.toVector3f())

        if (localRotation.isZero) return matrix

        val yaw = Math.toRadians(localRotation.y).toFloat()
        val pitch = Math.toRadians(localRotation.x).toFloat()
        val roll = Math.toRadians(localRotation.z).toFloat()
        matrix.rotateY(-yaw)
        matrix.rotateX(pitch)
        matrix.rotateZ(roll)

        return matrix
    }

    open val localTransform: Matrix4f
        get() = calculateLocalTransform

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