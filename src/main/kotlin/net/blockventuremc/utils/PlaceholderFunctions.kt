package net.blockventuremc.utils

import dev.fruxz.stacked.extension.asPlainString
import dev.fruxz.stacked.text
import me.clip.placeholderapi.PlaceholderAPI
import net.blockventuremc.extensions.rank
import org.bukkit.Bukkit
import org.bukkit.entity.Player

private val urlRegex =
    Regex("http[s]?:\\/\\/(?:[a-zA-Z]|[0-9]|[\$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+")

private val commandRegex = Regex("/[a-zA-Z]+")

fun parsePlaceholders(text: String, player: Player): String {
    var parsed = text
    parsed = parsed.replace("%playername%", player.name)
    parsed = parsed.replace("%displayname%", player.displayName().asPlainString)
    parsed = parsed.replace("%color%", player.rank.color)
    parsed = parsed.replace("%rank%", player.rank.displayName)

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

    // Test for command and replace it with a clickable command
    for (match in commandRegex.findAll(plainText)) {
        val command = match.value
        val commandText = "<color:#7593ff><click:suggest_command:'$command'>$command</click></color>"
        parsed = parsed.replace(command, commandText)
    }


    return parsed
}