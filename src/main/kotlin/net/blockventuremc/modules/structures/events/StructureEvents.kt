package net.blockventuremc.modules.structures.events

import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class StructureEvents: Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (entity.type != EntityType.INTERACTION) return
        if(!entity.isInsideVehicle) return
        val seat = entity.vehicle

        if(seat?.customName == null || seat.customName != "seat") return
        player.sendMessage("Interact with seat")
        event.isCancelled = true
        seat.addPassenger(player)
    }

}