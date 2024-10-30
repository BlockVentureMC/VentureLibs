package net.blockventuremc.modules.general.commands.crew

import com.destroystokyo.paper.MaterialSetTag
import net.blockventuremc.VentureLibs
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.consts.BLOCKVENTURE_DOOR_LOCKS
import net.blockventuremc.extensions.*
import net.blockventuremc.modules.customblockdata.CustomBlockData
import net.blockventuremc.modules.general.model.Ranks
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import org.bukkit.persistence.PersistentDataType

@VentureCommand(
    name = "lock",
    description = "Lock a door",
    permission = "blockventure.lock",
    permissionDefault = PermissionDefault.OP,
    usage = "/lock",
)
class LockCommand : CommandExecutor, TabCompleter {


    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false

        if (args.isEmpty()) {
            sender.sendError(sender.translate("lock.usage")?.message ?: "Usage: /lock <rank>")
            return true
        }

        val rank = Ranks.entries.find { it.name.equals(args[0], true) }
        if (rank == null) {
            sender.sendError(
                sender.translate("commands.rank_not_found", mapOf("rank" to args[0]))?.message ?: "Rank not found"
            )
            sender.sendDeniedSound()
            return true
        }

        val lookingAtBlock = sender.getTargetBlockExact(5)
        if (lookingAtBlock == null || !MaterialSetTag.DOORS.isTagged(lookingAtBlock.type)) {
            sender.sendError(
                sender.translate("lock.no_door")?.message ?: "You must look at a door to lock it."
            )
            sender.sendDeniedSound()
            return true
        }

        val bottomBlock =
            if ((lookingAtBlock.blockData as Door).half == Bisected.Half.TOP) lookingAtBlock.getRelative(org.bukkit.block.BlockFace.DOWN) else lookingAtBlock

        val customBlockData = CustomBlockData(bottomBlock, VentureLibs.instance)
        customBlockData[BLOCKVENTURE_DOOR_LOCKS, PersistentDataType.BYTE] = rank.ordinal.toByte()

        sender.sendSuccess(
            sender.translate("lock.success", mapOf("rank" to rank.name))?.message
                ?: "The door is now locked to only open for <color:#f78fb3>${rank.name}</color> or higher."
        )
        sender.sendSuccessSound()
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        return Ranks.entries.sortedByDescending { ranks: Ranks -> ranks.ordinal }.map { it.name }
            .filter { it.startsWith(args[0]) }
    }
}