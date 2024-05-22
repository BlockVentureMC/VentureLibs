package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.extensions.getLogger
import net.blockventuremc.modules.rides.track.TrackNode
import kotlin.time.measureTime

/**
 * Represents a segment of a track.
 *
 * A segment is defined by an ID, a list of track nodes, and a segment function.
 * It represents a specific portion of a track between two nodes.
 *
 * @property id The ID of the segment.
 * @property nodes The list of track nodes that define the segment.
 * @property function The segment function used to calculate the speed of the segment.
 */
data class TrackSegment(
    val id: Int,
    val nodes: List<TrackNode>,
    val function: SegmentFunction
) {

    /**
     * Calculates the speed of each track node based on the provided factors and initial speed.
     *
     * This method iterates through a list of track nodes and calculates the speed of each node based on the weather factor,
     * maintenance factor, train weight, and initial speed. The calculated speed is then assigned to the 'calculatedSpeed'
     * property of each track node.
     *
     * @param initialSpeed The initial speed of the train.
     * @param weatherFactor The factor representing the weather conditions affecting the train speed.
     * @param maintenanceFactor The factor representing the maintenance conditions affecting the train speed.
     * @param trainWeight The weight of the train.
     *
     * @return The list of track nodes with the calculated speed assigned to each node.
     */
    fun calculateSpeed(initialSpeed: Double, weatherFactor: Double, maintenanceFactor: Double, trainWeight: Double) {
        var currentSpeed = initialSpeed
        var previousNode: TrackNode? = null

        val time = measureTime {
            for (node in nodes) {
                currentSpeed = function.calculateSpeed(node, currentSpeed, previousNode, weatherFactor, maintenanceFactor, trainWeight)
                node.calculatedSpeed = currentSpeed
                previousNode = node
            }
        }
        getLogger().info("Calculated speeds for segment $id in $time")
    }


    val length: Double
        get() {
            var length = 0.0
            for (i in 0 until nodes.size - 1) {
                val node1 = nodes[i]
                val node2 = nodes[i + 1]
                length += node1.position.distance(node2.position)
            }
            return length
        }

    val averageSpeed: Double
        get() {
            var totalSpeed = 0.0
            for (node in nodes) {
                totalSpeed += node.calculatedSpeed
            }
            return totalSpeed / nodes.size
        }
}
