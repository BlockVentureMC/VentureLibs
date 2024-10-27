package net.blockventuremc.modules.discord.commands

import net.blockventuremc.modules.discord.annotations.SlashCommand
import net.blockventuremc.modules.general.model.Ranks
import net.blockventuremc.utils.parsePlaceholders
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Bukkit

@SlashCommand(
    name = "players",
    description = "Show the online players"
)
class PlayersCommand : ListenerAdapter() {

    private val userNamePreset = "%luckperms_primary_group_name% » %playername%"

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = with(event) {
        if (name != "players") return

        val onlinePlayers = Bukkit.getOnlinePlayers().map { parsePlaceholders(userNamePreset, it) }
        val ranks = onlinePlayers.groupBy { it.split(" » ")[0] }
        val orderedRanks = ranks.keys.sortedByDescending { Ranks.valueOf(it).ordinal }

        val embedBuilder = EmbedBuilder()
            .setTitle("Online Players")
            .setColor(0x3498db)
        orderedRanks.forEach { rank ->
            val players = ranks[rank] ?: return@forEach
            embedBuilder.addField(rank, players.joinToString("\n"), false)
        }

        replyEmbeds(embedBuilder.build()).setEphemeral(true).queue()
    }

}