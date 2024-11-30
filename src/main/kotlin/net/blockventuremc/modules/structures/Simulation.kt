package net.blockventuremc.modules.structures

import io.papermc.paper.entity.TeleportFlag
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

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

class SimplePendulum(val world: World, var origin: Vector, val length: Double = 4.0, var mass: Double = 1.0, var angle: Double = PI / 4) {

    var position = Vector(0.0, -length ,0.0)

    var angleVelocity = 0.0
    var angularAcceleration = 0.0

    var damping = 0.97

    var prevOrigin = origin.clone()
    var prevPrevOrigin = origin.clone()

    lateinit var itemDisplay: ItemDisplay
    lateinit var itemDisplay2: ItemDisplay

    fun spawn() {
        val location = Location(world, origin.x, origin.y, origin.z)
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
        itemDisplay2 = location.world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
        itemDisplay2.apply {
            shadowStrength = 0.0f
            teleportDuration = 3
            interpolationDuration = 3
            itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD
            setItemStack(ItemStack(Material.DIAMOND_BLOCK))
            setCustomType(StructureType.GENERIC)
            isPersistent = true
        }
    }

    fun update() {

        val originVelocity = origin.clone().subtract(prevOrigin)
        val prevOriginVelocity = prevOrigin.clone().subtract(prevPrevOrigin)
        val originAcceleration = originVelocity.clone().subtract(prevOriginVelocity)


        angularAcceleration = -cos(angle) / length * (originAcceleration.x + originAcceleration.z) * 100
        angularAcceleration += -sin(angle) / length * -originAcceleration.y * 100
        angularAcceleration += gravity * mass / length * sin(angle)


        angleVelocity += angularAcceleration
        angleVelocity *= damping

        angleVelocity = angleVelocity.coerceIn(-0.5,0.5)
        angle += angleVelocity

        position.x = length / 100 * sin(angle) + origin.x
        position.y = length / 100 * cos(angle) + origin.y
        position.z = origin.z

        prevPrevOrigin = prevOrigin
        prevOrigin = origin

        itemDisplay.teleport(Location(world, origin.x, origin.y, origin.z), TeleportFlag.EntityState.RETAIN_PASSENGERS)
        itemDisplay2.teleport(Location(world, position.x, position.y, position.z), TeleportFlag.EntityState.RETAIN_PASSENGERS)

        var transform = itemDisplay2.transformation
        var quaternion = Quaternionf()

        quaternion = Matrix4f().rotateZ(-angle.toFloat()).getNormalizedRotation(quaternion)
        transform.leftRotation.set(quaternion)
        if (transform != itemDisplay2.transformation) {
            itemDisplay2.interpolationDelay = 0
            itemDisplay2.transformation = transform
        }


    }


}

class PendulumOld(val pendulum: Stick, val mass: Float = 20.0f, val world: World) {

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

class RotationalPendulum(
    private val player: Player,
    private val axis: Vector, // Achse, um die sich das Pendel dreht
    private val length: Double = 5.0, // Abstand des Pendels zur Achse
    private val gravity: Double = 9.81, // Gravitationskonstante
    private val inertia: Double = 1.0 // Trägheitsmoment (einstellbar)
) {
    private var angle = 0.0 // Aktueller Winkel (in Radiant)
    private var angularVelocity = 0.0 // Winkelgeschwindigkeit
    private val angularDamping = 0.98 // Dämpfungsfaktor

    fun updatePendulum() {
        // Spielerposition als Drehpunkt
        val pivot = player.location.toVector()

        // Berechne Kraft durch Schwerkraft
        val force = Vector(0.0, -gravity, 0.0)

        // Hebelarm: Position des Pendels in Weltkoordinaten
        val pendulumPos = pivot.clone().add(axis.clone().normalize().crossProduct(Vector(0.0, 1.0, 0.0)).normalize().multiply(length * Math.sin(angle)))

        // Drehmoment berechnen: \tau = r x F
        val leverArm = pendulumPos.clone().subtract(pivot).normalize().multiply(length)
        val torque = leverArm.crossProduct(force).dot(axis)

        // Winkelbeschleunigung: \alpha = \tau / I
        val angularAcceleration = torque / inertia

        // Winkelgeschwindigkeit aktualisieren
        angularVelocity += angularAcceleration * (1.0 / 20.0) // Delta Zeit = 1/20s
        angularVelocity *= angularDamping // Dämpfung

        // Winkel aktualisieren
        angle += angularVelocity * (1.0 / 20.0)

        // Berechne neue Position des Pendels
        val newPosition = pivot.clone().add(
            axis.clone().normalize().crossProduct(Vector(0.0, 1.0, 0.0)).normalize().multiply(length * Math.sin(angle))
        )

        // Optional: Visualisiere die Position des Pendels
        player.world.spawnParticle(
            org.bukkit.Particle.FLAME, newPosition.toLocation(player.world), 1, 0.0, 0.0, 0.0, 0.0
        )
    }
}

// Starten der Simulation
fun startRotationalPendulum(player: Player) {
    val axis = Vector(1.0, 0.0, 0.0) // Achse: x-Achse
    val pendulum = RotationalPendulum(player, axis)
    interval(0, 1) {
        pendulum.updatePendulum()
    }
}
class PendulumSimulator(
    private val player: Player,
    private val length: Double = 5.0, // Länge des Fadens
    private val gravity: Double = 9.81 // Erdbeschleunigung
) {
    private var pendulumPosition = player.location.toVector().add(Vector(0.0, -length, 0.0))
    private var pendulumVelocity = Vector(0.0, 0.0, 0.0)

    // Aktualisiert die Position des Pendels pro Tick
    fun updatePendulum() {
        val playerPosition = player.location.toVector()
        val direction = pendulumPosition.clone().subtract(playerPosition)

        // Schwerkraft-Kraft
        val gravityForce = Vector(0.0, -gravity, 0.0)

        // Spannungskraft (Richtung zum Spieler, projiziert auf die Fadenrichtung)
        val tensionForce = direction.clone().normalize().multiply(
            direction.clone().length() - length
        ).multiply(-30) // Skalar für Stärke der Spannung

        // Gesamtkraft berechnen
        val totalForce = gravityForce.clone().add(tensionForce)

        // Beschleunigung (F = m * a, wir setzen m = 1 für Einfachheit)
        val acceleration = totalForce.clone()

        // Geschwindigkeit updaten
        pendulumVelocity.add(acceleration.multiply(1.0)) // Delta Zeit = 1/20s (1 Tick)

        // Position updaten
        pendulumPosition.add(pendulumVelocity.multiply(1.0 / 20.0))

        // Länge des Fadens einschränken
        val currentLength = pendulumPosition.clone().subtract(playerPosition).length()
        if (currentLength > length) {
            pendulumPosition = playerPosition.clone().add(
                pendulumPosition.clone().subtract(playerPosition).normalize().multiply(length)
            )
            val direction = pendulumPosition.clone().subtract(playerPosition).normalize()
            pendulumVelocity = direction.multiply(pendulumVelocity.dot(direction))
        }

        // Optional: Position visualisieren (z. B. Partikel)
        player.world.spawnParticle(
            org.bukkit.Particle.FLAME, pendulumPosition.toLocation(player.world), 1, 0.0, 0.0, 0.0, 0.0
        )
    }
}

// Starten der Simulation
fun startPendulumSimulation(player: Player) {
    val simulator = PendulumSimulator(player)
    interval(0, 1) {
        if(!player.isOnline || player.inventory.itemInMainHand.type == Material.DIAMOND_BLOCK) {
            return@interval
        }
        simulator.updatePendulum()
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


