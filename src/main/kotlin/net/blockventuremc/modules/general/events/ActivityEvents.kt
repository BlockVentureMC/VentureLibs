package net.blockventuremc.modules.general.events

import io.papermc.paper.event.player.AsyncChatEvent
import net.blockventuremc.extensions.toDatabaseUser
import net.blockventuremc.modules.general.events.custom.AFKChangeEvent
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent

class ActivityEvents : Listener {

    private fun handleMovement(from: Location, to: Location, player: Player) {
        if (from.blockX != to.blockX || from.blockZ != to.blockZ) {
            player.toDatabaseUser().addActivity(AFKChangeEvent.Cause.MOVE)
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        val from = event.from
        val to = event.to
        handleMovement(from, to, player)
    }


    @EventHandler
    fun onTeleportIntoArea(event: PlayerTeleportEvent) {
        val player = event.player
        val from = event.from
        val to = event.to
        handleMovement(from, to, player)
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        player.toDatabaseUser().addActivity(AFKChangeEvent.Cause.CHAT)
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        player.toDatabaseUser().addActivity(AFKChangeEvent.Cause.CHAT)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        player.toDatabaseUser().addActivity(AFKChangeEvent.Cause.INTERACT)
    }

}