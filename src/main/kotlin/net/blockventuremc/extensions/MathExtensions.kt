package net.blockventuremc.extensions

import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.*

operator fun Vector3f.minus(toVector3f: Vector3f): Vector3f {
    return Vector3f(this.x - toVector3f.x, this.y - toVector3f.y, this.z - toVector3f.z)
}

fun remap(
    input: Float,
    min: Float,
    max: Float,
    targetMin: Float,
    targetMax: Float
): Float {
    if (min == max) throw IllegalArgumentException("min und max dürfen nicht gleich sein")

    val proportion = (input - min) / (max - min)

    val remapped = if (targetMin < targetMax) {
        targetMin + proportion * (targetMax - targetMin)
    } else {
        targetMin - proportion * (targetMin - targetMax)
    }
    return if (targetMin < targetMax) {
        remapped.coerceIn(targetMin, targetMax)
    } else {
        remapped.coerceIn(targetMax, targetMin)
    }
}

fun lerp(currentValue: Float, targetValue: Float, deltaTime: Float, speed: Float): Float {
    val difference = targetValue - currentValue
    return currentValue + difference * speed * deltaTime
}

fun lerpVector(start: Vector, end: Vector, t: Float): Vector {
    val newX = start.getX() + t * (end.getX() - start.getX());
    val newY = start.getY() + t * (end.getY() - start.getY());
    val newZ = start.getZ() + t * (end.getZ() - start.getZ());
    return Vector(newX, newY, newZ);
}


// Function to create a quaternion from front, left, and up vectors
fun createQuaternionFromVectors(front: Vector3f, left: Vector3f, up: Vector3f): Quaternionf {
    val mat = org.joml.Matrix3f()
    mat.setColumn(0, left)
    mat.setColumn(1, up)
    mat.setColumn(2, front)
    return Quaternionf().setFromNormalized(mat)

}
