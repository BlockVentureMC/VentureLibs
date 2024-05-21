package net.blockventuremc.modules.boosters

import io.sentry.Sentry
import net.blockventuremc.VentureLibs
import net.blockventuremc.audioserver.common.extensions.getLogger
import net.blockventuremc.cache.BoosterCache
import net.blockventuremc.database.functions.makeBooster
import net.blockventuremc.database.model.BitBoosters
import net.blockventuremc.modules.discord.DiscordChannelEnvs
import net.blockventuremc.modules.discord.manager.sendToMainChannel
import net.blockventuremc.utils.mcasyncBlocking
import net.dv8tion.jda.api.EmbedBuilder
import org.bukkit.Bukkit

object BoosterManager {
    fun addBooster(boosters: BitBoosters) {
        makeBooster(boosters)
        BoosterCache.addBooster(boosters)

        val player = Bukkit.getOfflinePlayer(boosters.owner)

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
            sendToMainChannel(DiscordChannelEnvs.ECONOMY_CHANNEL, EmbedBuilder()
                .setTitle("Booster Purchased")
                .setDescription("A new booster has been purchased by ${player.name}")
                .addField("Modifier", boosters.modifier.toString(), false)
                .addField("Category", boosters.category.name, false)
                .build()
            )?.queue()
        }
    }

    fun getModifiers(uuid: String): Long {
        val boosters = BoosterCache.getUserBoosters(uuid)
        return boosters.sumOf { it.modifier }
    }
}