package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.rides.track.TrackNode
import net.blockventuremc.modules.rides.track.utils.calculateAdjustedSpeed

/**
 *
 * Represents a class that implements the `SegmentFunction` interface for calculating the speed of a segment on a track.
 *
 */
class NormalSegment : SegmentFunction {
    override fun calculateSpeed(node: TrackNode, currentSpeed: Double, previousNode: TrackNode?, weatherFactor: Double, maintenanceFactor: Double, trainWeight: Double): Double {
        if (previousNode == null) {
            return currentSpeed
        }

        return calculateAdjustedSpeed(node, previousNode, trainWeight, currentSpeed, weatherFactor, maintenanceFactor)
    }


}
