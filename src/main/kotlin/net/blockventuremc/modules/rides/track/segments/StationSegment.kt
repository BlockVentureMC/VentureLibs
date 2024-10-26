package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.structures.Train

open class StationSegment(startId: Int,endId: Int, val stationSpeed: Float) : TrackSegment(startId, endId) {

    override fun applyForces(train: Train) {
        train.velocity = stationSpeed
    }

    override val type: SegmentTypes
        get() = SegmentTypes.STATION

}