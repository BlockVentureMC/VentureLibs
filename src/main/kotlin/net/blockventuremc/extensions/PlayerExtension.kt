package net.blockventuremc.extensions

import dev.fruxz.stacked.text
import net.blockventuremc.cache.ChatMessageCache
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.consts.BLOCK_PREFIX
import net.blockventuremc.consts.PREFIX
import net.blockventuremc.consts.TEXT_GRAY
import net.blockventuremc.database.functions.createDatabaseUser
import net.blockventuremc.database.functions.getDatabaseUserOrNull
import net.blockventuremc.database.model.BlockUser
import net.blockventuremc.modules.general.manager.RankManager
import net.blockventuremc.modules.general.model.Languages
import net.blockventuremc.modules.general.model.Rank
import net.blockventuremc.modules.general.model.Ranks
import net.blockventuremc.modules.i18n.TranslationCache
import net.blockventuremc.modules.i18n.model.Translation
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*


fun CommandSender.sendMessagePrefixed(message: String) = sendMessage(text(PREFIX + message).addToHistory(this.uniqueId))

fun CommandSender.sendMessageBlock(vararg lines: String) {
    sendEmtpyLine(true)
    sendMessage(text(BLOCK_PREFIX).addToHistory(this.uniqueId))
    sendEmtpyLine(true)
    lines.forEach { sendMessage(text(it).addToHistory(this.uniqueId)) }
    sendEmtpyLine(true)
}

fun CommandSender.sendEmtpyLine(addToHistory: Boolean = false) = sendMessage(text(" ").also { if(addToHistory) it.addToHistory(this.uniqueId) })

fun CommandSender.sendText(message: String) = sendMessage(text(TEXT_GRAY + message).addToHistory(this.uniqueId))
fun CommandSender.sendTextPrefixedIf(message: String, condition: Boolean) =
    if (condition) sendMessage(text(PREFIX + message).addToHistory(this.uniqueId)) else Unit

fun CommandSender.sendTextPrefixed(message: String) = sendMessage(text(PREFIX + message).addToHistory(this.uniqueId))

fun CommandSender.getInBox(vararg lines: String, color: String = "blue"): List<Component> {
    val components = mutableListOf<Component>()
    components.add(text("<color:$color><st>" + "-".repeat(51) + "</st></color>"))
    lines.forEach { components.add(text("<color:$color><b>|</b></color> $it")) }
    components.add(text("<color:$color><st>" + "-".repeat(51) + "</st></color>"))
    return components
}


fun Component.addToHistory(uuid: UUID): Component {
    ChatMessageCache.addMessage(uuid, this)
    return this
}

fun Component.addToHistory(): Component {
    ChatMessageCache.addMessageForAll(this)
    return this
}

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

val Player.rank: Rank
    get() = RankManager.getRankOfUser(uniqueId)

fun CommandSender.isRankOrHigher(ranks: Ranks): Boolean {
    return if (this is Player) {
        this.rank.isHigherOrEqual(ranks)
    } else {
        true
    }
}

val CommandSender.uniqueId: UUID
    get() = if (this is Player) this.uniqueId else UUID.fromString("00000000-0000-0000-0000-000000000000")

val Player.bitsPerMinute: Long
    get() = rank.bitsPerMinute