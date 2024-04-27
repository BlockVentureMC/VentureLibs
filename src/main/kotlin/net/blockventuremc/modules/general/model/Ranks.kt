package net.blockventuremc.modules.general.model

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Role
import net.blockventuremc.BlockVenture

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
        val r = BlockVenture.instance.dotenv[this.role] ?: return
        val g = BlockVenture.bot.kord.getGuild(Snowflake(BlockVenture.instance.dotenv["GUILD_ID"]?.toLong() ?: 0))
        val m = g.getMember(userID)

        m.addRole(Snowflake(r))
    }
}