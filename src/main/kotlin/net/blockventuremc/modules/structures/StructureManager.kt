package net.blockventuremc.modules.structures

import net.blockventuremc.VentureLibs
import net.blockventuremc.consts.NAMESPACE_CUSTOMENTITY_IDENTIFIER
import net.blockventuremc.modules.`fun`.baloon.Balloon
import net.blockventuremc.modules.structures.impl.Train
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

    fun update() {

        interval(0, 1) {

            trains.forEach { (uuid, train) ->
                train.update()
            }

            structures.forEach { (uuid, attachment) ->
                attachment.update()
            }

            balloons.forEach { (player, balloon) ->
                balloon.update()
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
        trains.forEach { (player, train) ->
            train.remove()
        }
        balloons.clear()
        structures.clear()
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
    SEAT
}

fun Entity.setCustomType(type: StructureType, value: String = "") {
    this.persistentDataContainer.set(NAMESPACE_CUSTOMENTITY_IDENTIFIER, PersistentDataType.STRING, value)
}