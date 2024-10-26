package net.blockventuremc.database.model

import dev.fruxz.ascend.extension.logging.getItsLogger
import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.consts.AFK_DURATION
import net.blockventuremc.database.functions.getPlayerTitles
import net.blockventuremc.modules.general.events.custom.AFKChangeEvent
import net.blockventuremc.modules.general.model.Languages
import net.blockventuremc.modules.titles.Title
import java.util.*
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.time.Duration

data class BlockUser(
    val uuid: UUID,
    val username: String,
    val language: Languages = Languages.EN,

    val xp: Long = 0,

    val ventureBits: Long = 0,

    // Other
    val firstJoined: Calendar = Calendar.now(),
    val lastTimeJoined: Calendar = Calendar.now(),
    val onlineTime: Duration = Duration.ZERO,

    var selectedTitle: Title? = null,

    var afk: Boolean = false,
    var lastActivity: Calendar = Calendar.now(),

    val titles: MutableMap<Title, Calendar> = getPlayerTitles(uuid)
) {

    val level: Int
        get() {
            return ((this.xp.toDouble() / 100.toDouble()).pow(0.6)).toInt()
        }

    fun nextLevelExp(forLevel: Int = level + 1): Long {
        return (exp(ln(forLevel.toDouble()) / 0.6) * 100).toLong()
    }

    fun testForActivity() {
        if (afk) return
        val lastActivityDuration = lastActivity.durationToNow()
        if (lastActivityDuration >= AFK_DURATION) {
            val afkStatusChangedEvent = AFKChangeEvent(this, true, AFKChangeEvent.Cause.NO_ACTIVITY)
            if (afkStatusChangedEvent.isCancelled) return
            afk = true
            getItsLogger().info("Player $username ($uuid) is now AFK")
        }
    }

    fun addActivity(cause: AFKChangeEvent.Cause) {
        if (afk) {
            val afkStatusChangedEvent = AFKChangeEvent(this, false, cause)
            if (afkStatusChangedEvent.isCancelled) return
            afk = false
            val afkDuration = lastActivity.durationToNow()
            getItsLogger().info("Player $username ($uuid) is no longer AFK (${cause.name}) after $afkDuration")
        }
        lastActivity = Calendar.now()
    }


}