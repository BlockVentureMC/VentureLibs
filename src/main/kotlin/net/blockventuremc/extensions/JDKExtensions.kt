package net.blockventuremc.extensions

import dev.kord.common.Locale
import dev.kord.common.asJavaLocale
import net.blockventuremc.modules.general.model.Languages
import org.slf4j.LoggerFactory

fun <T : Any> T.getLogger(): org.slf4j.Logger {
    return LoggerFactory.getLogger(this::class.java)
}

fun <T : Any> T.nullIf(condition: (T) -> Boolean): T? {
    return if (condition(this)) null else this
}

val Locale.code: String
    get() = Languages.entries.firstOrNull { it.locale == this.asJavaLocale() }?.getLanguageCode()
        ?: throw IllegalArgumentException("Locale $this is not supported")
