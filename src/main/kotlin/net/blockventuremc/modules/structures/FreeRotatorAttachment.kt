package net.blockventuremc.modules.structures

import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.atan2
import kotlin.math.pow

open class FreeRotatorAttachment(name: String, localPosition: Vector, localRotation: Vector) :
    Attachment(name, localPosition, localRotation, true) {

    override fun updateTransform() {
        super.updateTransform()
    }

    override val localTransform: Matrix4f
        get() {
            return if(animate) calculateLocalTransform else matrix
        }

    var tempMatrix = Matrix4f()

    override fun updateTransformRecurse(transform: Matrix4f) {
        val yaw = atan2(transform.m20().toDouble(), transform.m00().toDouble()).toFloat()

        tempMatrix.set(transform)
        // Entferne die Rotation (Setze Rotationsanteile auf Identität)
        tempMatrix.m00(1f).m01(0f).m02(0f) // X-Achse right
        tempMatrix.m10(0f).m11(1f).m12(0f) // Y-Achse up
        tempMatrix.m20(0f).m21(0f).m22(1f) // Z-Achse forward

        //keep yaw
        tempMatrix.rotateY(yaw)

        worldTransform.set(tempMatrix)
        worldTransform.mul(localTransform)

        updateTransform()

        for (child in children.values) {
            child.updateTransformRecurse(worldTransform)
        }
    }

}