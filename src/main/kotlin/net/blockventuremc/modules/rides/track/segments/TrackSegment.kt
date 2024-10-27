package net.blockventuremc.modules.rides.track.segments

import kotlinx.serialization.Serializable
import net.blockventuremc.modules.structures.impl.Train

@Serializable
open class TrackSegment(
    val startId: Int, val endId: Int
) {

    open fun applyForces(train: Train, deltaTime: Float) {}

    open val type: SegmentTypes
        get() = SegmentTypes.NORMAL
}
