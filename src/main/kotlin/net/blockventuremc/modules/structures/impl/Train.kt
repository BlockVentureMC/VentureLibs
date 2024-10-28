package net.blockventuremc.modules.structures.impl

import net.blockventuremc.extensions.createQuaternionFromVectors
import net.blockventuremc.extensions.remap
import net.blockventuremc.modules.rides.track.TrackNode
import net.blockventuremc.modules.rides.track.TrackRide
import net.blockventuremc.modules.structures.deltaTime
import java.util.UUID
import kotlin.math.abs
import kotlin.collections.mutableListOf

class Train(name: String, val trackRide: TrackRide, val startPosition: Double = 0.0) {

    val uuid = UUID.randomUUID()

    val carts: MutableList<Cart> = mutableListOf()
    var currentPosition = 0.0
    var ticksLived = 0
    var velocity = 0.0f//m/s

    fun addCart(cart: Cart) {
        cart.train = this
        carts.add(cart)
    }

    fun addCarts(carts: List<Cart>) {
        carts.forEach { cart ->
            cart.train = this
        }
        this.carts.addAll(carts)
    }

    val totalLength: Float
        get() {
            return carts.sumOf { it.cartLength.toDouble() + it.cartDistance.toDouble() }.toFloat()
        }

    /*
    fun oldsimulate(trackNode: TrackNode, forward: Vector3f, up: Vector3f) {

        val directionOfMotion = Vector(forward.x, forward.y, forward.z).multiply(if (velocity < 0) -1 else 1)

        //Nettokraft alle forces in Newton
        var totalForce = Vector(0.0f, 0.0f, 0.0f)//In Newton

        //Gewichtskraft N
        val gravityForce = Vector(0.0f, -gravity * totalMass, 0.0f)
        totalForce.add(gravityForce)

        val normalForce = totalMass * gravity * Vector(0.0f,1.0f,0.0f).dot(Vector(up.x, up.y, up.z))

        //Rollresistance
        val k = 0.005f//EmpirischerKoeffizient
        val Cr = rollCoefficient + k * velocity.pow(1);
        val rollingResistanceForce = directionOfMotion.clone().multiply(Cr * normalForce * -1.0f)
        if(velocity != 0.0f) totalForce.add(rollingResistanceForce)

        //Airdrag = 0.5 * p * v^2 * A * Cd
        val dragCoefficient = 0.6f//Cd Luftwiderstandsbeiwert
        var v2 = velocity * velocity
        val airDragForce = directionOfMotion.clone().multiply((0.5f * airDensity * v2 * dragCoefficient * crossArea) * -1.0f)
        totalForce.add(airDragForce)

        //proiziert die forces auf die direction of motion
        val forwardForceMagnitude = totalForce.dot(Vector(forward.x, forward.y,forward.z)).toFloat()

       // v = u + at
        applyForce(forwardForceMagnitude)

        //Winkelgeschwindigkeit
        //TODO Zentripetalkraft  m*(v2/r) kurvenradius? wie finde ich ihn raus?

        val segment = trackRide.findSegment(trackNode.id)
        segment.let { segment ->
            segment?.applyForces(this, deltaTime)
        }
    }
 //2 newtonsche gesetz a = F/m
    fun applyForce(newtonForce: Float) {
        val acceleration = newtonForce / totalMass//m/s²
        // v = u + at
        velocity += acceleration * deltaTime
    }
     */



    fun initialize() {
        //var cartPosition = currentPosition
        carts.forEach { cart ->
            cart.world = trackRide.origin.world
            val trackNode = trackNodeAtDistance(startPosition)
            cart.position = trackRide.origin.toVector().add(trackNode.position)
            cart.initialize()
        }
    }

    fun remove() {
        carts.forEach { cart ->
            cart.despawnAttachmentsRecurse()
        }
    }

    fun update() {
        ticksLived++
        currentPosition += velocity * deltaTime

        if (currentPosition < 0) {
            currentPosition += trackRide.totalLength
        } else if (currentPosition >= trackRide.totalLength) {
            currentPosition %= trackRide.totalLength
        }

        var cartPosition = currentPosition

        var netforce = 0.0f
        var totalMass = 0.0f

        carts.forEach { cart ->
            totalMass += cart.mass
            val trackNode = trackNodeAtDistance(cartPosition)
            val front = trackNode.frontVector.toVector3f().normalize()
            val up = trackNode.upVector.toVector3f().normalize()
            val left = trackNode.leftVector.toVector3f().normalize()
            cart.front = front
            cart.up = up
            cart.left = left
            cart.currentPosition = cartPosition

            val trackLength = trackRide.totalLength
            if (currentPosition < 0) {
                currentPosition += trackLength
            } else if (currentPosition >= trackLength) {
                currentPosition %= trackLength
            }

            //position update
            cart.rotationQuaternion = createQuaternionFromVectors(front, left, up)
            cart.position = trackRide.origin.toVector().add(trackNode.position)
            cart.update()

            //force simulation
            val force = cart.simulate(trackNode, front, up)
            netforce += force

            cartPosition -= cart.cartLength + cart.cartDistance
        }

        //2 newtonsche gesetz a = F/m
        val acceleration = netforce / totalMass//m/s²
        // v = u + at
        velocity += acceleration * deltaTime

        sounds()
    }

    fun sounds() {

        if(ticksLived % 2 == 0) {
            val volume = remap(abs(velocity), 2.0f,30.0f,0.0f,1.0f)
            val pitch = remap(abs(velocity), 2.0f,30.0f,0.1f,1.2f)
            //world.playSound(Location(world, position.x, position.y, position.z), Sound.ENTITY_BREEZE_CHARGE, volume, pitch)
        }
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