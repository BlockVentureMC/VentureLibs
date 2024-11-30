package net.blockventuremc.modules.structures

import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class PendulumAttachment(name: String, localPosition: Vector, val length: Double = 4.0, var angle: Double = 0.0) :
    FreeRotatorAttachment(name, localPosition, Vector()) {

    var angleVelocity = 0.0
    var angularAcceleration = 0.0

    var damping = 0.97

    var prevOrigin = Vector(0.0,0.0,0.0)
    var prevPrevOrigin = Vector(0.0,0.0,0.0)

    override fun updateTransform() {
        val origin = worldPosition

        val originVelocity = origin.clone().subtract(prevOrigin)
        val prevOriginVelocity = prevOrigin.clone().subtract(prevPrevOrigin)
        val originAcceleration = originVelocity.clone().subtract(prevOriginVelocity)

        angularAcceleration = -cos(angle) / length * (originAcceleration.x + originAcceleration.z) * 100
        angularAcceleration += -sin(angle) / length * -originAcceleration.y * 100
        angularAcceleration += gravity / length * sin(angle)

        angleVelocity += angularAcceleration
        angleVelocity *= damping

        angleVelocity = angleVelocity.coerceIn(-0.5,0.5)
        angle += angleVelocity

        prevPrevOrigin = prevOrigin
        prevOrigin = origin

        localRotation.x = Math.toDegrees(angle) + 180
    }

}