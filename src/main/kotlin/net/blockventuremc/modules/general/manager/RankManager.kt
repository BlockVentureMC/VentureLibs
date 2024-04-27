package net.blockventuremc.modules.general.manager

import dev.kord.common.entity.Snowflake
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.database.functions.getLinkOfDiscord
import net.blockventuremc.database.functions.getLinkOfUser
import net.blockventuremc.database.model.DatabaseUser
import net.blockventuremc.modules.general.model.Ranks
import net.blockventuremc.utils.mcasyncBlocking

object RankManager {
    fun updateRank(rank: Ranks, u: DatabaseUser) {

        val user = u.copy(rank = rank)
        PlayerCache.updateCached(user)

        mcasyncBlocking {
            val link = getLinkOfUser(user.uuid) ?: return@mcasyncBlocking

            rank.updateRole(Snowflake(link.discordID))
        }
    }

    suspend fun updateDiscordRank(discordID: String) {
        val link = getLinkOfDiscord(discordID) ?: return
        val rank = PlayerCache.get(link.uuid).rank

        rank.updateRole(Snowflake(discordID))
    }
}