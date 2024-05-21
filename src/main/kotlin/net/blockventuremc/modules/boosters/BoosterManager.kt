package net.blockventuremc.modules.boosters

import com.google.common.reflect.ClassPath
import io.sentry.Sentry
import net.blockventuremc.VentureLibs
import net.blockventuremc.audioserver.common.extensions.getLogger
import net.blockventuremc.cache.BoosterCache
import net.blockventuremc.database.functions.makeBooster
import net.blockventuremc.database.model.BitBoosters
import net.blockventuremc.modules.discord.manager.ChannelManager
import net.blockventuremc.utils.mcasyncBlocking
import org.bukkit.Bukkit

object BoosterManager {
    fun addBooster(boosters: BitBoosters) {
        makeBooster(boosters)
        BoosterCache.addBooster(boosters)

        val p = Bukkit.getPlayer(boosters.owner) ?: return

        // use unixtimestamp for endTime

        mcasyncBlocking {
            val classLoader = VentureLibs.instance.javaClass.classLoader
            try {
                classLoader.loadClass("dev.kord.rest.builder.message.EmbedBuilder").kotlin
            } catch (e: ClassNotFoundException) {
                Sentry.captureException(e)
                e.printStackTrace()
                getLogger().error("Failed to load class: ${e.message}")
                return@mcasyncBlocking
            }
            ChannelManager.sendEconomy {
                title = "Booster Purchased"
                description = "A new booster has been purchased by ${p.name}"
                field {
                    name = "Modifier"
                    value = boosters.modifier.toString()
                }
                field {
                    name = "Category"
                    value = boosters.category.name
                }
            }
        }
    }

    fun getModifiers(uuid: String): Long {
        val boosters = BoosterCache.getUserBoosters(uuid)
        return boosters.sumOf { it.modifier }
    }
}