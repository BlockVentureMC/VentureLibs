package net.blockventuremc.consts

import dev.fruxz.stacked.text
import net.blockventuremc.utils.SoundEffect
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

enum class MessageFormat(val format: String, val sound: SoundEffect? = null) {

    INFO("<color:#CAD3C8>❗ %s"),
    LINK("<color:#1B9CFC>🔗 <click:open_url:'%s'><hover:show_text:'\uF822 <color:#1B9CFC>Click to open link'>%s</hover></click>"),
    COMMAND("\uF822 <color:#FFD700><click:run_command:'%s'><hover:show_text:'\uF822 <color:#FFD700>Click to run command'>%s</hover></click>"),
    WARNING("<color:#F8EFBA>‼ %s"),
    ERROR("<color:#FD7272>🪲 %s"),
    SUCCESS("<color:#55E6C1>✔ %s"),
    LOCKED("<gradient:#FC427B:#FD7272>🔒 %s"),
    ;

    fun sendPlayer(player: Player, message: String, args: String? = null) {
        if (args == null) player.sendMessage(text(format.format(message)))
        else player.sendMessage(text(format.format(args, message)))
        sound?.let { player.playSound(player.location, it.sound, it.volume, it.pitch) }
    }

    fun sendCommandSender(sender: CommandSender, message: String, args: String? = null) {
        if (args == null) sender.sendMessage(text(format.format(message)))
        else sender.sendMessage(text(format.format(args, message)))
        if (sender is Player) sound?.let { sender.playSound(sender.location, it.sound, it.volume, it.pitch) }
    }
}