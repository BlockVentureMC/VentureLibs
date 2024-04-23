package net.blockventuremc.modules.general.events.custom

import org.bukkit.event.HandlerList

class AFKChangeEvent(var afk: Boolean, val cause: Cause) : BooleanStatusChangedEvent(afk) {

    companion object {
        private val handlerList = HandlerList()

        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }


    enum class Cause {
        NO_ACTIVITY,
        MOVE,
        CHAT,
        INTERACT,
        UNKNOWN
    }
}