package net.blockventuremc.extensions

import org.bukkit.util.EulerAngle
import org.joml.Vector3f
import org.joml.Quaternionf
import kotlin.math.*

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

// Function to calculate Euler angles from front, left, and up vectors
fun calculateEulerAngles(front: Vector3f, up: Vector3f): EulerAngle {
    val yaw = atan2(front.z.toDouble(), front.x.toDouble())
    val pitch = atan2(-front.y.toDouble(), sqrt(front.x * front.x + front.z * front.z.toDouble()))
    val roll = atan2(up.y.toDouble(), up.z.toDouble())
    return EulerAngle(pitch, yaw, roll)
}

// Function to create a quaternion from front, left, and up vectors
fun createQuaternionFromVectors(front: Vector3f, left: Vector3f, up: Vector3f): Quaternionf {
    val mat = org.joml.Matrix3f()
    mat.setColumn(0, left)
    mat.setColumn(1, up)
    mat.setColumn(2, front)
    return Quaternionf().setFromNormalized(mat)
}