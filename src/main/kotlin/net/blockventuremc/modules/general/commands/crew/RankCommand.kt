package net.blockventuremc.modules.general.commands.crew

import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.toDatabaseUser
import net.blockventuremc.modules.general.model.Ranks
import org.bukkit.command.CommandExecutor
import org.bukkit.command.TabCompleter
import org.bukkit.permissions.PermissionDefault
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.map
import kotlin.collections.sortedByDescending
import kotlin.text.equals
import kotlin.text.startsWith

@BlockCommand(
    name = "rank",
    description = "Set the rank of a player",
    permission = "blockventure.rank",
    permissionDefault = PermissionDefault.OP,
    aliases = ["perms"]
)
class RankCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if(args.size < 2) {
            sender.sendMessagePrefixed("/rank <player> <rank>")
            return true
        }

        val targetPlayer = sender.server.getOfflinePlayer(args[0])
        var targetCringeUser = targetPlayer.uniqueId.toDatabaseUser()
        val rank = args[1]

        val realRank = Ranks.entries.find { it.name.equals(rank, true) }
        if(realRank == null) {
            sender.sendMessagePrefixed("<red>Rank not found")
            return true
        }

        targetCringeUser = targetCringeUser.copy(rank = realRank)
        PlayerCache.updateCached(targetCringeUser)
        if(!targetPlayer.isOnline) {
            PlayerCache.remove(targetPlayer.uniqueId)
        } else {
            targetPlayer.player?.sendMessage("§aDein Rang wurde auf §e${rank}§a geändert.")
        }

        sender.sendMessagePrefixed("Rang von ${targetPlayer.name} auf <${realRank.color}>$rank<reset> gesetzt")

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        return when(args.size) {
            1 -> {
                sender.server.onlinePlayers.map { it.name }.filter { it.startsWith(args[0]) }
            }

            2 -> {
                Ranks.entries.sortedByDescending { ranks: Ranks -> ranks.ordinal }.map { it.name }.filter { it.startsWith(args[1]) }
            }

            else -> {
                listOf()
            }
        }
    }
}