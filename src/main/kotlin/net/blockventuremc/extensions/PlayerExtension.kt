package net.blockventuremc.extensions

import dev.fruxz.stacked.text
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.consts.*
import net.blockventuremc.database.functions.createDatabaseUser
import net.blockventuremc.database.functions.getDatabaseUserOrNull
import net.blockventuremc.database.model.BlockUser
import net.blockventuremc.modules.general.model.Languages
import net.blockventuremc.modules.general.model.Ranks
import net.blockventuremc.modules.i18n.TranslationCache
import net.blockventuremc.modules.i18n.model.Translation
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
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
fun CommandSender.sendTextPrefixedIf(message: String, condition: Boolean) =
    if (condition) sendMessage(text(PREFIX + message)) else Unit

fun CommandSender.sendTextPrefixed(message: String) = sendMessage(text(PREFIX + message))

fun Player.sendDeniedSound() = playSound(location, "minecraft:block.note_block.bass", 1f, 1f)
fun CommandSender.sendDeniedSound(): Boolean {
    return if (this is Player) {
        sendDeniedSound()
        true
    } else {
        false
    }
}

fun Player.sendSuccessSound() = playSound(location, "minecraft:block.note_block.pling", 1f, 1f)
fun CommandSender.sendSuccessSound(): Boolean {
    return if (this is Player) {
        sendSuccessSound()
        true
    } else {
        false
    }
}

fun Player.sendTeleportSound() = playSound(location, "minecraft:block.note_block.harp", 1f, 1f)
fun CommandSender.sendTeleportSound(): Boolean {
    return if (this is Player) {
        sendTeleportSound()
        true
    } else {
        false
    }
}

fun Player.sendOpenSound() = playSound(location, "minecraft:block.note_block.chime", 1f, 1f)
fun CommandSender.sendOpenSound(): Boolean {
    return if (this is Player) {
        sendOpenSound()
        true
    } else {
        false
    }
}


fun UUID.toOfflinePlayer(): OfflinePlayer {
    return Bukkit.getOfflinePlayer(this)
}

fun String.toOfflinePlayer(): OfflinePlayer {
    return Bukkit.getOfflinePlayer(this)
}

fun String.toOfflinePlayerIfCached(): OfflinePlayer? {
    return Bukkit.getOfflinePlayerIfCached(this)
}


val Player.canBuild: Boolean
    get() = gameMode == GameMode.SPECTATOR || (this.toBlockUser().rank.isHigherOrEqual(Ranks.Trial) && hasBuildTag)

var Player.hasBuildTag: Boolean
    get() = this.scoreboardTags.contains("builder")
    set(value) {
        if (value) this.addScoreboardTag("builder") else this.removeScoreboardTag("builder")
    }

fun BlockUser.translate(message: String, placeholders: Map<String, Any?> = emptyMap()): Translation? {
    return TranslationCache.get(language.getLanguageCode(), message, placeholders)
}

fun CommandSender.translate(message: String, placeholders: Map<String, Any?> = emptyMap()): Translation? {
    if (this is Player) return toBlockUser().translate(message, placeholders)
    return TranslationCache.get(Languages.EN.getLanguageCode(), message, placeholders)
}

fun Player.translate(message: String, placeholders: Map<String, Any?> = emptyMap()): Translation? {
    return toBlockUser().translate(message, placeholders)
}

fun Player.toBlockUser(): BlockUser {
    return PlayerCache.get(uniqueId)
}

fun UUID.toBlockUser(): BlockUser {
    return PlayerCache.get(this)
}

fun UUID.toBlockUserDB(): BlockUser {
    return getDatabaseUserOrNull(this) ?: createDatabaseUser(
        BlockUser(
            this,
            Bukkit.getPlayer(this)?.name ?: Bukkit.getOfflinePlayer(this).name ?: "Unknown"
        )
    )
}

fun CommandSender.isRankOrHigher(rank: Ranks): Boolean {
    return if (this is Player) {
        this.toBlockUser().rank.isHigherOrEqual(rank)
    } else {
        true
    }
}

val BlockUser.bitsPerMinute: Long
    get() = (rank.bitsPerMinute).toLong()