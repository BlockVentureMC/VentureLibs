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
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

class Train(name: String, val trackRide: TrackRide, world: World, position: Vector, rotation: Vector): CustomEntity(name, world, position, rotation) {

    var currentPosition = 0.0
    var rotationQuaternion = Quaternionf()

    var mass = 700.0f //masse kilogramm
    val rollCoefficient = 0.072f  // Rollreibungskoeffizient (angenommener Wert) abhängig von wagen und schiene
    val crossArea = 1.1f //Querschnittsfläche 0.5 bis 1,5 in Quadratmeter damit ist die Stirnfläche gemeint
    var velocity = 0.0f

    fun simulate(trackNode: TrackNode, forward: Vector3f, up: Vector3f) {
        var force = 0.0f

        val worldUp = Vector3f(0.0f, 1.0f, 0.0f)

        //gravity
        val fdotUp = -forward.dot(worldUp)
        var acc = fdotUp * gravity
        force += acc * mass

        //roll resistance
        val NForce = up.dot(worldUp) * gravity
        force += -sign(velocity) * rollCoefficient * (NForce * mass)

        //drag
        val dragCoefficient = 0.6f
        var v2 = velocity * velocity
        force += -sign(velocity) * 0.5f * airDensity * v2 * dragCoefficient * crossArea

        val deltaTime = 0.025f
        velocity += force * deltaTime / mass
    }

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

        simulate(trackNode, front, up )

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