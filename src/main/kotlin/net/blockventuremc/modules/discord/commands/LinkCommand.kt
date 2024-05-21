package net.blockventuremc.modules.discord.commands

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import net.blockventuremc.audioserver.common.extensions.getLogger
import net.blockventuremc.database.functions.getLinkOfDiscord
import net.blockventuremc.database.model.Link
import net.blockventuremc.extensions.translate
import net.blockventuremc.modules.discord.manager.LinkManager
import net.blockventuremc.modules.discord.model.AbstractCommand
import org.bukkit.Bukkit

class LinkCommand : AbstractCommand() {
    override val name = "link"

    override val options = fun ChatInputCreateBuilder.() {
        string("username", "Username") {
            required = true

            translate()
        }
    }

    override suspend fun execute(bot: Kord, interaction: GuildChatInputCommandInteraction) = with(interaction) {
        val playerName = interaction.command.options["username"]?.value as? String ?: run {
            interaction.respondEphemeral {
                content = "Please provide a username"
            }
            return@with
        }

        getLogger().info("Linking $playerName")
        val player = Bukkit.getPlayer(playerName)
        if (player == null) {
            interaction.respondEphemeral {
                content = "The player $playerName is not online"
            }
            getLogger().info("Player $playerName is not online. Online players: ${Bukkit.getOnlinePlayers().joinToString(", ") { "`${it.name}`" }}")
            return
        }

        val tries = LinkManager.triesToGetLink(interaction.user.id)

        if (tries != null) {
            interaction.respondEphemeral {
                content = "You are already trying to link to ${Bukkit.getPlayer(tries.uuid)?.name}"
            }
            return
        }

        val dbLink = getLinkOfDiscord(interaction.user.id.toString())

        if (dbLink != null) {
            interaction.respondEphemeral {
                content = "You are already linked to ${Bukkit.getPlayer(dbLink.uuid)?.name}"
            }
            return
        }

        val link = Link(
            uuid = player.uniqueId,
            discordID = interaction.user.id.toString()
        )

        LinkManager.add(interaction.user.username, link)

        val em = EmbedBuilder().apply {
            title = "Linking"
            description = "> You successfully started the linking process to ${player.name}"
        }
        em.field("Next step", false) {
            "Now you need to run `/link` in the game chat"
        }

        interaction.respondEphemeral {
            embeds = mutableListOf(em)
        }
    }
}