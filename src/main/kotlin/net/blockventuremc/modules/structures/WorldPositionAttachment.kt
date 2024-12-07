package net.blockventuremc.modules.structures

import org.bukkit.util.Vector
import org.joml.Matrix4f

class WorldPositionAttachment(name: String, position: Vector, rotation: Vector) :
    Attachment(name, position, rotation, true) {

    override fun updateTransformRecurse(transform: Matrix4f) {
        worldTransform.set(localTransform)

        updateTransform()

        for (child in children.values) {
            child.updateTransformRecurse(worldTransform)
        }
    }

}