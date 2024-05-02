package net.blockventuremc.modules.general.model

import dev.kord.common.entity.Snowflake
import net.blockventuremc.VentureLibs

enum class Ranks(val color: String, val bitsPerMinute: Long = 1, val role: String? = null) {
    Crew("#54a0ff", 3),
    Trial("#576574", 2, "TRIAL_ROLE_ID"),
    ClubMember("#ea8685", 2, "CLUB_MEMBER_ROLE_ID"),
    Guest("#c8d6e5");

    fun isHigherOrEqual(rank: Ranks): Boolean {
        return this.ordinal <= rank.ordinal
    }

    suspend fun updateRole(userID: Snowflake) {
        if (this.role == null) return
        val r = VentureLibs.instance.dotenv[this.role] ?: return
        val g = VentureLibs.bot.kord.getGuild(Snowflake(VentureLibs.instance.dotenv["GUILD_ID"]?.toLong() ?: 0))
        val m = g.getMember(userID)

        m.addRole(Snowflake(r))
    }
}