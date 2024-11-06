package net.blockventuremc.modules.structures

import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.max

class Ball(name: String, position: Vector, rotation: Vector, var radius: Double) : RootAttachment(name, position, rotation)  {

    var velocity = Vector()
    var yaw = 0.0f
    val bounceStop = 0.3
    val retention = 0.8

    override fun update() {
        movementUpdate()
        super.update()
    }

    fun movementUpdate() {
        var location = bukkitLocation
        // Air drag
        velocity = velocity.multiply(0.9)

        val collisionCheckVector = if (velocity.length() > 0) velocity else Vector(0, -1, 0)


        //val collision =  world.rayTrace(location, collisionCheckVector, velocity.length(), FluidCollisionMode.NEVER, true, 0.4, null)
        val collision = location.world.rayTraceBlocks(location, collisionCheckVector, max(radius, velocity.length()), FluidCollisionMode.NEVER, true)

        if (collision?.hitBlockFace != null) {
            val normal = collision.hitBlockFace!!.direction

            // Reflexion der Geschwindigkeit

            velocity = velocity.subtract(normal.multiply(2 * velocity.dot(normal))).multiply(retention)


            val hitPosition = collision.hitPosition
            val penetrationDepth = radius - hitPosition.distance(location.toVector())
            Bukkit.broadcastMessage("p: $penetrationDepth")

            if (penetrationDepth > 0) {
                position = position.add(normal.clone().multiply(penetrationDepth + radius))
                velocity = velocity.add(normal.multiply(penetrationDepth + radius * 0.1))
            }
        }

        position = position.add(velocity)
    }
}