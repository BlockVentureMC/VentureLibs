package net.blockventuremc.modules.discord.commands

import net.blockventuremc.database.functions.getLinkOfDiscord
import net.blockventuremc.database.model.Link
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.extensions.sendInfo
import net.blockventuremc.modules.discord.annotations.SlashCommand
import net.blockventuremc.modules.discord.interfaces.HasOptions
import net.blockventuremc.modules.discord.manager.LinkManager
import net.blockventuremc.modules.general.model.Languages
import net.blockventuremc.modules.i18n.TranslationCache
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@SlashCommand(
    name = "link",
    description = "Link your Minecraft account to your Discord account"
)
class LinkCommand : ListenerAdapter(), HasOptions {

    override fun getOptions(): List<OptionData> {
        return listOf(
            OptionData(OptionType.STRING, "username", "The username of the Minecraft account you want to link")
                .setRequired(true)
        )
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = with(event) {
        if (name != "link") return

        val language = Languages.entries.firstOrNull { it.locale == userLocale.toLocale() } ?: Languages.EN

        val playerName = getOption("username")?.asString ?: run {
            reply(
                TranslationCache.get(language.code, "commands.link.username_not_provided")?.message
                    ?: "Please provide a username"
            ).setEphemeral(true).queue()
            return@with
        }

        getLogger().info("Linking $playerName")
        val player = Bukkit.getPlayer(playerName)

        if (checkPlayerOnline(player, language, playerName)) return
        player!! // Player cannot be null from this point

        val dbLink = getLinkOfDiscord(user.id)
        if (isAlreadyLinked(dbLink, language)) return

        val tries = LinkManager.triesToGetLink(user.id)
        if (isTryingLink(tries, language)) return

        LinkManager.add(user.id, Link(player.uniqueId, user.id))

        val embedBuilder = EmbedBuilder()
            .setTitle(
                TranslationCache.get(language.code, "commands.link.linked_title")?.message ?: "Linking your account"
            )
            .setDescription(
                TranslationCache.get(
                    language.code,
                    "commands.link.linked_description",
                    mapOf("player" to player.name)
                )?.message ?: "> You successfully started the linking process to `${player.name}`"
            )
            .addField(
                TranslationCache.get(language.code, "commands.link.linked_field")?.message ?: "What's next?",
                TranslationCache.get(language.code, "commands.link.linked_field_description")?.message
                    ?: "Please check your Minecraft chat for further instructions.",
                false
            )
            .setThumbnail("https://mcl.flawcra.cc/img/full/512/${player.uniqueId}")
            .setColor(0x2ecc71)

        replyEmbeds(embedBuilder.build()).setEphemeral(true).queue()

        player.sendInfo(
            TranslationCache.get(
                language.code,
                "commands.link.linked_field_description",
                mapOf("discordUser" to user.name)
            )?.message
                ?: "Someone is trying to link their Discord account (${user.name}) to your Minecraft account. Please run `/link` in the game chat to confirm."
        )

        getLogger().info("Successfully started the linking process to ${player.name}")
    }

    private fun SlashCommandInteractionEvent.checkPlayerOnline(
        player: Player?,
        language: Languages,
        playerName: String
    ): Boolean {
        if (player == null) {
            reply(
                TranslationCache.get(
                    language.code,
                    "commands.link.player_not_online",
                    mapOf("player" to playerName)
                )?.message ?: "The player `$playerName` is not online"
            ).setEphemeral(true).queue()

            getLogger().info(
                "Player $playerName is not online. Online players: ${
                    Bukkit.getOnlinePlayers().joinToString(", ") { "`${it.name}`" }
                }"
            )
            return true
        }
        return false
    }

    private fun SlashCommandInteractionEvent.isAlreadyLinked(
        dbLink: Link?,
        language: Languages
    ): Boolean {
        if (dbLink != null) {
            val linkedPlayer = Bukkit.getPlayer(dbLink.uuid)
            reply(
                TranslationCache.get(
                    language.code,
                    "commands.link.already_linked",
                    mapOf("player" to (linkedPlayer?.name ?: dbLink.uuid.toString()))
                )?.message ?: "You are already linked to `${linkedPlayer?.name ?: dbLink.uuid.toString()}`."
            ).setEphemeral(true).queue()
            return true
        }
        return false
    }

    private fun SlashCommandInteractionEvent.isTryingLink(
        tries: Link?,
        language: Languages
    ): Boolean {
        if (tries != null) {
            val linkedPlayer = Bukkit.getPlayer(tries.uuid)
            reply(
                TranslationCache.get(
                    language.code,
                    "commands.link.already_trying_to_link",
                    mapOf("player" to (linkedPlayer?.name ?: tries.uuid.toString()))
                )?.message
                    ?: "You are already trying to link to `${linkedPlayer?.name ?: tries.uuid.toString()}`. Run `/link` ingame."
            ).setEphemeral(true).queue()
            return true
        }
        return false
    }
}