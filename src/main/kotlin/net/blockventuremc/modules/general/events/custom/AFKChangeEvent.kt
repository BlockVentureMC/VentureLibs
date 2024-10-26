package net.blockventuremc.modules.general.events.custom

import net.blockventuremc.database.model.BlockUser
import org.bukkit.event.HandlerList

class AFKChangeEvent(val blockUser: BlockUser, var afk: Boolean, val cause: Cause) : BooleanStatusChangedEvent(afk) {

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


    enum class Cause {
        NO_ACTIVITY,
        MOVE,
        CHAT,
        INTERACT,
        UNKNOWN
    }
}