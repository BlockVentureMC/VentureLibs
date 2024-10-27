package net.blockventuremc.modules.general.cache

import dev.fruxz.ascend.tool.time.calendar.Calendar
import dev.fruxz.stacked.text
import net.blockventuremc.VentureLibs
import net.blockventuremc.database.functions.getDatabaseUserOrNull
import net.blockventuremc.database.functions.updateDatabaseUser
import net.blockventuremc.database.model.BlockUser
import net.blockventuremc.extensions.bitsPerMinute
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.extensions.toBlockUserDB
import net.blockventuremc.extensions.translate
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object PlayerCache {
    private var _cache = mapOf<UUID, BlockUser>()

    fun getOrNull(uuid: UUID): BlockUser? = _cache[uuid]

    fun get(uuid: UUID): BlockUser = _cache[uuid] ?: addToCache(uuid.toBlockUserDB())

    fun addToCache(user: BlockUser): BlockUser {
        _cache += Pair(user.uuid, user)
        return user
    }

    /**
     * Updates the given user in the database.
     *
     * @param user The user to be saved to the database.
     */
    fun saveToDB(user: BlockUser) {
        updateDatabaseUser(user)
    }

    fun updateCached(user: BlockUser): BlockUser {
        _cache -= user.uuid
        _cache += Pair(user.uuid, user)
        return user
    }

    fun remove(uuid: UUID) {
        _cache -= uuid
    }

    private fun loadPlayer(uuid: UUID): BlockUser {
        val player = getDatabaseUserOrNull(uuid) ?: return BlockUser(uuid, "Unknown")
        return addToCache(player)
    }

    fun reloadPlayer(uuid: UUID): BlockUser {
        return loadPlayer(uuid)
    }

    private var task: BukkitTask? = null
    fun runOnlineTimeScheduler() {
        var lastAutoSave = Calendar.now()
        var lastVentureTreasure = Calendar.now()
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(VentureLibs.instance, Runnable {
            if (Bukkit.getOnlinePlayers().isEmpty()) return@Runnable

            Bukkit.getOnlinePlayers().forEach { player ->
                val dbUser = get(player.uniqueId)
                dbUser.testForActivity()
                if (dbUser.afk) return@forEach

                updateCached(dbUser.copy(onlineTime = dbUser.onlineTime + 1.seconds))
            }

            // Give venture bits and xp every minute
            if (lastVentureTreasure.plus(1.minutes) < Calendar.now()) {
                Bukkit.getOnlinePlayers().forEach { player ->
                    val dbUser = get(player.uniqueId)
                    updateCached(
                        dbUser.copy(
                            ventureBits = dbUser.ventureBits + player.bitsPerMinute,
                            xp = dbUser.xp + 1
                        )
                    )
                }
                lastVentureTreasure = Calendar.now()
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