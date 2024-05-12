package net.blockventuremc.modules.general.events.custom

import net.kyori.adventure.text.Component
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

class ChatHistoryUpdateEvent(val uniqueId: UUID, val history: List<Component>) : Event(), Cancellable {

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return ChatHistoryUpdateEvent.handlerList
    }

    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

}