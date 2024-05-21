package net.blockventuremc.modules.boosters

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