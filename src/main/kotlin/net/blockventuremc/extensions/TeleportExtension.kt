package net.blockventuremc.extensions

import io.papermc.paper.entity.TeleportFlag
import net.blockventuremc.modules.`fun`.baloon.Balloon
import net.blockventuremc.modules.structures.StructureManager
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent


/**
 * Teleports an entity asynchronously to a location, with passengers.
 *
 * @param entity The entity to teleport.
 * @param location The location to teleport to.
 */
fun Entity.teleportAsyncWithPassengers(location: Location) {

    var currentBalloon: Balloon? = null
    if(this is Player) {
        currentBalloon = StructureManager.balloons[this]
        currentBalloon?.remove()
    }

    val passengers = this.passengers
    val shouldRemovePassengers = passengers.isNotEmpty() && this.world != location.world
    if (shouldRemovePassengers) {
        for (passenger in passengers) {
            this.removePassenger(passenger)
            passenger.teleportAsyncWithPassengers(location)
        }
    }
    this.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS)
        .thenAccept {
            if (shouldRemovePassengers) {
                for (passenger in passengers) {
                    this.addPassenger(passenger)
                }
            }
        }
    currentBalloon?.spawn()
}