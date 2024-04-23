package net.blockventuremc.cache

import net.blockventuremc.BlockVenture
import net.blockventuremc.database.model.DatabaseUser
import net.blockventuremc.extensions.toDatabaseUserDB
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

object PlayerCache {
    private var _cache = mapOf<UUID, DatabaseUser>()

    fun get(uuid: UUID): DatabaseUser = _cache[uuid] ?: register(uuid.toDatabaseUserDB())

    private fun register(user: DatabaseUser): DatabaseUser {
        _cache += Pair(user.uuid, user)
        return user
    }


    private fun updateCached(user: DatabaseUser): DatabaseUser {
        _cache -= user.uuid
        _cache += Pair(user.uuid, user)
        return user
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