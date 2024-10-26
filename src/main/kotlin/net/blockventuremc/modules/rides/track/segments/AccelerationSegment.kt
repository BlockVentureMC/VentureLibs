package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.rides.track.TrackNode
import net.blockventuremc.modules.structures.Train

/**
 * Represents a segment of acceleration on a track.
 *
 * This class implements the `SegmentFunction` interface and provides a method to calculate the speed of a train
 * segment based on the acceleration and various factors such as weather, maintenance, and train weight.
 *
 * @param acceleration The acceleration value for the segment.
 */
class AccelerationSegment(startId: Int,endId: Int, val acceleration: Float) : TrackSegment(startId, endId) {

    override fun applyForces(train: Train, deltaTime: Float) {
        train.velocity += acceleration
    }


    override val type: SegmentTypes
        get() = SegmentTypes.ACCELERATION

}
