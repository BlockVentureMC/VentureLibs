package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.rides.track.TrackNode

/**
 * Represents a segment of acceleration on a track.
 *
 * This class implements the `SegmentFunction` interface and provides a method to calculate the speed of a train
 * segment based on the acceleration and various factors such as weather, maintenance, and train weight.
 *
 * @param acceleration The acceleration value for the segment.
 */
class AccelerationSegment(private val acceleration: Double) : SegmentFunction {
    override fun calculateSpeed(node: TrackNode, currentSpeed: Double, previousNode: TrackNode?, weatherFactor: Double, maintenanceFactor: Double, trainWeight: Double): Double {
        val newSpeed = currentSpeed + acceleration * weatherFactor * maintenanceFactor
        val frictionLoss = 0.01 * trainWeight
        return (newSpeed - frictionLoss).coerceAtLeast(0.0)
    }
}
