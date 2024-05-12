package net.blockventuremc.modules.chatpanels

import net.blockventuremc.cache.ChatMessageCache
import net.blockventuremc.modules.general.events.custom.ChatHistoryUpdateEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ChatHistoryListener : Listener {

    @EventHandler
    fun onChatHistoryUpdate(event: ChatHistoryUpdateEvent) {
        ChatPanelManager.redisplayChatPanels(event.uniqueId)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        ChatMessageCache.initPlayer(event.player.uniqueId)

        ChatPanelManager.redisplayChatPanels(event.player.uniqueId)
    }
}