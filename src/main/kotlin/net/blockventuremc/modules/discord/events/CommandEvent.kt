package net.blockventuremc.modules.discord.events

import dev.kord.core.Kord
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import net.blockventuremc.modules.discord.model.Event
import net.blockventuremc.utils.RegisterManager

class CommandEvent: Event() {
    override suspend fun execute(bot: Kord) = bot.on<GuildChatInputCommandInteractionCreateEvent> {
        val command = RegisterManager.dcCommands.find { it.name == interaction.command.rootName }

        if (command != null) {
            command.execute(bot, interaction)
        }
    }
}