package net.blockventuremc.database.model

import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.modules.archievements.model.Achievement
import java.util.*

data class DatabaseAchievement(
    val uuid: UUID,
    val achievement: Achievement,

    val gottenAt: Calendar = Calendar.now()
)