package net.blockventuremc.modules.placeholders

import dev.fruxz.stacked.text
import me.neznamy.tab.api.TabAPI
import me.neznamy.tab.api.event.plugin.TabLoadEvent
import me.neznamy.tab.api.placeholder.Placeholder
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.extensions.rank
import net.blockventuremc.extensions.toBlockUser
import net.blockventuremc.extensions.translate
import org.bukkit.Bukkit
import org.bukkit.entity.Player


/**
 * Manages player placeholders.
 */
class PlayerPlaceholderManager {

    /**
     * Represents a list of player placeholders.
     */
    private val placeholders = mutableListOf<Placeholder>()

    init {
        reloadPlaceholders()
        setupReload()
    }

    /**
     * Sets up the reload functionality for the player placeholders.
     * This method registers TabLoadEvent listener and reloads the placeholders.
     * @see PlayerPlaceholderManager.reloadPlaceholders
     */
    private fun setupReload() {
        TabAPI.getInstance().eventBus?.register(TabLoadEvent::class.java) { _ ->
            reloadPlaceholders()
        }
    }

    /**
     * Registers the placeholders for the player.
     */
    private fun registerPlaceholders() {
        placeholders.clear()

        val placeholderManager = TabAPI.getInstance().placeholderManager

        placeholders += placeholderManager.registerPlayerPlaceholder("%rank%", 5000) { player ->
            (player.player as Player).rank.displayName
        }

        placeholders += placeholderManager.registerPlayerPlaceholder("%color%", 5000) { player ->
            (player.player as Player).rank.color
        }

        placeholders += placeholderManager.registerRelationalPlaceholder("%rel_title%", 5000) { viewer, player ->
            val title = (player.player as Player).toBlockUser().selectedTitle
            return@registerRelationalPlaceholder if (title == null) {
                (viewer.player as Player).translate("title.none")?.message ?: "<color:#4b6584>No title"
            } else {
                title.display(viewer.player as Player)
            }
        }

        placeholders += placeholderManager.registerPlayerPlaceholder("%rankord%", 5000) { player ->
            (player.player as Player).rank.weight.toString()
        }

        placeholders += placeholderManager.registerPlayerPlaceholder("%xp%", 1000) { player ->
            (player.player as Player).toBlockUser().xp.toString()
        }

        placeholders += placeholderManager.registerPlayerPlaceholder("%level%", 1000) { player ->
            (player.player as Player).toBlockUser().level.toString()
        }

        placeholders += placeholderManager.registerPlayerPlaceholder("%nextLevelExp%", 1000) { player ->
            (player.player as Player).toBlockUser().nextLevelExp().toString()
        }
    }

    /**
     * Reloads the placeholders for the player.
     */
    private fun reloadPlaceholders() {
        registerPlaceholders()
        Bukkit.getOnlinePlayers().forEach { player ->
            player.kick(text("TAB reload. Please rejoin."))
        }
        getLogger().info("TAB placeholders have been re-registered!")
    }
}


