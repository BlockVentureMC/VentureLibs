package net.blockventuremc.modules.structures

import org.bukkit.World
import org.bukkit.util.Vector
import org.joml.Matrix4f
import java.util.*

open class RootAttachment(name: String, var position: Vector = Vector(), rotation: Vector = Vector()) :
    Attachment(name, Vector(), rotation, true) {

    val uuid = UUID.randomUUID()
    var animation: Animation? = null
    lateinit var world: World

    var smoothFactor: Int = 3

    init {
        parent = this
        root = this
    }

    open fun initialize() {
        matrix.identity()
        val matrix = matrix.translate(position.toVector3f())
        updateTransformRecurse(matrix)
        spawnAttachmentsRecurse()
    }

    open fun update() {
        matrix.identity()
        matrix.translate(position.toVector3f())
        animation?.animate()
        updateTransformRecurse(matrix)
    }

}