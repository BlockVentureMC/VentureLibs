package net.blockventuremc.modules.titles

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendError
import net.blockventuremc.extensions.sendSuccess
import net.blockventuremc.extensions.translate
import net.blockventuremc.modules.general.cache.PlayerCache
import net.blockventuremc.modules.titles.events.TitleChangedEvent
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@VentureCommand(
    name = "title",
    permission = "blockventuremc.title",
    permissionDefault = PermissionDefault.TRUE,
    description = "Manage your titles.",
    usage = "/title [<title>]"
)
class TitleCommand : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (args.size != 1) {
            TitleSelectionGui.instance.openInventory(sender)
            return true
        }

        val title = Title.entries.firstOrNull { titles: Title -> titles.name == args[0].uppercase() } ?: run {
            sender.sendError(
                sender.translate("title.command.not_found", mapOf("title" to args[0]))?.message
                    ?: "<color:#e74c3c>Title ${args[0]} not found."
            )
            return true
        }
        val blockPlayer = PlayerCache.getOrNull(sender.uniqueId) ?: return true

        if (!blockPlayer.titles.containsKey(title)) {
            sender.sendError(
                sender.translate(
                    "title.not_unlocked",
                    mapOf("title" to title.display(sender))
                )?.message
                    ?: "<color:#e74c3c>You have not unlocked the title <yellow>${title.display(sender)}</yellow> <color:#e74c3c>yet."
            )
            return true
        }

        blockPlayer.selectedTitle = title
        PlayerCache.updateCached(blockPlayer)

        val titleChangedEvent = TitleChangedEvent(sender, title)
        Bukkit.getPluginManager().callEvent(titleChangedEvent)

        sender.sendSuccess(
            blockPlayer.translate(
                "title.changed",
                mapOf("title" to title.display(sender))
            )?.message ?: "Your title has been changed to <yellow>${title.display(sender)}</yellow>!"
        )
        sender.playSound(sender, Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 0.4f, 1.3f)

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> {
                if (sender !is Player) return emptyList()
                val blockPlayer = PlayerCache.getOrNull(sender.uniqueId) ?: return emptyList()
                blockPlayer.titles.map { it.key.name }.filter { it.startsWith(args[0], true) }
            }

            else -> emptyList()
        }
    }

}