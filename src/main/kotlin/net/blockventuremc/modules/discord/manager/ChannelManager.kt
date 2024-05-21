package net.blockventuremc.modules.discord.manager

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.EmbedBuilder
import net.blockventuremc.VentureLibs

object ChannelManager {

    suspend fun sendRideLog(builder: EmbedBuilder.() -> Unit) {
        val guildID = Snowflake(VentureLibs.instance.dotenv["GUILD_ID"]?.toLong() ?: 0)
        val rideLog = Snowflake(VentureLibs.instance.dotenv["RIDE_CHANNEL_ID"]?.toLong() ?: 0)
        var guildChannel = VentureLibs.bot.kord.getGuild(guildID).getChannel(rideLog)

        guildChannel = guildChannel as TextChannel

        guildChannel.createEmbed(builder)
    }

    suspend fun sendEconomy(builder: EmbedBuilder.() -> Unit) {
        val guildID = Snowflake(VentureLibs.instance.dotenv["GUILD_ID"]?.toLong() ?: 0)
        val economy = Snowflake(VentureLibs.instance.dotenv["ECONOMY_CHANNEL_ID"]?.toLong() ?: 0)
        var guildChannel = VentureLibs.bot.kord.getGuild(guildID).getChannel(economy)

        guildChannel = guildChannel as TextChannel

        guildChannel.createEmbed(builder)
    }
}