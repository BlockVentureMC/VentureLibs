package net.blockventuremc.modules.boosters

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
            sendToMainChannel(
                DiscordChannelEnvs.ECONOMY_CHANNEL, EmbedBuilder()
                    .setTitle("Booster activated")
                    .setDescription("A new BitBooster has been activated by ${player.name}")
                    .addField("Bit Modifier", "+${boosters.modifier}", false)
                    .addField("Duration", (boosters.startTime.durationTo(boosters.endTime)).toString(), false)
                    .setColor(0xff9ff3)
                    .build()
            )?.queue()
        }
    }

    fun getModifiers(uuid: String): Long {
        val boosters = BoosterCache.getUserBoosters(uuid)
        return boosters.sumOf { it.modifier }
    }
}