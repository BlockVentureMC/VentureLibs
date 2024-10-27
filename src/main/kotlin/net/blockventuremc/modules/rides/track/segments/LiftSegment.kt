package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.structures.impl.Train
import kotlin.math.abs

/**
 * Represents a LiftSegment, which is a class that implements the SegmentFunction interface.
 * The LiftSegment calculates the speed of a segment on a track based on the liftSpeed and adjustmentRate provided.
 *
 * @param liftSpeed The desired speed for the segment.
 */
class LiftSegment(startId: Int, endId: Int, val liftSpeed: Float) : TrackSegment(startId, endId) {

    override fun applyForces(train: Train, deltaTime: Float) {
        if (abs(train.velocity) > liftSpeed) return
        train.velocity = liftSpeed
    }

    override val type: SegmentTypes
        get() = SegmentTypes.LIFT

}


