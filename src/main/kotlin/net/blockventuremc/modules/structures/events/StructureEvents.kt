package net.blockventuremc.modules.structures.events

import me.m56738.smoothcoasters.api.event.PlayerSmoothCoastersHandshakeEvent
import net.blockventuremc.VentureLibs
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.vehicle.VehicleExitEvent

class StructureEvents: Listener {

    @EventHandler
    fun onPlayerSmoothCoastersHandshakeEvent(event: PlayerSmoothCoastersHandshakeEvent) {
        event.player.sendMessage("Yippi :3")
    }

    @EventHandler
    fun onEntityDismount(event: EntityDismountEvent) {
        val passenger = event.entity
        if(passenger !is Player) return
        VentureLibs.instance.smoothCoastersAPI.resetRotation(VentureLibs.instance.networkInterface, passenger)
    }

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        event.player.leaveVehicle()
    }

    @EventHandler
    fun onInteract(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (entity.type != EntityType.INTERACTION) return
        if(!entity.isInsideVehicle) return
        val seat = entity.vehicle

        if(seat?.customName == null || seat.customName != "seat") return

        if(seat.passengers.size > 1)  {
            player.sendMessage("Kuscheln verboten! Nimm dir einen eigenen Platz :3")
            return
        }

        event.isCancelled = true
        seat.addPassenger(player)
    }

}