package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.rides.track.TrackNode

/**
 * Represents a function interface for calculating the speed of a segment on a track.
 */
fun interface SegmentFunction {
    fun calculateSpeed(node: TrackNode, currentSpeed: Double, previousNode: TrackNode?, weatherFactor: Double, maintenanceFactor: Double, trainWeight: Double): Double
}
