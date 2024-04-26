package net.blockventuremc.cache

import dev.fruxz.ascend.tool.time.calendar.Calendar
import dev.fruxz.stacked.text
import net.blockventuremc.BlockVenture
import net.blockventuremc.database.functions.getDatabaseUserOrNull
import net.blockventuremc.database.functions.updateDatabaseUser
import net.blockventuremc.database.model.DatabaseUser
import net.blockventuremc.extensions.bitsPerMinute
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.extensions.toDatabaseUserDB
import net.blockventuremc.extensions.translate
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
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
        var lastAutoSave = Calendar.now()
        var lastVentureTreassure = Calendar.now()
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(BlockVenture.instance, Runnable {
            if (Bukkit.getOnlinePlayers().isEmpty()) return@Runnable

            Bukkit.getOnlinePlayers().forEach { player ->
                val dbUser = get(player.uniqueId)
                dbUser.testForActivity()
                if (dbUser.afk) return@forEach

                updateCached(dbUser.copy(onlineTime = dbUser.onlineTime + 1.seconds))
            }

            // Give venture bits every minute
            if (lastVentureTreassure.plus(1.minutes) < Calendar.now()) {
                Bukkit.getOnlinePlayers().forEach { player ->
                    val dbUser = get(player.uniqueId)
                    updateCached(dbUser.copy(ventureBits = dbUser.ventureBits + dbUser.bitsPerMinute))
                }
                lastVentureTreassure = Calendar.now()
            }

            // Save all players every 15 minutes
            if (lastAutoSave.plus(15.minutes) < Calendar.now()) {
                getLogger().info("Auto-saving all players to the database...")
                _cache.values.forEach { saveToDB(it) }
                lastAutoSave = Calendar.now()
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.sendActionBar(
                        text(
                            player.translate("messages.auto-save")?.message
                                ?: "<color:#4cd137>Your data was saved automatically."
                        )
                    )
                }
            }
        }, 20L, 20L)
    }

    fun cleanup() {
        task?.cancel()
    }
}