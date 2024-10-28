package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.structures.impl.Train

/**
 * Represents a segment of acceleration on a track.
 *
 * This class implements the `SegmentFunction` interface and provides a method to calculate the speed of a train
 * segment based on the acceleration and various factors such as weather, maintenance, and train weight.
 *
 * @param acceleration The acceleration value for the segment.
 */
class LaunchSegment(startId: Int, endId: Int, var acceleration: Float) : TrackSegment(startId, endId) {

    constructor(startId: Int, endId: Int) : this(startId, endId, 1.0f)

    override fun applyForces(train: Train, deltaTime: Float) {
        train.velocity += acceleration * deltaTime
    }


    override val type: SegmentTypes
        get() = SegmentTypes.LAUNCH

    override fun getSaveData(): Map<String, String> {
        return mapOf(
            "acceleration" to acceleration.toString()
        )
    }

    override fun setSaveData(data: Map<String, String>) {
        acceleration = data["acceleration"]?.toFloat() ?: 1.0f
    }
}
