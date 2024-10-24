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

fun Quaternionf.toEulerAngles(): EulerAngle {
    val ysqr = y * y

    // Roll (x-axis rotation)
    val t0 = +2.0 * (w * x + y * z)
    val t1 = +1.0 - 2.0 * (x * x + ysqr)
    val roll = atan2(t0, t1)

    // Pitch (y-axis rotation)
    var t2 = +2.0 * (w * y - z * x)
    t2 = if (t2 > 1.0) 1.0 else t2
    t2 = if (t2 < -1.0) -1.0 else t2
    val pitch = asin(t2)

    // Yaw (z-axis rotation)
    val t3 = +2.0 * (w * z + x * y)
    val t4 = +1.0 - 2.0 * (ysqr + z * z)
    val yaw = atan2(t3, t4)

    return EulerAngle(pitch, yaw, roll)
}
