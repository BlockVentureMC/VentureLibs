package net.blockventuremc.modules.discord.mc

import dev.kord.common.entity.Snowflake
import net.blockventuremc.BlockVenture
import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.database.functions.getLinkOfUser
import net.blockventuremc.database.functions.unlinkUser
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.modules.discord.manager.LinkManager
import net.blockventuremc.utils.mcroutine
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@BlockCommand(
    name = "unlink",
    description = "Unlink your Minecraft account from your Discord account",
    permissionDefault = PermissionDefault.TRUE,
    usage = "/unlink"
)
class UnlinkCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender as? Player ?: run {
            sender.sendMessagePrefixed("This command is only available to players.")
            return true
        }

        val linking = LinkManager.triesToGetLink(player.uniqueId)

        if (linking != null) {
            LinkManager.remove(player.uniqueId)
            sender.sendMessagePrefixed("You have successfully cancelled the linking process.")
            return true
        }

        val linked = getLinkOfUser(player.uniqueId)

        if (linked == null) {
            sender.sendMessagePrefixed("You are not linked to any Discord account.")
            return true
        }

        unlinkUser(player.uniqueId)

        mcroutine {
            val name = BlockVenture.bot.kord.getUser(Snowflake(linked.discordID))?.username ?: "Unknown"

            sender.sendMessagePrefixed("You have successfully unlinked your account from $name's Discord account.")

        }

        return true
    }
}