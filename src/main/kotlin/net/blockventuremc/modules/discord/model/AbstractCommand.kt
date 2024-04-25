package net.blockventuremc.modules.discord.model

import dev.kord.common.entity.Permission
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder

abstract class AbstractCommand {
    abstract val name: String
    open val permission: Permission? = null
    abstract val options: ChatInputCreateBuilder.() -> Unit

    abstract suspend fun execute(bot: Kord, interaction: GuildChatInputCommandInteraction)
}