package net.blockventuremc.modules.discord.commands

import dev.kord.common.entity.Permission
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer
import net.blockventuremc.modules.discord.model.AbstractCommand
import net.blockventuremc.utils.translate

class TestCommand: AbstractCommand() {
    override val name = "test"

    override val permission = Permission.SendMessages

    override val options = fun ChatInputCreateBuilder.() {
        integer("number", "x") {
            required = true

            translate()
        }
    }

    override suspend fun execute(bot: Kord, interaction: GuildChatInputCommandInteraction) {
        interaction.channel.createMessage("Test command executed!")
    }
}