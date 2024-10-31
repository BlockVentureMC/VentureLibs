package net.blockventuremc.extensions

import dev.fruxz.stacked.text
import net.blockventuremc.consts.BLOCK_PREFIX
import net.blockventuremc.consts.MessageFormat
import net.blockventuremc.consts.PREFIX
import net.blockventuremc.consts.TEXT_GRAY
import net.blockventuremc.database.functions.createDatabaseUser
import net.blockventuremc.database.functions.getDatabaseUserOrNull
import net.blockventuremc.database.model.BlockUser
import net.blockventuremc.modules.boosters.BoosterManager
import net.blockventuremc.modules.general.cache.PlayerCache
import net.blockventuremc.modules.general.manager.RankManager
import net.blockventuremc.modules.general.model.Languages
import net.blockventuremc.modules.general.model.Rank
import net.blockventuremc.modules.general.model.Ranks
import net.blockventuremc.modules.i18n.TranslationCache
import net.blockventuremc.modules.i18n.model.Translation
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@Deprecated("Use the new message System")
fun CommandSender.sendMessagePrefixed(message: String) = sendMessage(text(PREFIX + message))

fun CommandSender.sendMessageBlock(vararg lines: String) {
    sendEmtpyLine()
    sendMessage(text(BLOCK_PREFIX))
    sendEmtpyLine()
    lines.forEach {
        if (it.isNotEmpty()) sendMessage(text(it))
    }
    sendEmtpyLine()
}

fun CommandSender.sendMessageFormated(message: String, format: MessageFormat, args: String? = null) = format.sendCommandSender(this, message, args)

// TODO: auto use translation system and add placeholders and replace msg

fun CommandSender.sendInfo(message: String) = sendMessageFormated(message, MessageFormat.INFO)
fun CommandSender.sendWarning(message: String) = sendMessageFormated(message, MessageFormat.WARNING)
fun CommandSender.sendError(message: String) = sendMessageFormated(message, MessageFormat.ERROR)
fun CommandSender.sendSuccess(message: String) = sendMessageFormated(message, MessageFormat.SUCCESS)
fun CommandSender.sendLink(message: String, url: String) = sendMessageFormated(message, MessageFormat.LINK, url)
fun CommandSender.sendCommand(message: String, command: String) = sendMessageFormated(message, MessageFormat.COMMAND, command)
fun CommandSender.sendLocked(message: String) = sendMessageFormated(message, MessageFormat.LOCKED)

fun CommandSender.sendEmtpyLine() = sendMessage(text(" "))

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
    get() = gameMode == GameMode.SPECTATOR || (this.rank.isHigherOrEqual(Ranks.BUILDER) && hasBuildTag)

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

val Player.rank: Rank
    get() = RankManager.getRankOfUser(uniqueId)

fun CommandSender.isRankOrHigher(ranks: Ranks): Boolean {
    return if (this is Player) {
        this.rank.isHigherOrEqual(ranks)
    } else {
        true
    }
}

val Player.bitsPerMinute: Long
    get() = rank.bitsPerMinute + BoosterManager.getModifiers(uniqueId.toString())