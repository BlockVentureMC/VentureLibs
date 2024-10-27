package net.blockventuremc.database.model

import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.modules.boosters.BoosterCategory
import java.util.*
import kotlin.time.Duration.Companion.hours

data class BitBoosters(
    val owner: UUID,
    val startTime: Calendar = Calendar.now(),
    val endTime: Calendar = Calendar.now() + 2.hours,
    val modifier: Long = 1,
    val user: Boolean = false,
    val category: BoosterCategory = BoosterCategory.USER_ACTIVATED
)

