package net.blockventuremc.modules.rides.track

import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.modules.general.events.custom.VentureLocation
import net.blockventuremc.modules.general.events.custom.toBukkitLocation
import net.blockventuremc.modules.general.events.custom.toSimpleString
import net.blockventuremc.modules.general.events.custom.toVentureLocation
import net.blockventuremc.modules.rides.track.segments.SegmentTypes
import net.blockventuremc.utils.FileConfig
import java.io.File
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

object TrackManager {

    val tracks = mutableMapOf<Int, TrackRide>()

    fun saveTrack(trackRide: TrackRide) {
        val trackConfig = FileConfig("rides/loaded.yml")
        val trackId = trackRide.id

        trackConfig["tracks.$trackId.origin"] = trackRide.origin.toVentureLocation().toSimpleString()

        trackConfig["tracks.$trackId.segments"] = null
        for (segment in trackRide.trackSegments) {
            val section = trackConfig.getConfigurationSection("tracks.$trackId.segments.${segment.key.first}-${segment.key.second}") ?: trackConfig.createSection("tracks.$trackId.segments.${segment.key.first}-${segment.key.second}")
            section["type"] = segment.value.type.name

            for (key in segment.value.getSaveData()) {
                section[key.key] = key.value
            }
        }

        trackConfig.saveConfig()
        getLogger().info("Saved track $trackId")
    }

    fun loadTracks() {
        val trackConfig = FileConfig("rides/loaded.yml")
        trackConfig.getConfigurationSection("tracks")?.getKeys(false)?.forEach {
            val trackId = it.toInt()
            val origin = trackConfig.getString("tracks.$trackId.origin")

            val file = File(VentureLibs.instance.dataFolder, "rides/track/$trackId.nl2").also { it.parentFile.mkdirs() }
            if (!file.exists()) {
                getLogger().info("Track file for TrackID $trackId does not exist. Please upload the track file to ${file.path}.")
                return
            }
            val trackRide = Nl2Importer(file, trackId, origin?.toVentureLocation()?.toBukkitLocation() ?: return@forEach).import()
            tracks[trackId] = trackRide


            val segments = trackConfig.getConfigurationSection("tracks.$trackId.segments")
            if (segments != null) {
                for (key in segments.getKeys(false)) {
                    val section = segments.getConfigurationSection(key) ?: continue

                    val start = key.split("-")[0].toInt()
                    val end = key.split("-")[1].toInt()

                    val type = SegmentTypes.entries.find { it.name.equals(section.getString("type"), true) } ?: continue
                    val segment = type.segmentType.constructors.find { it.parameters.size == 2 }?.call(start, end) ?: continue

                    val data = section.getValues(false).map { it.key to it.value as String }.toMap()
                    segment.setSaveData(data)

                    trackRide.trackSegments[Pair(start, end)] = segment
                }
                trackRide.recalculateSegments()
            }

            getLogger().info("Loaded track $trackId")
        }
    }

    fun cleanUp() {
        tracks.values.forEach { it.hideTrack() }
    }

}