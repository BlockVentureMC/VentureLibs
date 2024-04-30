package net.blockventuremc.modules.audioserver

import de.themeparkcraft.audioserver.minecraft.AudioServer
import dev.fruxz.ascend.tool.time.calendar.Calendar
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

class MovementListener: Listener {

    private val lastMovements = mutableMapOf<UUID, Calendar>()


    @EventHandler
    fun onMovementRegistered(event: PlayerMoveEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val lastMovement = lastMovements[uuid]
        val now = Calendar.now()

        if (lastMovement != null && lastMovement + 50.milliseconds > now) return

        lastMovements[uuid] = now
        AudioServer.sendPlayerUpdate(player)
    }
}