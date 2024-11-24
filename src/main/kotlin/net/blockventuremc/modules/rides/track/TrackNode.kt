package net.blockventuremc.modules.rides.track

import net.blockventuremc.extensions.createQuaternionFromVectors
import net.blockventuremc.modules.rides.track.segments.SegmentTypes
import net.blockventuremc.modules.structures.StructureType
import net.blockventuremc.modules.structures.setCustomType
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BlockVector
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*

data class TrackNode(
    val id: Int = -1,

    // The position of the node
    val posX: Double = 0.0,
    val posY: Double = 0.0,
    val posZ: Double = 0.0,

    // The front vector of the node
    val frontX: Float = 0.0f,
    val frontY: Float = 0.0f,
    val frontZ: Float = 0.0f,

    // The left vector of the node
    val leftX: Float = 0.0f,
    val leftY: Float = 0.0f,
    val leftZ: Float = 0.0f,

    // The up vector of the node
    val upX: Float = 0.0f,
    val upY: Float = 0.0f,
    val upZ: Float = 0.0f,

) {

    val position: BlockVector = BlockVector(posX, posY, posZ)

    val frontVector: BlockVector = BlockVector(frontX, frontY, frontZ)

    val leftVector: BlockVector = BlockVector(leftX, leftY, leftZ)

    val upVector: BlockVector = BlockVector(upX, upY, upZ)

    fun displayInWord(origin: Location): UUID {
        val loc = Location(origin.world, origin.x + posX, origin.y + posY, origin.z + posZ)

        if (!loc.chunk.isLoaded) {
            loc.chunk.load()
            loc.chunk.isForceLoaded = true
        }

        val originVec = Vector3f(0f, 0f, 0f)
        // Calculate the quaternion for the rotation
        val front = frontVector.toVector3f().normalize()
        val up = upVector.toVector3f().normalize()
        val left = leftVector.toVector3f().normalize()

        // Create quaternion from vectors
        val quaternion = createQuaternionFromVectors(front, left, up)

        // Display the item with the calculated orientation
        val itemDisplayEnt = loc.world.spawnEntity(loc, EntityType.ITEM_DISPLAY) as ItemDisplay
        itemDisplayEnt.setItemStack(ItemStack(SegmentTypes.NORMAL.material))
        itemDisplayEnt.itemDisplayTransform = ItemDisplayTransform.NONE
        itemDisplayEnt.viewRange = 16.0f
        itemDisplayEnt.transformation = Transformation(originVec, quaternion, Vector3f(1f, 1f, 1f), Quaternionf())
        itemDisplayEnt.setCustomType(StructureType.TRACK)

        return itemDisplayEnt.uniqueId
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 11 * hash + Objects.hashCode(this.position)
        hash = 11 * hash + Objects.hashCode(this.frontVector)
        hash = 11 * hash + Objects.hashCode(this.leftVector)
        hash = 11 * hash + Objects.hashCode(this.upVector)
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this.javaClass != other.javaClass) return false
        val otherNode = other as TrackNode
        return this.position == otherNode.position && this.frontVector == otherNode.frontVector && this.leftVector == otherNode.leftVector && this.upVector == otherNode.upVector
    }
}
