package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.structures.impl.Train

open class StationSegment(startId: Int, endId: Int, var stationSpeed: Float) : TrackSegment(startId, endId) {

    constructor(startId: Int, endId: Int) : this(startId, endId, 10.0f)

    override fun applyForces(train: Train, deltaTime: Float) {
        train.velocity = stationSpeed
    }

    override val type: SegmentTypes
        get() = SegmentTypes.STATION

    override fun getSaveData(): Map<String, String> {
        return mapOf(
            "stationSpeed" to stationSpeed.toString()
        )
    }

    override fun setSaveData(data: Map<String, String>) {
        stationSpeed = data["stationSpeed"]?.toFloat() ?: 1.0f
    }
}