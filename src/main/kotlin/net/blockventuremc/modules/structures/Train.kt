package net.blockventuremc.modules.structures

import net.blockventuremc.extensions.createQuaternionFromVectors
import net.blockventuremc.modules.rides.track.TrackNode
import net.blockventuremc.modules.rides.track.TrackRide
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

//physikalische konstanten
const val g = 9.81//m/s²
const val airDensity = 1.225//kg/m³

class Train(name: String, val trackRide: TrackRide, world: World, position: Vector, rotation: Vector): CustomEntity(name, world, position, rotation) {

    var currentPosition = 0.0
    var rotationQuaternion = Quaternionf()

    var mass = 1500.0 //masse kilogramm
    var dragCoefficient = 0.3 // Luftwiderstandsbeiwert (Cw-Wert)
    val frictionCoefficient = 0.04  // Rollreibungskoeffizient (angenommener Wert) abhängig von wagen und schiene

    var velocity = 0.0

    override fun update() {
        currentPosition += velocity

        if (currentPosition < 0) {
            currentPosition += trackRide.totalLength
        } else if (currentPosition >= trackRide.totalLength) {
            currentPosition %= trackRide.totalLength
        }

        val trackNode = trackNodeAtDistance(currentPosition)

        val front = trackNode.frontVector.toVector3f().normalize()
        val up = trackNode.upVector.toVector3f().normalize()
        val left = trackNode.leftVector.toVector3f().normalize()

        simulate(trackNode, front)

        rotationQuaternion = createQuaternionFromVectors(front, left, up)
        position = trackRide.origin.toVector().add(trackNode.position)

        super.update()
    }

    override val localTransform: Matrix4f
        get() {
            var matrix = Matrix4f().translate(localPosition.toVector3f())
            matrix.rotate(rotationQuaternion)
            return matrix
        }

    fun simulate(trackNode: TrackNode, front: Vector3f) {

        val horizontalMagnitude = sqrt(front.x.pow(2) + front.y.pow(2))
        val slope = atan2(front.z, horizontalMagnitude)//> Radians

        //Kräfteberechnung
        val normalForce = mass * g * cos(atan(slope))

        val gravityforce = mass * g * sin(atan(slope))

        val airResist = 0.5 * airDensity * dragCoefficient * velocity.pow(2)
        val frictionForce = frictionCoefficient * normalForce

        val netForce = gravityforce - airResist - frictionForce

        val acceleration = (netForce / mass) / 20.0
        velocity += acceleration

    }

    fun trackNodeAtDistance(distance: Double): TrackNode {
        val totalNodes = trackRide.nodes.size - 1
        val distanceBetweenNodes = trackRide.nodeDistance

        val nodeIndex = (distance / distanceBetweenNodes).toInt()

        val clampedNodeIndex = nodeIndex % totalNodes
        val nextNodeIndex = (clampedNodeIndex + 1) % totalNodes

        val currentNode = trackRide.nodes[clampedNodeIndex]
        val nextNode = trackRide.nodes[nextNodeIndex]

        val localDistance = distance % distanceBetweenNodes
        val t = localDistance / distanceBetweenNodes
        return lerpTrackNodes(currentNode, nextNode, t)
    }

    fun lerp(start: Double, target: Double, t: Double) = start + t * (target - start)

    fun lerp(start: Float, target: Float, t: Double): Float = start + t.toFloat() * (target - start)

    fun lerpTrackNodes(node1: TrackNode, node2: TrackNode, t: Double): TrackNode {
        val posX = lerp(node1.posX, node2.posX, t)
        val posY = lerp(node1.posY, node2.posY, t)
        val posZ = lerp(node1.posZ, node2.posZ, t)

        val frontX = lerp(node1.frontX, node2.frontX, t)
        val frontY = lerp(node1.frontY, node2.frontY, t)
        val frontZ = lerp(node1.frontZ, node2.frontZ, t)

        val leftX = lerp(node1.leftX, node2.leftX, t)
        val leftY = lerp(node1.leftY, node2.leftY, t)
        val leftZ = lerp(node1.leftZ, node2.leftZ, t)

        val upX = lerp(node1.upX, node2.upX, t)
        val upY = lerp(node1.upY, node2.upY, t)
        val upZ = lerp(node1.upZ, node2.upZ, t)

        return TrackNode(
            id = node2.id,
            posX = posX,
            posY = posY,
            posZ = posZ,
            frontX = frontX,
            frontY = frontY,
            frontZ = frontZ,
            leftX = leftX,
            leftY = leftY,
            leftZ = leftZ,
            upX = upX,
            upY = upY,
            upZ = upZ
        )
    }

}