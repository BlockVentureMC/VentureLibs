package net.blockventuremc.extensions

import org.slf4j.LoggerFactory

fun <T : Any> T.getLogger(): org.slf4j.Logger {
    return LoggerFactory.getLogger(this::class.java)
}

fun <T : Any> T.nullIf(condition: (T) -> Boolean): T? {
    return if (condition(this)) null else this
}