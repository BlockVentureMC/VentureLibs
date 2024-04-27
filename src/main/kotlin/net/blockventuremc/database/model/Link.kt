package net.blockventuremc.database.model

import dev.fruxz.ascend.tool.time.calendar.Calendar
import java.util.*

data class Link(
    val uuid: UUID,
    val discordID: String,
    val linkedAt: Calendar = Calendar.now()
)