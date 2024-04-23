package net.blockventuremc.modules.general.events.custom

import org.bukkit.event.Cancellable
import org.bukkit.event.Event

abstract class BooleanStatusChangedEvent(var newValue: Boolean) : Event(), Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
}