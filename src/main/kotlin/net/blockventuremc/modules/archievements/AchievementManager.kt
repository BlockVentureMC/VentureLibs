package net.blockventuremc.modules.archievements

import dev.fruxz.ascend.extension.logging.getItsLogger
import dev.fruxz.stacked.extension.Title
import dev.fruxz.stacked.text
import net.blockventuremc.database.functions.addAchievementToUser
import net.blockventuremc.database.functions.getAchievementOfUser
import net.blockventuremc.database.model.DatabaseAchievement
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.toDatabaseUser
import net.blockventuremc.extensions.translate
import net.blockventuremc.modules.archievements.model.Achievement
import org.bukkit.Bukkit
import java.util.*

object AchievementManager {
    private fun addAchievement(uuid: UUID, achievement: Achievement) {

        val player = Bukkit.getPlayer(uuid) ?: return
        val title = player.toDatabaseUser().translate("achievement.${achievement.name.lowercase()}.title")?.message ?: achievement.title
        val description = player.toDatabaseUser().translate("achievement.${achievement.name.lowercase()}.description")?.message ?: achievement.description

        player.showTitle(Title(text(title), text(description)))

        player.sendMessagePrefixed(player.toDatabaseUser().translate("achievement.achieved", mapOf("achievement" to title))
            ?.message ?: "Achievement achieved: $title")

        addAchievementToUser(DatabaseAchievement(uuid, achievement))

        getItsLogger().info("Added achievement $title to ${player.name}")

    }

    fun addIfNotAchieved(uuid: UUID, achievement: Achievement) {
        if (getAchievementOfUser(uuid, achievement) == null) {
            addAchievement(uuid, achievement)
        }
    }
}