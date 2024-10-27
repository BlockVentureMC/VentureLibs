package net.blockventuremc.modules.general.model

import net.blockventuremc.VentureLibs
import net.blockventuremc.utils.Environment
import net.dv8tion.jda.api.entities.UserSnowflake

data class Rank(
    val name: String,
    val displayName: String,
    val color: String,
    val bitsPerMinute: Long = 1,
    val weight: Int = 0,
    val parent: Rank? = null,
    val discordRoleID: String? = null
) {

    fun isHigherOrEqual(rank: Ranks): Boolean {
        return this.weight >= rank.rank.weight
    }


    fun updateRole(userID: String) {
        if (this.discordRoleID == null) return
        val roleId = Environment.getEnv(this.discordRoleID) ?: return
        val guild = VentureLibs.instance.jda.getGuildById(Environment.getEnv("GUILD_ID") ?: "906872550078967839")
        val role = guild?.getRoleById(roleId) ?: return

        guild.addRoleToMember(UserSnowflake.fromId(userID), role).queue()
    }
}
