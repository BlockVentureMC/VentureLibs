package net.blockventuremc.modules.discord.commands

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import net.blockventuremc.modules.discord.model.AbstractCommand
import net.blockventuremc.utils.translate

class LinkCommand: AbstractCommand() {
    override val name = "link"

    override val options = fun ChatInputCreateBuilder.() {
        string("username", "Username") {
            required = true

            translate()
        }
    }

    override suspend fun execute(bot: Kord, interaction: GuildChatInputCommandInteraction) {
        interaction.deferEphemeralResponse()
        interaction.respondEphemeral {
            content = "You entered ${interaction.command.options["username"]?.value}"
        }
    }
}