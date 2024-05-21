package net.blockventuremc.extensions

import org.joml.Vector3f
import org.joml.Quaternionf
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

operator fun Vector3f.minus(toVector3f: Vector3f): Vector3f {
    return Vector3f(this.x - toVector3f.x, this.y - toVector3f.y, this.z - toVector3f.z)
}

fun directionToQuaternion(origin: Vector3f, target: Vector3f): Quaternionf {
    // Normalize the input vectors
    val uNorm = Vector3f(origin).normalize()
    val vNorm = Vector3f(target).normalize()

    // Compute the rotation axis
    val a = uNorm.cross(vNorm, Vector3f()).normalize()

    // Compute the rotation angle
    val dotProduct = uNorm.dot(vNorm)
    val theta = acos(dotProduct)

    // Compute the quaternion components
    val halfTheta = theta / 2
    val w = cos(halfTheta)
    val sinHalfTheta = sin(halfTheta)
    val x = a.x * sinHalfTheta
    val y = a.y * sinHalfTheta
    val z = a.z * sinHalfTheta

    return Quaternionf(x, y, z, w)
}