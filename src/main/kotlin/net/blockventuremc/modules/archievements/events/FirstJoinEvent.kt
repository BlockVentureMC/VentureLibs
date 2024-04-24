package net.blockventuremc.modules.archievements.events

import net.blockventuremc.database.functions.getAchievementOfUser
import net.blockventuremc.modules.archievements.AchievementManager
import net.blockventuremc.modules.archievements.model.Achievement
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class FirstJoinEvent: Listener {
    @EventHandler
    fun onFirstJoin(event: PlayerJoinEvent) {
        val achievement = getAchievementOfUser(event.player.uniqueId, Achievement.FirstJoin)

        if (achievement == null) {
            AchievementManager.addAchievement(event.player.uniqueId, Achievement.FirstJoin)
        }
    }
}