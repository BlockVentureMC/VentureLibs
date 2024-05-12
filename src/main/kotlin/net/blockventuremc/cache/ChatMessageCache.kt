package net.blockventuremc.cache

import dev.fruxz.stacked.text
import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.addToHistory
import net.blockventuremc.modules.general.events.custom.ChatHistoryUpdateEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object ChatMessageCache {

    private val chatMessages = mutableMapOf<UUID, LRUCacheList<Component>>()
    private val cacheLock: ReadWriteLock = ReentrantReadWriteLock()

    /**
     * Adds a chat message to the cache.
     *
     * This method adds a chat message to the cache by acquiring a write lock on the cache lock, adding the message to
     * the chatMessages collection, and then releasing the write lock. It ensures that the chat message is added to the
     * cache by preventing concurrent access to the cache.
     *
     * @param player the player to whom the message belongs
     * @param message the chat message to add
     */
    fun addMessage(player: UUID, message: Component) {
        try {
            cacheLock.writeLock().lock()
            if (!chatMessages.containsKey(player)) {
                chatMessages[player] = LRUCacheList(100)
            }

            //if (isMessageBeforeTheSame(player, message)) return
            chatMessages[player]?.add(message)

            val chatHistoryUpdateEvent = ChatHistoryUpdateEvent(player, chatMessages[player]?.toList() ?: emptyList())
            Bukkit.getScheduler().runTask(VentureLibs.instance, Runnable {
                Bukkit.getPluginManager().callEvent(chatHistoryUpdateEvent)
            })
        }
        finally {
            cacheLock.writeLock().unlock()
        }
    }

    /**
     * Checks if the given message is the last message sent by the player.
     *
     * @param player The UUID of the player.
     * @param message The message to check.
     * @return true if the message is the last message sent by the player, false otherwise.
     */
    private fun isMessageBeforeTheSame(player: UUID, message: Component): Boolean {
        return chatMessages[player]?.lastOrNull() == message
    }

    /**
     * Adds the given [message] to the chat cache for all players.
     *
     * This method iterates over all players in the chatMessages cache and calls the "addMessage" method for each player
     * with the given [message]. It ensures that the message is added to the cache for each player by preventing concurrent
     * access to the cache.
     *
     * @param message the chat message to add for all players
     * @see addMessage
     */
    fun addMessageForAll(message: Component) {
        chatMessages.keys.forEach { uuid ->
            addMessage(uuid, message)
        }
    }

    /**
     * Clears the chat messages.
     *
     * This method clears the chat messages by acquiring a write lock on the cache lock, removing all messages from the
     * chatMessages collection, and then releasing the write lock. It ensures that the chat is cleared by removing the
     * messages and preventing concurrent access to the cache.
     */
    fun clearMessages() {
        cacheLock.writeLock().lock()
        chatMessages.clear()
        cacheLock.writeLock().unlock()
    }

    /**
     * Prints messages to the players in the chat.
     *
     * This method iterates over the `chatMessages` map and sends each message to the respective player if they are online.
     */
    fun printMessages() {
        chatMessages.forEach { (uuid, messages) ->
            messages.forEach { message ->
                Bukkit.getPlayer(uuid)?.sendMessage(message)
            }
        }
    }

    /**
     * Prints the chat messages for the specified player.
     *
     * @param player The UUID of the player.
     */
    fun printMessages(player: UUID) {
        chatMessages[player]?.forEach { message ->
            Bukkit.getPlayer(player)?.sendMessage(message)
        }
    }

    /**
     * Initializes the player by adding 100 empty chat history entries.
     *
     * @param uniqueId The unique ID of the player to initialize.
     */
    fun initPlayer(uniqueId: UUID) {
        try {
            cacheLock.writeLock().lock()
            chatMessages[uniqueId] = LRUCacheList(100)
            for (i in 0 until 100) {
                chatMessages[uniqueId]?.add(Component.empty())
            }
        }
        finally {
            cacheLock.writeLock().unlock()
        }
    }
}