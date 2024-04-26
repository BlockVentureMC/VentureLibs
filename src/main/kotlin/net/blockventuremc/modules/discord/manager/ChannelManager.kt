package net.blockventuremc.modules.discord.manager

import dev.fruxz.ascend.extension.logging.getItsLogger
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.EmbedBuilder
import net.blockventuremc.BlockVenture

object ChannelManager {
    var rideLog: Snowflake = Snowflake(BlockVenture.instance.dotenv["RIDE_LOG_CHANNEL_ID"]?.toLong() ?: 0)
    var economy: Snowflake = Snowflake(BlockVenture.instance.dotenv["ECONOMY_CHANNEL_ID"]?.toLong() ?: 0)
    var guildID: Snowflake = Snowflake(BlockVenture.instance.dotenv["GUILD_ID"]?.toLong() ?: 0)

    suspend fun sendRideLog(builder: EmbedBuilder.() -> Unit) {
        var c = BlockVenture.bot.kord.getGuild(guildID).getChannel(rideLog)

        c = c as TextChannel

        c.createEmbed(builder)
    }

    suspend fun sendEconomy(builder: EmbedBuilder.() -> Unit) {
        var c = BlockVenture.bot.kord.getGuild(guildID).getChannel(economy)

        c = c as TextChannel

        c.createEmbed(builder)
    }
}