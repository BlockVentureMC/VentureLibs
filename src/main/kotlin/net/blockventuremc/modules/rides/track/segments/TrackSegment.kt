package net.blockventuremc.modules.rides.track.segments

import kotlinx.serialization.Serializable
import net.blockventuremc.modules.structures.Train
import org.joml.Matrix4f

@Serializable
open class TrackSegment(
    val startId: Int, val endId: Int
) {

    open fun applyForces(train: Train) {}

    open val type: SegmentTypes
        get() = SegmentTypes.NORMAL
}
