package net.blockventuremc.modules.structures.events

import me.m56738.smoothcoasters.api.event.PlayerSmoothCoastersHandshakeEvent
import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.sendError
import net.blockventuremc.extensions.sendInfo
import net.blockventuremc.modules.`fun`.baloon.Balloon
import net.blockventuremc.modules.structures.StructureManager
import net.blockventuremc.modules.structures.vehicle.PacketHandler
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.util.Vector

class StructureEvents : Listener {

    @EventHandler
    fun onPlayerSmoothCoastersHandshakeEvent(event: PlayerSmoothCoastersHandshakeEvent) {
        event.player.sendInfo("Yippi :3")
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
    fun onTeleport(event: PlayerTeleportEvent) {
        val player = event.player

        if(event.cause == PlayerTeleportEvent.TeleportCause.PLUGIN) return

        val targetLocation = event.to
        var currentBalloon: Balloon? = null

        currentBalloon = StructureManager.balloons[player]
        currentBalloon?.remove()

        val passengers = player.passengers
        val shouldRemovePassengers = passengers.isNotEmpty() && player.world != targetLocation.world
        if (shouldRemovePassengers) {
            for (passenger in passengers) {
                player.removePassenger(passenger)
                passenger.teleportAsync(targetLocation)
            }
        }

        if (shouldRemovePassengers) {
            for (passenger in passengers) {
                player.addPassenger(passenger)
            }
        }

        currentBalloon?.spawn(targetLocation.add(Vector(0.0, 0.1, 0.0)))
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        PacketHandler.movementPacketCheck(event.player)
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
            player.sendError("Kuscheln verboten! Nimm dir einen eigenen Platz :3")
            return
        }

        event.isCancelled = true
        seat.addPassenger(player)

        val trainEnterEvent = TrainEnterEvent(player, seat)
        Bukkit.getPluginManager().callEvent(trainEnterEvent)
    }

}