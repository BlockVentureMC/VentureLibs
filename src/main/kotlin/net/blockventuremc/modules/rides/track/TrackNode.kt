package net.blockventuremc.modules.rides.track

import org.bukkit.Location
import org.bukkit.Material
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

    /**
     * Represents a position in three-dimensional space.
     *
     * This class provides a convenient way to represent a position using three coordinates - posX, posY, and posZ.
     * Instances of this class are generally used to represent positions in a block-based environment.
     *
     * @property posX The X-coordinate of the position.
     * @property posY The Y-coordinate of the position.
     * @property posZ The Z-coordinate of the position.
     */
    val position: BlockVector
        get() = BlockVector(posX, posY, posZ)

    /**
     * Represents the front vector.
     *
     * This vector defines the direction in which an object is facing.
     *
     * @property frontVector The front vector.
     */
    val frontVector: BlockVector
        get() = BlockVector(frontX, frontY, frontZ)

    /**
     * Represents a left vector in a Cartesian coordinate system.
     * The vector is represented as a BlockVector.
     */
    val leftVector: BlockVector
        get() = BlockVector(leftX, leftY, leftZ)

    /**
     * Represents an upward vector in three-dimensional space.
     * This vector is used to define the direction of the "up" in relation to a reference point.
     *
     * @property upX The x-coordinate value of the upward vector.
     * @property upY The y-coordinate value of the upward vector.
     * @property upZ The z-coordinate value of the upward vector.
     */
    val upVector: BlockVector
        get() = BlockVector(upX, upY, upZ)


    /**
     * Displays a node in the given world as itemDisplayEntities.
     *
     * @param world The world in which the node should be displayed.
     *
     * @return A triple containing the unique IDs of the front vector entity, left vector entity, and up vector entity.
     */
    fun displayInWord(origin: Location): Triple<UUID, UUID, UUID> {
        val loc = Location(origin.world, origin.x + posX, origin.y + posY, origin.z + posZ)

        if (!loc.chunk.isLoaded) {
            loc.chunk.load()
            loc.chunk.isForceLoaded = true
        }

        // Zero rotation quaternion
        val zeroRotation = Quaternionf(0f, 0f, 0f, 1f)

        // Display the node in the world as itemDisplayEntites
        // We use the location of the node as base position
        // The vectors are displayed as lines from the base position with the items: stick, blaze rod, and lightning rod

        // Display the front vector
        val frontVectorEnt = loc.world.spawnEntity(loc, EntityType.ITEM_DISPLAY) as ItemDisplay
        frontVectorEnt.itemStack = ItemStack(Material.STICK)
        frontVectorEnt.itemDisplayTransform = ItemDisplayTransform.NONE
        frontVectorEnt.transformation = Transformation(frontVector.toVector3f(), zeroRotation, Vector3f(frontVector.toVector3f().length(), 0f, 0f), zeroRotation)

        // Display the left vector
        val leftVectorEnt = loc.world.spawnEntity(loc, EntityType.ITEM_DISPLAY) as ItemDisplay
        leftVectorEnt.itemStack = ItemStack(Material.BLAZE_ROD)
        leftVectorEnt.itemDisplayTransform = ItemDisplayTransform.NONE
        leftVectorEnt.transformation = Transformation(leftVector.toVector3f(), zeroRotation, Vector3f(leftVector.toVector3f().length(), 0f, 0f), zeroRotation)


        // Display the up vector
        val upVectorEnt = loc.world.spawnEntity(loc, EntityType.ITEM_DISPLAY) as ItemDisplay
        upVectorEnt.itemStack = ItemStack(Material.LIGHTNING_ROD)
        upVectorEnt.itemDisplayTransform = ItemDisplayTransform.NONE
        upVectorEnt.transformation = Transformation(upVector.toVector3f(), zeroRotation,  Vector3f(upVector.toVector3f().length(), 0f, 0f), zeroRotation)

        return Triple(frontVectorEnt.uniqueId, leftVectorEnt.uniqueId, upVectorEnt.uniqueId)
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
