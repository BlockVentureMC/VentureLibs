package net.blockventuremc.modules.structures

import net.blockventuremc.extensions.createQuaternionFromVectors
import net.blockventuremc.modules.rides.track.TrackRide
import org.bukkit.World
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

class Train(name: String, val trackRide: TrackRide, world: World, position: Vector, rotation: Vector): CustomEntity(name, world, position, rotation) {

    init {
    }
    var length = 0.0
    var nextNode = 0
    var rotationQuaternion = Quaternionf()

    override fun update() {

        nextNode++
        nextNode %= trackRide.nodes.size -1

        val trackNode = trackRide.nodes[nextNode]

        var targetPosition = trackRide.origin.toVector().add(trackNode.position)
        position = targetPosition

        val front = trackNode.frontVector.toVector3f().normalize()
        val up = trackNode.upVector.toVector3f().normalize()
        val left = trackNode.leftVector.toVector3f().normalize()

        rotationQuaternion = createQuaternionFromVectors(front, left, up)
        super.update()
    }

    override val localTransform: Matrix4f
        get() {
            var matrix = Matrix4f().translate(localPosition.toVector3f())
            matrix.rotate(rotationQuaternion)
            return matrix
        }

}