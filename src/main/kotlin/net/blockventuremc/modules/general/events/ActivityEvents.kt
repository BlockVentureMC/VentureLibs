package net.blockventuremc.modules.general.events

import de.themeparkcraft.audioserver.minecraft.AudioServer
import io.papermc.paper.event.player.AsyncChatEvent
import net.blockventuremc.extensions.toBlockUser
import net.blockventuremc.modules.general.events.custom.*
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent

class ActivityEvents : Listener {

    /**
     * Handles the movement of a player from one location to another.
     *
     * If the player moves from one area to another, the playerLeavesArea and playerEntersArea functions are called.
     * If the player moves to a different block (x or z coordinate changes), the player's activity status is updated.
     * The AudioServer is notified about the player's movement.
     *
     * @param from The initial location of the player.
     * @param to The final location of the player.
     * @param player The player who moved.
     */
    private fun handleMovement(from: Location, to: Location, player: Player) {
        val fromArea = from.getArea()
        val toArea = to.getArea()

        if (fromArea != null && toArea != null && fromArea.name != toArea.name) {
            fromArea.onLeave(player)
            toArea.onEnter(player)
        } else if (fromArea != null && toArea == null) {
            fromArea.onLeave(player)
        } else if (fromArea == null && toArea != null) {
            toArea.onEnter(player)
        }

        if (from.blockX != to.blockX || from.blockZ != to.blockZ) {
            player.toBlockUser().addActivity(AFKChangeEvent.Cause.MOVE)
        }

        AudioServer.sendPlayerUpdate(player)
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
        player.toBlockUser().addActivity(AFKChangeEvent.Cause.CHAT)
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        player.toBlockUser().addActivity(AFKChangeEvent.Cause.CHAT)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        player.toBlockUser().addActivity(AFKChangeEvent.Cause.INTERACT)
    }

}