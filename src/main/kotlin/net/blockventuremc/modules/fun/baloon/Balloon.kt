package net.blockventuremc.modules.`fun`.baloon


import dev.fruxz.ascend.extension.isNull
import io.papermc.paper.entity.TeleportFlag
import org.bukkit.entity.Chicken
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

class Balloon(val follow: Entity, val item: ItemStack) {

    private var velocity = Vector()
    private var spin = 0.0f
    private var currentRotation = 0.0f

    var stiffness = 0.02
    var damping = 0.91
    var ydamping = 0.97
    var test = 0.01

    private var itemDisplay: ItemDisplay? = null
    private var chicken: Chicken? = null

    fun spawn() {
        val location = follow.location
        location.add(0.0, 0.5, 0.0)
        location.yaw = 0.0f
        location.pitch = 0.0f
        itemDisplay = follow.world.spawn(location, ItemDisplay::class.java).apply {
            setItemStack(item)
            val transform = transformation
            transform.translation.add(0.0f, 0f, 0.0f)
            transform.scale.mul(0.74f)
            transformation = transform
            interpolationDuration = 3
            teleportDuration = 3
        }

        chicken = follow.world.spawn(location, Chicken::class.java).apply {
            isCollidable = false
            setGravity(false)
            setBaby()
            velocity = Vector()
            isSilent = true
            isInvulnerable = true
            maxHealth = Double.MAX_VALUE
            addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, Int.MAX_VALUE))
        }

        chicken!!.addPassenger(itemDisplay!!)
        chicken!!.setLeashHolder(follow)
    }


    fun update() {
        if(chicken == null) return

        val playerLocation = follow.location
        val balloonLocation = chicken?.location ?: return
        var directionToFollower = follow.location.toVector().subtract(chicken!!.location.toVector())
        val followerDirection = follow.location.direction

        if (directionToFollower.isNull || directionToFollower.isZero) return

        directionToFollower = directionToFollower.normalize()


        val balloonDistancePlayer = playerLocation.distance(balloonLocation)
        val maxDistance = 3

        if (balloonDistancePlayer > maxDistance) {
            balloonLocation.add(directionToFollower.multiply(balloonDistancePlayer - maxDistance))
        }

        val balloonRestPosition = follow.location.add(
            Vector(
                -followerDirection.z * 0.9,
                1.6 + if (follow.isSneaking) 0.5 else 0.9,
                followerDirection.x * 0.9
            )
        )
        val force = balloonLocation.toVector().subtract(balloonRestPosition.toVector())
        val displacement = force.length()
        val ballonRotationDirection =
            balloonLocation.toVector().subtract(balloonRestPosition.toVector().add(Vector(0.0, -2.0, 0.0)))

        val flatForce = force.clone()
        flatForce.y = 0.0
        val flatdisplacement = flatForce.length()

        if (!displacement.isNaN()) {
            force.normalize().multiply(-1.0 * stiffness * displacement)
            velocity.add(force)
            velocity.multiply(damping)

            velocity.y -= flatdisplacement * test

            if (velocity.y > 0) {
                velocity.y *= ydamping
            }
            balloonLocation.add(velocity)
        }

        currentRotation += (velocity.length().toFloat() * 0.9f) + 0.02f
        itemDisplay!!.interpolationDelay = 0

        val normalizedSpinDirection = ballonRotationDirection.toVector3f().normalize()
        val matrix = Matrix4f()
        val quaternion = Quaternionf().rotationTo(Vector3f(0f, 1f, 0f).normalize(), normalizedSpinDirection)
        quaternion.rotateY(currentRotation)
        matrix.scale(0.74f)
        matrix.rotate(quaternion)

        itemDisplay!!.setTransformationMatrix(matrix)

        chicken!!.velocity = Vector()
        chicken!!.teleport(balloonLocation, TeleportFlag.EntityState.RETAIN_PASSENGERS)
    }

    fun remove() {
        itemDisplay?.remove()
        chicken?.remove()
        chicken = null
        itemDisplay = null
    }

    private fun lerp(start: Vector, end: Vector, t: Float): Vector {
        val x = (1.0 - t) * start.x + t * end.x
        val y = (1.0 - t) * start.y + t * end.y
        val z = (1.0 - t) * start.z + t * end.z
        return Vector(x, y, z)
    }


}