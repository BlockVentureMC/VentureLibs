package net.blockventuremc.database.model

import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.modules.achievements.model.Achievement
import java.util.*

data class DatabaseAchievement(
    val uuid: UUID,
    val achievement: Achievement,
    val receivedAt: Calendar = Calendar.now(),
    val counter: Int = 1,
    val lastReceivedAt: Calendar = Calendar.now()
)