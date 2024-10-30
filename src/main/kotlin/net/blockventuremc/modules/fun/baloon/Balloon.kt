package net.blockventuremc.modules.`fun`.baloon


import dev.fruxz.ascend.extension.isNull
import io.papermc.paper.entity.TeleportFlag
import net.blockventuremc.modules.structures.StructureType
import net.blockventuremc.modules.structures.setCustomType
import org.bukkit.Location
import org.bukkit.entity.Chicken
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.PufferFish
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

open class Balloon(val follow: Entity) {

    var velocity = Vector()
    var balloon: PufferFish? = null

    open fun spawn(location: Location = follow.location) {
        location.add(0.0, 0.5, 0.0)
        location.yaw = 0.0f
        location.pitch = 0.0f

        balloon = follow.world.spawn(location, PufferFish::class.java).apply {
            isCollidable = false
            setGravity(false)
            isSilent = true
            isInvulnerable = true
            setNoPhysics(true)
            isInvisible = false
            addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, Int.MAX_VALUE))
            setLeashHolder(follow)
            setCustomType(StructureType.BALLOON)
        }
    }


    open fun update() {
        if(balloon == null) return
    }

    open fun remove() {
        balloon?.remove()
        balloon = null
    }

    fun lerp(start: Vector, end: Vector, t: Float): Vector {
        val x = (1.0 - t) * start.x + t * end.x
        val y = (1.0 - t) * start.y + t * end.y
        val z = (1.0 - t) * start.z + t * end.z
        return Vector(x, y, z)
    }

}

class EntityBalloon(follow: Entity, val entityType: EntityType): Balloon(follow) {

    private var currentRotation = 0.0f

    var stiffness = 0.02
    var damping = 0.91
    var ydamping = 0.97
    var test = 0.01

    private var entity: Entity? = null

    override fun spawn(location: Location) {
        super.spawn(location)
        val location = follow.location
        location.add(0.0, 0.5, 0.0)
        location.yaw = 0.0f
        location.pitch = 0.0f
        entity = follow.world.spawnEntity(location, entityType).apply {
            setGravity(false)
            isInvulnerable = true
            setCustomType(StructureType.BALLOON)
        }
        balloon!!.addPassenger(entity!!)
    }


    override fun update() {
        super.update()

        val playerLocation = follow.location
        val balloonLocation = balloon?.location ?: return
        var directionToFollower = follow.location.toVector().subtract(balloon!!.location.toVector())
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

        currentRotation += (velocity.length().toFloat() * 0.9f) + 0.4f

        entity?.setRotation(currentRotation, 0.0f)
        balloon!!.velocity = Vector()
        balloon!!.teleport(balloonLocation, TeleportFlag.EntityState.RETAIN_PASSENGERS)
    }

    override fun remove() {
        super.remove()
        entity?.remove()
        entity = null
    }
}

class ItemBalloon(follow: Entity, val item: ItemStack): Balloon(follow) {

    private var currentRotation = 0.0f

    var stiffness = 0.02
    var damping = 0.91
    var ydamping = 0.97
    var test = 0.01

    private var itemDisplay: ItemDisplay? = null

    override fun spawn(location: Location) {
        super.spawn(location)
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
            setCustomType(StructureType.BALLOON)
        }
        balloon!!.addPassenger(itemDisplay!!)
    }


    override fun update() {
        super.update()

        val playerLocation = follow.location
        val balloonLocation = balloon?.location ?: return
        var directionToFollower = follow.location.toVector().subtract(balloon!!.location.toVector())
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

        balloon!!.velocity = Vector()
        balloon!!.teleport(balloonLocation, TeleportFlag.EntityState.RETAIN_PASSENGERS)
    }

    override fun remove() {
        super.remove()
        itemDisplay?.remove()
        itemDisplay = null
    }

}