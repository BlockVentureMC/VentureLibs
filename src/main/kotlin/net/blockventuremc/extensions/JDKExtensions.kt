package net.blockventuremc.extensions

import net.blockventuremc.VentureLibs
import net.blockventuremc.modules.general.model.Languages
import net.dv8tion.jda.api.interactions.DiscordLocale
import org.slf4j.LoggerFactory

fun <T : Any> T.getLogger(): org.slf4j.Logger {
    return LoggerFactory.getLogger(VentureLibs::class.java)
}

fun <T : Any> T.nullIf(condition: (T) -> Boolean): T? {
    return if (condition(this)) null else this
}

val DiscordLocale.code: String
    get() = Languages.entries.firstOrNull { it.locale == this.toLocale() }?.getLanguageCode()
        ?: throw IllegalArgumentException("Locale $this is not supported")


fun Int.toFixedString(digits: Int = 3): String {
    return this.toString().padStart(digits, '0')
}