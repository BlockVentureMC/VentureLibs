package net.blockventuremc.database.model

import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.modules.general.model.Languages
import net.blockventuremc.modules.general.model.Ranks
import java.util.*
import kotlin.time.Duration

data class DatabaseUser(val uuid: UUID,
                        val username: String,
                        val rank: Ranks = Ranks.Default,
                        val language: Languages = Languages.EN,

                        // Other

                        val firstJoined: Calendar = Calendar.now(),
                        val lastTimeJoined: Calendar = Calendar.now(),
                        val onlineTime: Duration = Duration.ZERO,
)