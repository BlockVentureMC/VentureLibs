package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.structures.impl.Train
import kotlin.math.min

/**
 * Represents a segment of acceleration on a track.
 *
 * This class implements the `SegmentFunction` interface and provides a method to calculate the speed of a train
 * segment based on the acceleration and various factors such as weather, maintenance, and train weight.
 *
 * @param fixedSpeed The speed value for the segment.
 */
class AccelerationSegment(startId: Int, endId: Int, var fixedSpeed: Float) : TrackSegment(startId, endId) {

    constructor(startId: Int, endId: Int) : this(startId, endId, 1.0f)

    override fun applyForces(train: Train, deltaTime: Float) {
        train.velocity = min(train.velocity + fixedSpeed * deltaTime, fixedSpeed)
    }


    override val type: SegmentTypes
        get() = SegmentTypes.ACCELERATION


    override fun getSaveData(): Map<String, String> {
        return mapOf(
            "fixedSpeed" to fixedSpeed.toString()
        )
    }

    override fun setSaveData(data: Map<String, String>) {
        fixedSpeed = data["fixedSpeed"]?.toFloat() ?: 1.0f
    }
}
