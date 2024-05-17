package net.blockventuremc.modules.animations.faucets

import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.getLogger
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.Levelled
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Class representing a Faucet Animation.
 *
 * @param location the location where the faucet animation will be displayed
 * @param blockState the block state of the faucet
 */
class FaucetAnimation(
    private val location: Location,
    private val block: Block
) {

    companion object {
        private val objectCache = mutableListOf<String>()
        private val cacheLock: ReadWriteLock = ReentrantReadWriteLock()

        fun isObjectCached(loc: Location): Boolean {
            val locString = loc.toString()
            cacheLock.readLock().lock()
            val result = objectCache.contains(locString)
            cacheLock.readLock().unlock()
            return result
        }

        fun addObjectToCache(loc: Location) {
            val locString = loc.toString()
            cacheLock.writeLock().lock()
            objectCache.add(locString)
            cacheLock.writeLock().unlock()
        }

        fun removeObjectFromCache(loc: Location) {
            val locString = loc.toString()
            cacheLock.writeLock().lock()
            objectCache.remove(locString)
            cacheLock.writeLock().unlock()
        }
    }

    private var task: BukkitTask? = null

    init {
        start()
    }

    /**
     * Starts the faucet animation at the specified location.
     */
    private fun start() {
        if (isObjectCached(location)) return
        addObjectToCache(location)

        var timer = 40

        task = Bukkit.getScheduler().runTaskTimer(VentureLibs.instance, Runnable {

            if (timer <= 0) {
                block.type = Material.CAULDRON
                removeObjectFromCache(location)
                task?.cancel()
                return@Runnable
            }

            timer--

            if (timer > 20) {
                playWaterAnimation()
            }

            if (timer in listOf(35, 26, 21, 15, 11, 1)) {
                updateWaterLevel(timer)
            }

        }, 5L, 5L)
    }

    /**
     * Plays a water animation effect around the location of the faucet.
     */
    private fun playWaterAnimation() {
        location.world?.getNearbyEntities(location, 5.0, 5.0, 5.0)?.forEach { entity ->
            if (entity is Player) {
                val effectLocation = location.clone().add(0.5, 0.35, 0.5)
                entity.apply {
                    spawnParticle(Particle.DRIPPING_DRIPSTONE_WATER, effectLocation, 3)
                    spawnParticle(Particle.SPLASH, effectLocation, 1)
                    playSound(effectLocation, Sound.ITEM_BUCKET_FILL, SoundCategory.PLAYERS, 0.1f, 1.7f)
                }
            }
        }
    }

    /**
     * Updates the water level of a cauldron block based on the given timer value.
     *
     * @param timer the timer value used to determine the new water level
     */
    private fun updateWaterLevel(timer: Int) {
        Bukkit.getScheduler().runTask(VentureLibs.instance, Runnable {
            if (block.type == Material.CAULDRON) {
                block.type = Material.WATER_CAULDRON
            }
            val blockData = block.blockData as Levelled
            val newData = when (timer) {
                21 -> blockData.maximumLevel
                35, 11 -> 1
                15, 26 -> 2
                else -> blockData.minimumLevel
            }
            // Aktualisiere BlockData direkt, ohne veraltete Byte-Daten zu verwenden.
            blockData.apply {
                level = newData
                block.blockData = this
            }
            block.state.update(true, false)
        })
    }
}