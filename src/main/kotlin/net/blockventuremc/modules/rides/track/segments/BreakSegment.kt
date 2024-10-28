package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.structures.impl.Train
import kotlin.math.abs

class BreakSegment(startId: Int, endId: Int, var breakType: BreakType = BreakType.BLOCKBREAK, var minspeed: Float) : TrackSegment(startId, endId) {

    constructor(startId: Int, endId: Int) : this(startId, endId, BreakType.BLOCKBREAK, 1.0f)

    override fun applyForces(train: Train, deltaTime: Float) {
        if (abs(train.velocity) < minspeed) return
        train.velocity /= breakType.force
    }

    override val type: SegmentTypes
        get() = SegmentTypes.BRAKE


    enum class BreakType(val force: Float) {
        BLOCKBREAK(1.5f), TRIMBREAK(1.1f)
    }

    override fun getSaveData(): Map<String, String> {
        return mapOf(
            "breakType" to breakType.name,
            "minspeed" to minspeed.toString()
        )
    }

    override fun setSaveData(data: Map<String, String>) {
        breakType = BreakType.entries.find { it.name.equals(data["breakType"], true) } ?: BreakType.BLOCKBREAK
        minspeed = data["minspeed"]?.toFloat() ?: 1.0f
    }
}


