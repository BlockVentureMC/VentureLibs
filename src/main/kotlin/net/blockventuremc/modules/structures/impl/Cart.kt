package net.blockventuremc.modules.structures.impl

import net.blockventuremc.extensions.remap
import net.blockventuremc.modules.rides.track.TrackNode
import net.blockventuremc.modules.rides.track.TrackRide
import net.blockventuremc.modules.structures.RootAttachment
import net.blockventuremc.modules.structures.airDensity
import net.blockventuremc.modules.structures.deltaTime
import net.blockventuremc.modules.structures.gravity
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.sign

class Cart(val cartLength: Float, val cartDistance: Float) :
    RootAttachment("cart") {

    lateinit var train: Train

    var rotationQuaternion = Quaternionf()

    var mass = 700.0f //masse kilogramm
    val rollCoefficient = 0.002f * 4.0f  // Rollreibungskoeffizient (angenommener Wert) abhängig von wagen und schiene
    val crossArea = 1.5f//m2 //Querschnittsfläche 0.5 bis 1,5 in Quadratmeter damit ist die Stirnfläche gemeint

    var front = Vector3f()
    var up = Vector3f()
    var left = Vector3f()

    fun simulate(trackNode: TrackNode, dirMotion: Int, velocity: Float): Float {
        val directionOfMotion = Vector(front.x, front.y, front.z).multiply(dirMotion)

        //Nettokraft alle forces in Newton
        var totalForce = Vector(0.0f, 0.0f, 0.0f)//In Newton

        //Gewichtskraft N
        val gravityForce = Vector(0.0f, -gravity * mass, 0.0f)
        totalForce.add(gravityForce)

        val normalForce = mass * gravity * Vector(0.0f,1.0f,0.0f).dot(Vector(up.x, up.y, up.z))

        //Rollresistance
        val k = 0.005f//EmpirischerKoeffizient
        val Cr = rollCoefficient + k * velocity
        val rollingResistanceForce = directionOfMotion.clone().multiply(Cr * normalForce * -1.0f)
        if(velocity != 0.0f) totalForce.add(rollingResistanceForce)

        //Airdrag = 0.5 * p * v^2 * A * Cd
        val dragCoefficient = 0.6f//Cd Luftwiderstandsbeiwert
        var v2 = velocity * velocity
        val airDragForce = directionOfMotion.clone().multiply((0.5f * airDensity * v2 * dragCoefficient * crossArea) * -1.0f)
        totalForce.add(airDragForce)

        //proiziert die forces auf die direction of motion
        val forwardForceMagnitude = totalForce.dot(Vector(front.x, front.y,front.z)).toFloat()
        val acceleration = forwardForceMagnitude / mass//m/s²
        // v = u + at
        return acceleration * deltaTime
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

    fun sounds() {
            if (train.ticksLived % 2 == 0) {
                val volume = remap(abs(train.velocity), 2.0f, 30.0f, 0.0f, 1.0f)
                val pitch = remap(abs(train.velocity), 2.0f, 30.0f, 0.1f, 1.2f)
                //world.playSound(Location(world, position.x, position.y, position.z), Sound.ENTITY_BREEZE_CHARGE, volume, pitch)

        }
    }
    val track: TrackRide
        get() = train.trackRide

    override val localTransform: Matrix4f
        get() {
            var matrix = Matrix4f().translate(localPosition.toVector3f())
            matrix.rotate(rotationQuaternion)
            return matrix
        }

}