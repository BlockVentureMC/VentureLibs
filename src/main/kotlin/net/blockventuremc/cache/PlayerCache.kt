package net.blockventuremc.cache

import net.blockventuremc.BlockVenture
import net.blockventuremc.database.functions.getDatabaseUserOrNull
import net.blockventuremc.database.functions.updateDatabaseUser
import net.blockventuremc.database.model.DatabaseUser
import net.blockventuremc.extensions.toDatabaseUserDB
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

object PlayerCache {
    private var _cache = mapOf<UUID, DatabaseUser>()

    fun getOrNull(uuid: UUID): DatabaseUser? = _cache[uuid]

    fun get(uuid: UUID): DatabaseUser = _cache[uuid] ?: addToCache(uuid.toDatabaseUserDB())

    fun addToCache(user: DatabaseUser): DatabaseUser {
        _cache += Pair(user.uuid, user)
        return user
    }

    /**
     * Updates the given user in the database.
     *
     * @param user The user to be saved to the database.
     */
    fun saveToDB(user: DatabaseUser) {
        updateDatabaseUser(user)
    }

    fun updateCached(user: DatabaseUser): DatabaseUser {
        _cache -= user.uuid
        _cache += Pair(user.uuid, user)
        return user
    }

    fun remove(uuid: UUID) {
        _cache -= uuid
    }

    private fun loadPlayer(uuid: UUID): DatabaseUser {
        val player = getDatabaseUserOrNull(uuid) ?: return DatabaseUser(uuid, "Unknown")
        return addToCache(player)
    }

    fun reloadPlayer(uuid: UUID): DatabaseUser {
        return loadPlayer(uuid)
    }

    private var task: BukkitTask? = null
    fun runOnlineTimeScheduler() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(BlockVenture.instance, Runnable {
            Bukkit.getOnlinePlayers().forEach { player ->
                val dbUser = get(player.uniqueId)
                dbUser.testForActivity()
                if (dbUser.afk) return@forEach

                updateCached(dbUser.copy(onlineTime = dbUser.onlineTime + 1.seconds))
            }
        }, 20L, 20L)
    }

    fun cleanup() {
        task?.cancel()
    }
}