package net.blockventuremc.modules.rides.track

import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.modules.general.events.custom.VentureLocation
import net.blockventuremc.modules.general.events.custom.toBukkitLocation
import net.blockventuremc.modules.general.events.custom.toSimpleString
import net.blockventuremc.modules.general.events.custom.toVentureLocation
import net.blockventuremc.utils.FileConfig
import java.io.File

object TrackManager {

    val tracks = mutableMapOf<Int, TrackRide>()

    fun saveTrack(trackId: Int, origin: VentureLocation) {
        val trackConfig = FileConfig("rides/loaded.yml")

        trackConfig["tracks.$trackId.origin"] = origin.toSimpleString()
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
            tracks[trackId] = Nl2Importer(file, trackId, origin?.toVentureLocation()?.toBukkitLocation() ?: return@forEach).import()
            getLogger().info("Loaded track $trackId")
        }
    }

    fun cleanUp() {
        tracks.values.forEach { it.hideTrack() }
    }

}