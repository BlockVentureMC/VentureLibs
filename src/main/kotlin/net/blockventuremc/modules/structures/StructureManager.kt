package net.blockventuremc.modules.structures

import net.blockventuremc.VentureLibs
import net.blockventuremc.consts.NAMESPACE_CUSTOMENTITY_IDENTIFIER
import net.blockventuremc.modules.`fun`.baloon.Balloon
import net.blockventuremc.modules.structures.impl.Train
import net.blockventuremc.modules.structures.vehicle.CustomVehicle
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.io.Closeable
import java.util.*

object StructureManager {

    val trains = mutableMapOf<UUID, Train>()
    val structures = mutableMapOf<UUID, RootAttachment>()
    val balloons = mutableMapOf<Player, Balloon>()
    val vehicles = mutableMapOf<UUID, CustomVehicle>()

    fun update() {

        //general structures and trains
        interval(0, 1) {
            trains.forEach { (uuid, train) ->
                train.update()
            }

            structures.forEach { (uuid, attachment) ->
                attachment.update()
            }

        }

        //vehicles and balloons
        interval(0, 1) {
            balloons.forEach { (player, balloon) ->
                balloon.update()
            }

            vehicles.values.forEach { vehicle ->
                vehicle.update()
            }
        }

    }

    fun cleanup() {
        structures.forEach { (uuid, attachment) ->
            attachment.despawnAttachmentsRecurse()
        }
        balloons.forEach { (player, balloon) ->
            balloon.remove()
        }
        trains.forEach { (uuid, train) ->
            train.remove()
        }
        vehicles.forEach { (uuid, vehicle) ->
            vehicle.despawnAttachmentsRecurse()
        }
        balloons.clear()
        structures.clear()
        trains.clear()
        vehicles.clear()
    }

    fun cleanUpWorld() {
        Bukkit.getWorlds().forEach { world ->
            world.loadedChunks.forEach { chunk ->
                chunk.entities.forEach { entity ->
                    if(entity.persistentDataContainer.keys.contains(NAMESPACE_CUSTOMENTITY_IDENTIFIER)) entity.remove()
                }
            }
        }
    }

}

fun interval(delay: Long, period: Long, task: () -> Unit): Closeable {
    val plugin = VentureLibs.instance
    val handler = plugin.server.scheduler.runTaskTimer(plugin, task, delay, period)
    return Closeable {
        handler.cancel()
    }
}

enum class StructureType {
    TRAIN,
    BALLOON,
    TRACK,
    GENERIC,
    VEHICLE,
    SEAT,
    TITLE
}

fun Entity.setCustomType(type: StructureType, value: String = "") {
    this.persistentDataContainer[NAMESPACE_CUSTOMENTITY_IDENTIFIER, PersistentDataType.STRING] = value
}