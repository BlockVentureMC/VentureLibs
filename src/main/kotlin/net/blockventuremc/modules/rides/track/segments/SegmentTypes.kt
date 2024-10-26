package net.blockventuremc.modules.rides.track.segments

import org.bukkit.Material
import kotlin.reflect.KClass

enum class SegmentTypes(val material: Material, val segmentType: KClass<out TrackSegment>) {

    NORMAL(Material.SPRUCE_TRAPDOOR, TrackSegment::class),
    LIFT(Material.OAK_TRAPDOOR, LiftSegment::class),
    LAUNCH(Material.ACACIA_TRAPDOOR, AccelerationSegment::class),
    BRAKE(Material.BIRCH_TRAPDOOR, TrackSegment::class),
    STATION(Material.DARK_OAK_TRAPDOOR, TrackSegment::class),
    ACCELERATION(Material.CRIMSON_TRAPDOOR, AccelerationSegment::class),

    HIGHLIGHTED(Material.CRIMSON_TRAPDOOR, TrackSegment::class),

}