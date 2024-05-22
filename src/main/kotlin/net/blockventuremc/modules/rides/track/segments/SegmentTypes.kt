package net.blockventuremc.modules.rides.track.segments

import org.bukkit.Material
import kotlin.reflect.KClass

enum class SegmentTypes(val material: Material, val segmentType: KClass<out SegmentFunction>) {

    NORMAL(Material.SPRUCE_TRAPDOOR, NormalSegment::class),
    LIFT(Material.OAK_TRAPDOOR, LiftSegment::class),
    LAUNCH(Material.ACACIA_TRAPDOOR, AccelerationSegment::class),
    BRAKE(Material.BIRCH_TRAPDOOR, NormalSegment::class),
    STATION(Material.DARK_OAK_TRAPDOOR, NormalSegment::class),

    HIGHLIGHTED(Material.CRIMSON_TRAPDOOR, NormalSegment::class),

}