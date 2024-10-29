package net.blockventuremc.modules.structures.events

import me.m56738.smoothcoasters.api.event.PlayerSmoothCoastersHandshakeEvent
import net.blockventuremc.VentureLibs
import net.blockventuremc.modules.structures.StructureManager
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerQuitEvent

class StructureEvents : Listener {

    @EventHandler
    fun onPlayerSmoothCoastersHandshakeEvent(event: PlayerSmoothCoastersHandshakeEvent) {
        event.player.sendMessage("Yippi :3")
    }

    @EventHandler
    fun onEntityDismount(event: EntityDismountEvent) {
        val passenger = event.entity
        if (passenger !is Player) return
        VentureLibs.instance.smoothCoastersAPI.resetRotation(VentureLibs.instance.networkInterface, passenger)

        val trainExitEvent = TrainExitEvent(passenger, event.dismounted)
        Bukkit.getPluginManager().callEvent(trainExitEvent)
    }

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        val player = event.player

        if(player.isInsideVehicle) {
            val trainExitEvent = TrainExitEvent(player, player.vehicle!!)
            Bukkit.getPluginManager().callEvent(trainExitEvent)
            player.leaveVehicle()
        }

        StructureManager.balloons[player]?.let { balloon ->
            balloon.remove()
            StructureManager.balloons.remove(player)
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (entity.type != EntityType.INTERACTION) return
        if (!entity.isInsideVehicle) return
        val seat = entity.vehicle

        if (seat?.customName == null || seat.customName != "seat") return

        if (seat.passengers.size > 1) {
            player.sendMessage("Kuscheln verboten! Nimm dir einen eigenen Platz :3")
            return
        }

        event.isCancelled = true
        seat.addPassenger(player)

        val trainEnterEvent = TrainEnterEvent(player, seat)
        Bukkit.getPluginManager().callEvent(trainEnterEvent)
    }

}