package net.blockventuremc.modules.archievements

import dev.fruxz.ascend.extension.logging.getItsLogger
import dev.fruxz.ascend.tool.time.calendar.Calendar
import dev.fruxz.stacked.extension.Title
import dev.fruxz.stacked.text
import net.blockventuremc.database.functions.addAchievementToUser
import net.blockventuremc.database.functions.getAchievementOfUser
import net.blockventuremc.database.model.DatabaseAchievement
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.toBlockUser
import net.blockventuremc.extensions.translate
import net.blockventuremc.modules.archievements.model.Achievement
import org.bukkit.Bukkit
import java.util.*


/**
 * Singleton class that manages the achievements in the system.
 */
object AchievementManager {

    /**
     * Adds an achievement to a player.
     *
     * @param uuid The UUID of the player.
     * @param achievement The achievement to add.
     */
    private fun addAchievement(uuid: UUID, achievement: Achievement) {
        val player = Bukkit.getPlayer(uuid) ?: return
        val title = player.toBlockUser().translate("achievement.${achievement.name.lowercase()}.title")?.message
            ?: achievement.title
        val description =
            player.toBlockUser().translate("achievement.${achievement.name.lowercase()}.description")?.message
                ?: achievement.description

        player.showTitle(Title(text(title), text(description)))

        player.sendMessagePrefixed(
            player.toBlockUser().translate("achievement.achieved", mapOf("achievement" to title))
                ?.message ?: "Achievement achieved: $title"
        )

        addAchievementToUser(DatabaseAchievement(uuid, achievement))
        getItsLogger().info("Added new achievement $title to ${player.name}")
    }

    /**
     * Adds or updates an achievement for a user with the given UUID.
     *
     * @param uuid The UUID of the user.
     * @param achievement The achievement to add or update.
     */
    fun addOrUpdateAchievement(uuid: UUID, achievement: Achievement) {
        val achievementData = getAchievementOfUser(uuid, achievement)
        if (achievementData == null) {
            addAchievement(uuid, achievement)
        } else {
            addAchievementToUser(
                achievementData.copy(
                    counter = achievementData.counter + 1,
                    lastReceivedAt = Calendar.now()
                )
            )
        }
    }
}