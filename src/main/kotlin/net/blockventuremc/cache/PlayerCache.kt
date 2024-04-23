package net.blockventuremc.cache

import net.blockventuremc.database.model.DatabaseUser
import net.blockventuremc.extensions.toDatabaseUserDB
import java.util.UUID

object PlayerCache {
    private var _cache = mapOf<UUID, DatabaseUser>()

    fun get(uuid: UUID): DatabaseUser = _cache[uuid] ?: register(uuid.toDatabaseUserDB())

    private fun register(user: DatabaseUser): DatabaseUser {
        _cache += Pair(user.uuid, user)
        return user
    }
}