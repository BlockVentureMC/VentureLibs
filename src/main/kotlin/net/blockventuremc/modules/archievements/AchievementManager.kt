package net.blockventuremc.modules.archievements

import dev.fruxz.ascend.extension.logging.getItsLogger
import dev.fruxz.stacked.extension.Title
import dev.fruxz.stacked.text
import net.blockventuremc.database.functions.addAchievementToUser
import net.blockventuremc.database.model.DatabaseAchievement
import net.blockventuremc.modules.archievements.model.Achievement
import org.bukkit.Bukkit
import java.util.*

object AchievementManager {
    fun addAchievement(uuid: UUID, achievement: Achievement) {
        // TODO: Notify User

        val player = Bukkit.getPlayer(uuid) ?: return

        player.showTitle(Title(text(achievement.title), text(achievement.description)))

        addAchievementToUser(DatabaseAchievement(uuid, achievement))

        getItsLogger().info("Added achievement ${achievement.title} to ${player.name}")

    }
}