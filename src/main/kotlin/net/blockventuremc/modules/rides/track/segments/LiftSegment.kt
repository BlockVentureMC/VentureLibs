package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.structures.impl.Train
import kotlin.math.abs

/**
 * Represents a LiftSegment, which is a class that implements the SegmentFunction interface.
 * The LiftSegment calculates the speed of a segment on a track based on the liftSpeed and adjustmentRate provided.
 *
 * @param liftSpeed The desired speed for the segment.
 */
class LiftSegment(startId: Int, endId: Int, var liftSpeed: Float) : TrackSegment(startId, endId) {

    constructor(startId: Int, endId: Int) : this(startId, endId, 1.0f)

    override fun applyForces(train: Train, deltaTime: Float) {
        if (abs(train.velocity) > liftSpeed) return
        train.velocity = liftSpeed
    }

    override val type: SegmentTypes
        get() = SegmentTypes.LIFT

    override fun getSaveData(): Map<String, String> {
        return mapOf(
            "liftSpeed" to liftSpeed.toString()
        )
    }

    override fun setSaveData(data: Map<String, String>) {
        liftSpeed = data["liftSpeed"]?.toFloat() ?: 1.0f
    }
}


