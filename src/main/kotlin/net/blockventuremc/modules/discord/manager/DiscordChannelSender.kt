package net.blockventuremc.modules.discord.manager

import net.blockventuremc.VentureLibs
import net.blockventuremc.modules.discord.DiscordChannelEnvs
import net.blockventuremc.utils.Environment
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.utils.messages.MessageCreateData

/**
 * Throws an exception message to a Discord channel.
 *
 * @param exception The exception to be thrown to the Discord channel.
 */
fun throwErrorToDiscord(exception: Exception) {
    // Throw to discord
    sendToChannel("TEAM_GUILD", "CHANNEL_LOG", "Ein Fehler ist aufgetreten: ${exception.message}")
}

/**
 * Sends a message to the main channel of a Discord guild based on the provided DiscordChannelEnvs.
 *
 * @param discordChannelEnvs The DiscordChannelEnvs value representing the environment key for the main channel ID.
 * @param message The message to be sent to the main channel.
 * @return A MessageCreateAction object that represents the sent message, or null if an error occurred.
 */
fun sendToMainChannel(discordChannelEnvs: DiscordChannelEnvs, message: String): MessageCreateAction? {
    return sendToChannel("GUILD_ID", discordChannelEnvs.env, message)
}

/**
 * Sends a message embed to the main channel.
 *
 * @param discordChannelEnvs The enum representing the main channel environment.
 * @param embed The embed to be sent.
 * @return The result of sending the message, or null if the message couldn't be sent.
 */
fun sendToMainChannel(discordChannelEnvs: DiscordChannelEnvs, embed: MessageEmbed): MessageCreateAction? {
    return sendToChannel("GUILD_ID", discordChannelEnvs.env, embed)
}


/**
 * Sends a message to a specified channel in a guild.
 *
 * @param guildEnv The environment key for the guild ID.
 * @param channelEnv The environment key for the channel ID.
 * @param message The message to be sent.
 */
fun sendToChannel(guildEnv: String = "GUILD_ID", channelEnv: String, message: String): MessageCreateAction? {
    return sendToChannel(guildEnv, channelEnv, MessageCreateData.fromContent(message))
}

/**
 * Sends a message to a specified channel in a guild.
 *
 * @param guildEnv The environment key for the guild ID.
 * @param channelEnv The environment key for the channel ID.
 * @param message The message to be sent.
 */
fun sendToChannel(guildEnv: String = "GUILD_ID", channelEnv: String, message: MessageCreateData): MessageCreateAction? {
    Environment.getEnv(guildEnv)?.let { guildId ->
        Environment.getEnv(channelEnv)?.let { channelId ->
            val channel = VentureLibs.instance.jda.getGuildById(guildId)?.getTextChannelById(channelId)
            return channel?.sendMessage(message)
        }
    }
    return null
}

/**
 * Sends a message embed to the specified channel in a guild.
 *
 * @param guildId The environment key for the guild ID.
 * @param channelId The environment key for the channel ID.
 * @param embed The embed to be sent.
 */
fun sendToChannel(guildId: String, channelId: String, embed: MessageEmbed): MessageCreateAction? {
    return sendToChannel(guildId, channelId, MessageCreateData.fromEmbeds(embed))
}

/**
 * Sends a log message as an embedded message to a specified channel in a guild.
 *
 * @param embed The EmbedBuilder object representing the embedded message to be sent.
 * @return The MessageCreateAction object representing the action of sending the message, or null if the message cannot be sent.
 */
fun sendLogEmbed(embed: EmbedBuilder): MessageCreateAction? {
    return sendToChannel("TEAM_GUILD", "CHANNEL_LOG", MessageCreateData.fromEmbeds(embed.build()))
}

/**
 * Sends a direct message to a user with the provided user ID and embed.
 *
 * @param userId The ID of the user to send the direct message to.
 * @param embed The embed to be sent.
 * @return The message create action of the sent message, or null if an error occurs.
 */
fun sendDMToUser(userId: String, embed: EmbedBuilder): MessageCreateAction? {
    try {
        val user = VentureLibs.instance.jda.retrieveUserById(userId).complete() ?: throw Exception("User not found.")
        val privateChannel =
            user.openPrivateChannel().complete() ?: throw Exception("Unable to open a private channel.")
        return privateChannel.sendMessageEmbeds(embed.build())
    } catch (e: Exception) {
        throwErrorToDiscord(e)
        return null
    }
}