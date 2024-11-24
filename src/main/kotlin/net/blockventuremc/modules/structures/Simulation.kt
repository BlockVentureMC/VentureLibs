package net.blockventuremc.modules.structures

import io.papermc.paper.entity.TeleportFlag
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.sign

//physikalische konstanten
const val gravity = 9.81f//m/s²
const val airDensity = 1.225f//kg/m³
const val deltaTime = 0.05f//s
const val solveIterations = 1

class Simulation {
}

class Point(
    var position: Vector,
    var prevPosition: Vector = position.clone(),
    var locked: Boolean = false
)

class Stick(
    var point1: Point,
    var point2: Point,
) {
    val length: Double = point1.position.distance(point2.position)
}

class EntityRope(val world: World, val item: ItemStack, gravity: Float) : Rope(gravity) {

    var entities: MutableList<ItemDisplay> = mutableListOf()

    fun update() {
        simulate()

        entities.forEachIndexed { index, entity ->
            val point = points[index]
            entity.teleport(Location(world, point.position.x, point.position.y, point.position.z), TeleportFlag.EntityState.RETAIN_PASSENGERS)
            if(index != 0) {
                val pointBefore = points[index - 1]

                val stickDirection = pointBefore.position.clone().subtract(point.position).normalize()

                val matrix = Matrix4f()
                val quaternion = Quaternionf().rotationTo(Vector3f(0f, 1f, 0f).normalize(), stickDirection.toVector3f())
                matrix.rotate(quaternion)

                entity.setTransformationMatrix(matrix)
            }
        }

    }

    fun spawn() {
        for(point in points) {
            val location = Location(world, point.position.x, point.position.y, point.position.z)
            val itemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
            itemDisplay.apply {
                shadowStrength = 0.0f
                teleportDuration = 3
                interpolationDuration = 3
                itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD
                setItemStack(item)
                setCustomType(StructureType.GENERIC)
                isPersistent = true
            }
            entities.add(itemDisplay)
        }
    }

    fun despawn() {
        entities.forEach { entity ->
            entity.remove()
        }
        entities.clear()
    }

}

class Pendulum(val pendulum: Stick, val mass: Float = 20.0f, val world: World) {

    lateinit var itemDisplay: ItemDisplay

    fun spawn() {
            val location = Location(world, pendulum.point2.position.x, pendulum.point2.position.y, pendulum.point2.position.z)
        itemDisplay = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
            itemDisplay.apply {
                shadowStrength = 0.0f
                teleportDuration = 3
                interpolationDuration = 3
                itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD
                setItemStack(ItemStack(Material.DIAMOND_BLOCK))
                setCustomType(StructureType.GENERIC)
                isPersistent = true
            }

    }

    fun despawn() {
        itemDisplay.remove()
    }

    fun simulate() {
        val positionBeforeUpdate = pendulum.point2.position.clone()
        pendulum.point2.position.add(pendulum.point2.position.clone().subtract(pendulum.point2.prevPosition))
        pendulum.point2.position.add(Vector(0.0f, -1.0f, 0.0f).multiply(mass * deltaTime * deltaTime))
        pendulum.point2.prevPosition = positionBeforeUpdate

        val stickCentre = pendulum.point1.position.clone().add(pendulum.point2.position).multiply(0.5f)
        val stickDirection = pendulum.point1.position.clone().subtract(pendulum.point2.position).normalize()
        //val length = pendulum.point1.position.distance(pendulum.point2.position)
        //if(length > pendulum.length) {
        pendulum.point2.position = stickCentre.subtract(stickDirection.clone().multiply(pendulum.length * 0.5f))

        val matrix = Matrix4f()
        matrix.rotate(Quaternionf().rotationTo(Vector3f(0f, sign(mass), 0f).normalize(), stickDirection.toVector3f()))

        itemDisplay.teleport(Location(world, pendulum.point2.position.x, pendulum.point2.position.y, pendulum.point2.position.z), TeleportFlag.EntityState.RETAIN_PASSENGERS)

        itemDisplay.setTransformationMatrix(matrix)
    }

}

open class Rope(val gravity: Float = 20.0f) {
    var points: MutableList<Point> = mutableListOf()
    var sticks: MutableList<Stick> = mutableListOf()

    fun simulate() {
        points.forEach { point ->
            if(!point.locked) {
                val positionBeforeUpdate = point.position.clone()
                point.position.add(point.position.clone().subtract(point.prevPosition))
                point.position.add(Vector(0.0f, -1.0f, 0.0f).multiply(gravity * deltaTime * deltaTime))
                point.prevPosition = positionBeforeUpdate
            }
        }

            sticks.forEach { stick ->

                val stickCentre = stick.point1.position.clone().add(stick.point2.position).multiply(0.5f)
                val stickDirection = stick.point1.position.clone().subtract(stick.point2.position).normalize()
                //val length = stick.point1.position.distance(stick.point2.position)

                //if (!stick.point1.locked)
                    //stick.point1.position = stickCentre.clone().add(stickDirection.clone().multiply(stick.length * 0.5f))

                if (!stick.point2.locked)
                    stick.point2.position = stickCentre.clone().subtract(stickDirection.clone().multiply(stick.length * 0.5f))

            }

    }
}


