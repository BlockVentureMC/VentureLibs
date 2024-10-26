package net.blockventuremc.modules.structures.events

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TrainEnterEvent(val player: Player, val seat: Entity) : Event(!Bukkit.isPrimaryThread()), Cancellable {

    var cancelledEvent = false

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return cancelledEvent
    }

    override fun setCancelled(cancel: Boolean) {
        cancelledEvent = cancel
    }

}