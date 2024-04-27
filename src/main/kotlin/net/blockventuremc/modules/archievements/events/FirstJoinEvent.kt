package net.blockventuremc.modules.archievements.events

import net.blockventuremc.modules.archievements.AchievementManager
import net.blockventuremc.modules.archievements.model.Achievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Represents a class that listens for the player's first join event and adds or updates the FirstJoin achievement for the player.
 */
class FirstJoinEvent : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onFirstJoin(event: PlayerJoinEvent) {
        AchievementManager.addOrUpdateAchievement(event.player.uniqueId, Achievement.FirstJoin)
    }

}