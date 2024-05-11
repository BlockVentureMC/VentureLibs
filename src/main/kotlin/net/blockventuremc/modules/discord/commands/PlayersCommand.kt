package net.blockventuremc.modules.discord.commands

import dev.kord.common.Color
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import net.blockventuremc.modules.discord.model.AbstractCommand
import net.blockventuremc.modules.general.model.Ranks
import net.blockventuremc.modules.placeholders.parsePlaceholders
import org.bukkit.Bukkit

class PlayersCommand : AbstractCommand() {
    override val name = "players"

    override val options = fun ChatInputCreateBuilder.() { }
    private val userNamePreset = "%luckperms_primary_group_name% » %playername%"

    override suspend fun execute(bot: Kord, interaction: GuildChatInputCommandInteraction) {

        val onlinePlayers = Bukkit.getOnlinePlayers().map { parsePlaceholders(userNamePreset, it) }
        val ranks = onlinePlayers.groupBy { it.split(" » ")[0] }
        val orderedRanks = ranks.keys.sortedByDescending { Ranks.valueOf(it).ordinal }

        val em = EmbedBuilder().apply {
            title = "Online Players"
            orderedRanks.forEach { rank ->
                val players = ranks[rank] ?: return@forEach
                field {
                    name = rank
                    value = players.joinToString("\n")
                }
            }
            color = Color(255, 107, 107)
        }

        interaction.respondEphemeral {
            embeds = mutableListOf(em)
        }
    }
}