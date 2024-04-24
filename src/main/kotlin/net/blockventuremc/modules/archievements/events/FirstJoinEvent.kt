package net.blockventuremc.modules.archievements.events

import net.blockventuremc.modules.archievements.AchievementManager
import net.blockventuremc.modules.archievements.model.Achievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class FirstJoinEvent: Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onFirstJoin(event: PlayerJoinEvent) {
        AchievementManager.addIfNotAchieved(event.player.uniqueId, Achievement.FirstJoin)
    }
}