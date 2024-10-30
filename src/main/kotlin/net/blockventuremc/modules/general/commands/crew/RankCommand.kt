package net.blockventuremc.modules.general.commands.crew

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.*
import net.blockventuremc.modules.general.manager.RankManager
import net.blockventuremc.modules.general.model.Ranks
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@VentureCommand(
    name = "rank",
    description = "Set the rank of a player",
    permission = "blockventure.rank",
    permissionDefault = PermissionDefault.OP,
    aliases = ["perms", "ranks"]
)
class RankCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (args.size == 1 && args[0].equals("reload", true)) {
            RankManager.reloadRanks()
            sender.sendSuccess("Ranks reloaded.")
            return true
        }

        if (args.size < 2) {
            sender.sendError("/rank <player> <rank>")
            sender.sendMessageBlock(
                "Ranks:",
                Ranks.entries.joinToString(", ") { "<color:${it.rank.color}>${it.name}</color>" }
            )
            return true
        }

        val targetPlayer = sender.server.getOfflinePlayer(args[0])
        val rank = args[1]

        val realRank = Ranks.entries.find { it.name.equals(rank, true) }?.rank
        if (realRank == null) {
            sender.sendError(
                sender.translate("commands.rank_not_found", mapOf("rank" to args[0]))?.message ?: "Rank not found"
            )
            return true
        }

        RankManager.updateRank(realRank, targetPlayer.uniqueId)
        if (targetPlayer.isOnline) {
            targetPlayer.player?.sendSuccess(
                targetPlayer.player?.translate(
                    "commands.rank_changed",
                    mapOf("rank" to realRank.displayName, "color" to realRank.color)
                )?.message ?: "Your rank was updated to <color:${realRank.color}>$rank</color>."
            )
        }

        if (sender is Player) {
            sender.sendSuccessSound()

            if (sender.uniqueId != targetPlayer.uniqueId) {
                sender.sendSuccess(
                    sender.translate(
                        "commands.rank_changed_other",
                        mapOf("other" to targetPlayer.name, "rank" to realRank.displayName, "color" to realRank.color)
                    )?.message ?: "${targetPlayer.name}'s rank was set to  <color:${realRank.color}>$rank</color>."
                )
            }
            return true
        }

        sender.sendSuccess(
            sender.translate(
                "commands.rank_changed_other",
                mapOf("other" to targetPlayer.name, "rank" to realRank.displayName, "color" to realRank.color)
            )?.message ?: "${targetPlayer.name}'s rank was set to  <color:${realRank.color}>$rank</color>."
        )
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
                sender.server.onlinePlayers.map { it.name }.filter { it.startsWith(args[0]) }
            }

            2 -> {
                Ranks.entries.sortedByDescending { ranks: Ranks -> ranks.ordinal }.map { it.name }
                    .filter { it.startsWith(args[1]) }
            }

            else -> {
                listOf()
            }
        }
    }
}