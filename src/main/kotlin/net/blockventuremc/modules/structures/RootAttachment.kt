package net.blockventuremc.modules.structures

import org.bukkit.World
import org.bukkit.util.Vector
import org.joml.Matrix4f
import java.util.*

open class RootAttachment(name: String, var position: Vector = Vector(), rotation: Vector = Vector()) :
    Attachment(name, Vector(), rotation) {

    val uuid = UUID.randomUUID()
    var animation: Animation? = null
    lateinit var world: World

    var smoothFactor: Int = 3

    init {
        parent = this
        root = this
    }

    open fun initialize() {
        val matrix = Matrix4f().translate(position.toVector3f())
        updateTransformRecurse(matrix)
        spawnAttachmentsRecurse()
    }

    open fun update() {
        val matrix = Matrix4f().translate(position.toVector3f())
        animation?.animate()
        updateTransformRecurse(matrix)
    }

}