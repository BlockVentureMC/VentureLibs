package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.structures.impl.Train

class BreakSegment(startId: Int, endId: Int, val minspeed: Float) : TrackSegment(startId, endId) {

    val breakType: BreakType = BreakType.BLOCKBREAK

    override fun applyForces(train: Train, deltaTime: Float) {

        when (breakType) {
            BreakType.BLOCKBREAK -> {
                //  train.velocity = 0.0
            }

            BreakType.TRIMBREAK -> {
                //   train.velocity = liftSpeed
            }
        }
        //if(abs(train.velocity) > liftSpeed) return
        //train.velocity = liftSpeed
    }

    override val type: SegmentTypes
        get() = SegmentTypes.BRAKE


    enum class BreakType(val force: Float) {
        BLOCKBREAK(0.0f), TRIMBREAK(0.0f)
    }

}


