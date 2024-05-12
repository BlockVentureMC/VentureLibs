package net.blockventuremc.modules.general.commands.crew

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.cache.ChatMessageCache
import net.blockventuremc.extensions.sendMessageBlock
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@VentureCommand(
    name = "clearchat",
    permission = "venturechat.clearchat",
    description = "Clears the chat",
    usage = "/clearchat",
    permissionDefault = PermissionDefault.OP,
    aliases = ["cc"]
)
class ClearChatCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        for (player in sender.server.onlinePlayers) {
            sendChatClearMessage(player)
        }
        ChatMessageCache.clearMessages()
        return true
    }


    /**
     * Sends a chat clear message to the specified player.
     *
     * This method clears the chat by sending a large number of empty messages to the player. It also sends a
     * block of text containing a ghost that appears to spook the chat away.
     *
     * @param player the player to send the chat clear message to
     */
    private fun sendChatClearMessage(player: Player) {
        for (i in 0..1000) {
            player.sendMessage(Component.empty())
        }
        ChatMessageCache.initPlayer(player.uniqueId)
        player.sendMessageBlock(
            "<gray>    .-.       ",
            "<gray>   (o o) boo!  ",
            "<gray>   | O \\      ",
            "<gray>    \\   \\     <white>A ghost appears and spooks the chat away!",
            "<gray>     `~~~'     ",
            "<gray>               ",
            "<white>    *     *    ",
            "<white>   * *   * * "
        )
    }

}