package net.blockventuremc.database.model

import dev.fruxz.ascend.extension.logging.getItsLogger
import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.consts.AFK_DURATION
import net.blockventuremc.modules.general.events.custom.AFKChangeEvent
import net.blockventuremc.modules.general.model.Languages
import net.blockventuremc.modules.general.model.Ranks
import java.util.*
import kotlin.time.Duration

data class DatabaseUser(
    val uuid: UUID,
    val username: String,
    val rank: Ranks = Ranks.Guest,
    val language: Languages = Languages.EN,

    // Other
    val firstJoined: Calendar = Calendar.now(),
    val lastTimeJoined: Calendar = Calendar.now(),
    val onlineTime: Duration = Duration.ZERO,

    var afk: Boolean = false,
    var lastActivity: Calendar = Calendar.now()
) {

    fun testForActivity() {
        if (afk) return
        val lastActivityDuration = lastActivity.durationToNow()
        if (lastActivityDuration >= AFK_DURATION) {
            val afkStatusChangedEvent = AFKChangeEvent(true, AFKChangeEvent.Cause.NO_ACTIVITY)
            if (afkStatusChangedEvent.isCancelled) return
            afk = true
            getItsLogger().info("Player $username ($uuid) is now AFK")
        }
    }

    fun addActivity(cause: AFKChangeEvent.Cause) {
        if (afk) {
            val afkStatusChangedEvent = AFKChangeEvent(false, cause)
            if (afkStatusChangedEvent.isCancelled) return
            afk = false
            val afkDuration = lastActivity.durationToNow()
            getItsLogger().info("Player $username ($uuid) is no longer AFK (${cause.name}) after $afkDuration")
        }
        lastActivity = Calendar.now()
    }


}