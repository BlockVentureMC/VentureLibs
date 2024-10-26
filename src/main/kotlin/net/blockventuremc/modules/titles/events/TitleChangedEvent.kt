package net.blockventuremc.modules.titles.events

import net.blockventuremc.modules.titles.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TitleChangedEvent(val player: Player, val newTitle: Title? = null) : Event(!Bukkit.isPrimaryThread()) {

    companion object {
        private val handlerList = HandlerList()

        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

}