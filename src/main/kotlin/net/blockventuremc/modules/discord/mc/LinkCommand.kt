package net.blockventuremc.modules.discord.mc

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.database.functions.getLinkOfUser
import net.blockventuremc.database.functions.linkUser
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.modules.discord.manager.LinkManager
import net.blockventuremc.modules.general.manager.RankManager
import net.blockventuremc.utils.mcasync
import net.blockventuremc.utils.mcroutine
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@VentureCommand(
    name = "link",
    description = "Link your Minecraft account to your Discord account",
    permissionDefault = PermissionDefault.TRUE,
    usage = "/link"
)
class LinkCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender as? Player ?: run {
            sender.sendMessagePrefixed("This command is only available to players.")
            return true
        }
        val linking = LinkManager.triesToGetLink(player.uniqueId)

        if (linking == null) {
            sender.sendMessagePrefixed("You aren't trying to link any account. If you want to link an account, use /link ${player.name} in Discord.")
            return true
        }

        val linked = getLinkOfUser(player.uniqueId)

        if (linked != null) {
            sender.sendMessagePrefixed("You are already linked to a Discord account. If you want to link another account, use /unlink in Discord or Minecraft.")
            return true
        }

        val link = LinkManager.getLink(player.uniqueId)!!

        linkUser(link)

        LinkManager.remove(player.uniqueId)

        mcroutine {
            mcasync {
                RankManager.updateDiscordRank(link.discordID)
            }
        }


        sender.sendMessagePrefixed("You have successfully linked your account to ${linking}'s Discord account.")

        return true
    }
}