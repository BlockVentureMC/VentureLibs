package net.blockventuremc.modules.discord.manager

import net.blockventuremc.database.model.Link
import java.util.*

/**
 * Store for all setup processes on the link between a Minecraft account and a Discord account.
 */
object LinkManager {
    private val _cache = mutableMapOf<String, Link>()

    fun add(name: String, link: Link) {
        _cache[name] = link
    }

    fun remove(uuid: UUID) {
        _cache.remove(_cache.filter { it.value.uuid == uuid }.keys.firstOrNull())
    }

    fun triesToGetLink(uuid: UUID): String? {
        return _cache.filter { it.value.uuid == uuid }.keys.firstOrNull()
    }

    fun getLink(uuid: UUID): Link? {
        return _cache.values.find { it.uuid == uuid }
    }

    fun triesToGetLink(discordID: String): Link? {
        return _cache.values.find { it.discordID == discordID }
    }
}