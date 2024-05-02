package net.blockventuremc.modules.general.model

import dev.kord.common.entity.Snowflake
import net.blockventuremc.VentureLibs

data class Rank(val name: String, val color: String, val bitsPerMinute: Long = 1, val weight: Int = 0, val parent: Rank? = null, val discordRoleID: String? = null) {

    fun isHigherOrEqual(rank: Ranks): Boolean {
        return this.weight <= rank.rank.weight
    }


    suspend fun updateRole(userID: Snowflake) {
        if (this.discordRoleID == null) return
        val role = VentureLibs.instance.dotenv[this.discordRoleID] ?: return
        val guild = VentureLibs.bot.kord.getGuild(Snowflake(VentureLibs.instance.dotenv["GUILD_ID"]?.toLong() ?: 0))
        val member = guild.getMember(userID)

        member.addRole(Snowflake(role))
    }
}
