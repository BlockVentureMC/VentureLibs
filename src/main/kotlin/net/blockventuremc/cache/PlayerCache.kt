package net.blockventuremc.cache

import net.blockventuremc.database.model.DatabaseUser
import java.util.UUID

class PlayerCache {
    private var _cache = mapOf<UUID, DatabaseUser>()

    fun get(uuid: UUID): DatabaseUser? {
        return _cache[uuid]
    }
}