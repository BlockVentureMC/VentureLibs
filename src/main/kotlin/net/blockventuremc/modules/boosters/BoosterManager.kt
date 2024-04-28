package net.blockventuremc.modules.boosters

import net.blockventuremc.cache.BoosterCache
import net.blockventuremc.database.functions.makeBooster
import net.blockventuremc.database.model.BitBoosters

object BoosterManager {
    fun addBooster(boosters: BitBoosters) {
        makeBooster(boosters)
        BoosterCache.addBooster(boosters)
    }

    fun getModifiers(uuid: String): Long {
        val boosters = BoosterCache.getUserBoosters(uuid)
        return boosters.sumOf { it.modifier }
    }
}