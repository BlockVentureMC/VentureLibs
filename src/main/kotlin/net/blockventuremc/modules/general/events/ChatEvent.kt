package net.blockventuremc.modules.general.events

import dev.fruxz.stacked.extension.asPlainString
import dev.fruxz.stacked.text
import io.papermc.paper.event.player.AsyncChatEvent
import me.clip.placeholderapi.PlaceholderAPI
import net.blockventuremc.extensions.toDatabaseUser
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatEvent: Listener {

    private val chatFormat = "<color:%color%>%rank% <color:#3d3d3d>»</color> <color:#c8d6e5>%playername%</color> <color:#f6e58d>"

    private val urlRegex =
        Regex("http[s]?:\\/\\/(?:[a-zA-Z]|[0-9]|[\$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+")

    @EventHandler
    fun onChat(event: AsyncChatEvent): Unit = with(event) {

        event.renderer { _, _, _, _ ->
            return@renderer text(parsePlaceholders(chatFormat, player)).append(text(parsePlaceholders(message().asPlainString, player)))
        }
    }

    private fun parsePlaceholders(text: String, player: Player): String {
        var parsed = text
        parsed = parsed.replace("%playername%", player.name)
        parsed = parsed.replace("%displayname%", player.displayName().asPlainString)
        parsed = parsed.replace("%color%", player.toDatabaseUser().rank.color)
        parsed = parsed.replace("%rank%", player.toDatabaseUser().rank.name)

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            parsed = PlaceholderAPI.setPlaceholders(player, parsed)
        }

        val plainText = text(parsed).asPlainString

        // Test for link and replace it with a clickable link
        for (match in urlRegex.findAll(plainText)) {
            val url = match.value
            val urlText = "<color:#7593ff><click:open_url:'$url'>$url</click></color>"
            parsed = parsed.replace(url, urlText)
        }
        return parsed
    }
}