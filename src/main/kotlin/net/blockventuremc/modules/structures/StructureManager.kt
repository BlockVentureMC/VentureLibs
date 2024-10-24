package net.blockventuremc.modules.structures

import net.blockventuremc.VentureLibs
import java.io.Closeable
import java.util.UUID

object StructureManager {

    val structures = mutableMapOf<UUID, CustomEntity>()

    fun update() {

        interval (0, 1) {

            structures.forEach { (uuid, attachment) ->
                attachment.update()
            }

        }

    }

    fun cleanup() {
        structures.forEach { (uuid, attachment) ->
            attachment.despawnAttachmentsRecurse()
        }
        structures.clear()
    }
}

fun interval(delay: Long, period: Long, task: () -> Unit): Closeable {
    val plugin = VentureLibs.instance
    val handler = plugin.server.scheduler.runTaskTimer(plugin, task, delay, period)
    return Closeable {
        handler.cancel()
    }
}