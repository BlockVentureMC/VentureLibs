package net.blockventuremc.modules.chatpanels

import net.blockventuremc.VentureLibs
import net.blockventuremc.cache.ChatMessageCache
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.util.*

object ChatPanelManager {

    private val chatPanels = mutableMapOf<String, ChatPanel>()

    /**
     * Registers a chat panel and adds it to the chat panels collection.
     *
     * @param chatPanel the chat panel to be registered
     *
     * @see ChatPanel
     */
    fun registerChatPanel(chatPanel: ChatPanel) {
        chatPanels[chatPanel.id] = chatPanel
        chatPanel.display()
    }

    /**
     * Redisplay chat panels.
     *
     * This function is responsible for re-displaying chat panels. It first calls the `printMessages()` function
     * from the `ChatMessageCache` class to print any pending messages to the players in the chat.
     *
     * Then, it iterates over the `chatPanels` map and calls the `display()` method for each chat panel,
     * which will update the chat panel's display according to its implementation.
     *
     * @see ChatMessageCache#printMessages
     * @see ChatPanel#display
     */
    private fun redisplayChatPanels() {
        ChatMessageCache.printMessages()
        chatPanels.values.forEach { chatPanel ->
            chatPanel.display()
        }
    }

    /**
     * Redisplays the chat panels to the specified player.
     *
     * @param player The UUID of the player to redisplay the chat panels to.
     */
    fun redisplayChatPanels(player: UUID) {
        if (chatPanels.isEmpty()) return
        ChatMessageCache.printMessages(player)
        chatPanels.values.forEach { chatPanel ->
            chatPanel.display()
        }
    }

    /**
     * Removes the specified chatPanel from the list of registered chat panels.
     *
     * @param chatPanel the chat panel to unregister
     */
    fun unregisterChatPanel(chatPanel: ChatPanel) {
        chatPanels.remove(chatPanel.id)
    }

    /**
     * Retrieves the ChatPanel from the collection of chat panels based on the specified ID.
     *
     * @param id The ID of the ChatPanel to retrieve.
     * @return The ChatPanel object associated with the given ID, or null if no such ChatPanel exists.
     */
    fun getChatPanel(id: String): ChatPanel? {
        return chatPanels[id]
    }

    /**
     * Retrieves a list of all the chat panels.
     *
     * @return a list of [ChatPanel] objects representing all the chat panels.
     */
    fun getChatPanels(): List<ChatPanel> {
        return chatPanels.values.toList()
    }

    // Update task
    private var updateTask: BukkitTask? = null
    /**
     * Starts the update task.
     *
     * This method schedules an asynchronous task using the Bukkit scheduler to update the chat panels.
     * It iterates through all chat panels in the `chatPanels` map and removes any expired chat panels.
     * An expired chat panel is determined by checking if its `until` property is expired.
     *
     * @since <version_number>
     */
    fun startUpdateTask() {
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(VentureLibs.instance, kotlinx.coroutines.Runnable {
            chatPanels.values.forEach { chatPanel ->
                if (chatPanel.until.isExpired) {
                    chatPanels.remove(chatPanel.id)
                    redisplayChatPanels()
                    return@forEach
                }
            }
        }, 0, 20)
    }

    /**
     * Stops the update task.
     *
     * Cancels the update task if it is running.
     */
    fun stopUpdateTask() {
        updateTask?.cancel()
    }
}