package net.blockventuremc.modules.general.commands.guests

import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.sendSuccessSound
import net.blockventuremc.extensions.translate
import net.blockventuremc.modules.general.model.Languages
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@BlockCommand(
    name = "language",
    permission = "blockventure.language",
    description = "Change your language",
    usage = "/language <language>",
    permissionDefault = PermissionDefault.TRUE,
    aliases = ["lang", "sprache"]
)
class LanguageCommand : CommandExecutor, TabExecutor {


    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return true

        if (args.isEmpty()) {
            sender.sendMessagePrefixed(sender.translate("commands.language.usage")?.message ?: "Usage: /language <color:#f78fb3><language>")
            return true
        }

        val language = Languages.entries.firstOrNull { it.name.equals(args[0], ignoreCase = true) } ?: run {
            sender.sendMessagePrefixed(sender.translate("commands.language.invalid", mapOf(
                "languages" to Languages.entries.joinToString(", ") { it.name }
            ))?.message ?: "Invalid language")
            return true
        }

        val blockPlayer = PlayerCache.getOrNull(sender.uniqueId) ?: return true
        PlayerCache.updateCached(blockPlayer.copy(language = language))

        sender.sendMessagePrefixed(sender.translate("commands.language.changed", mapOf(
            "language" to language.name
        ))?.message ?: "Language changed to ${language.name}")

        sender.sendSuccessSound()
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        return Languages.entries.map { it.name }.filter { it.startsWith(args[0], ignoreCase = true) }
    }
}