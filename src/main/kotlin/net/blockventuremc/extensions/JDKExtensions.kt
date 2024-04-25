package net.blockventuremc.extensions

import dev.kord.common.Locale
import org.slf4j.LoggerFactory

fun <T : Any> T.getLogger(): org.slf4j.Logger {
    return LoggerFactory.getLogger(this::class.java)
}

fun <T : Any> T.nullIf(condition: (T) -> Boolean): T? {
    return if (condition(this)) null else this
}

val Locale.code: String
    get() = when (this) {
        Locale.GERMAN -> "de-DE"
        Locale.ENGLISH_UNITED_STATES -> "en-US"
        else -> "en-US"
    }
