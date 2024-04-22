package net.blockventuremc.extensions

import dev.fruxz.stacked.text
import net.blockventuremc.consts.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.*


fun CommandSender.sendMessagePrefixed(message: String) = sendMessage(text(PREFIX + message))

fun CommandSender.sendMessageBlock(vararg lines: String) {
    sendEmtpyLine()
    sendMessage(text(BLOCK_PREFIX))
    sendEmtpyLine()
    lines.forEach { sendMessage(text(it)) }
    sendEmtpyLine()
}

fun CommandSender.sendEmtpyLine() = sendMessage(text(" "))

fun CommandSender.sendText(message: String) = sendMessage(text(TEXT_GRAY + message))
fun CommandSender.sendTextPrefixedIf(message: String, condition: Boolean) = if(condition) sendMessage(text(PREFIX + message)) else Unit
fun CommandSender.sendTextPrefixed(message: String) = sendMessage(text(PREFIX + message))


fun UUID.toOfflinePlayer(): OfflinePlayer {
    return Bukkit.getOfflinePlayer(this)
}

fun String.toOfflinePlayer(): OfflinePlayer {
    return Bukkit.getOfflinePlayer(this)
}

fun String.toOfflinePlayerIfCached(): OfflinePlayer? {
    return Bukkit.getOfflinePlayerIfCached(this)
}