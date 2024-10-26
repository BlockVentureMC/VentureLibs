package net.blockventuremc.modules.rides.track.segments

import net.blockventuremc.modules.rides.track.TrackNode
import net.blockventuremc.modules.rides.track.utils.calculateAdjustedSpeed
import net.blockventuremc.modules.structures.Train
import kotlin.math.abs

class BreakSegment(startId: Int, endId: Int, val minspeed: Float) : TrackSegment(startId, endId) {

    val breakType: BreakType = BreakType.BLOCKBREAK

    override fun applyForces(train: Train) {

        when(breakType) {
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


