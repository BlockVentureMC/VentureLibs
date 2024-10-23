package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.rides.track.TrackNode
import net.blockventuremc.modules.rides.track.utils.calculateAdjustedSpeed

/**
 * Represents a LiftSegment, which is a class that implements the SegmentFunction interface.
 * The LiftSegment calculates the speed of a segment on a track based on the liftSpeed and adjustmentRate provided.
 *
 * @param liftSpeed The desired speed for the segment.
 */
class LiftSegment(private val liftSpeed: Double = 7.0) : SegmentFunction {

    override val trackDisplay: SegmentTypes = SegmentTypes.LIFT

    override fun calculateSpeed(node: TrackNode, currentSpeed: Double, previousNode: TrackNode?, weatherFactor: Double, maintenanceFactor: Double, trainWeight: Double): Double {
        if (previousNode == null) {
            return currentSpeed
        }

        val adjustedSpeed =  calculateAdjustedSpeed(node, previousNode, trainWeight, currentSpeed, weatherFactor, maintenanceFactor)
        return adjustedSpeed.coerceAtMost(liftSpeed)
    }
}

