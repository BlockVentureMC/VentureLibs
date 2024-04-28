package net.blockventuremc.modules.boosters

import dev.fruxz.ascend.tool.time.calendar.Calendar
import java.util.UUID

data class BitBoosters(
    val owner: UUID,
    val startTime: Calendar,
    val endTime: Calendar,
    val modifier: Long
)