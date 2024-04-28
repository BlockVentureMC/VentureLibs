package net.blockventuremc.cache

import net.blockventuremc.database.functions.BoosterTable
import net.blockventuremc.database.functions.mapToBooster
import net.blockventuremc.database.model.BitBoosters
import net.blockventuremc.database.smartTransaction
import org.jetbrains.exposed.sql.selectAll

object BoosterCache {
    private val _cache = mutableListOf<BitBoosters>()

    fun load() = smartTransaction {
        BoosterTable.selectAll().forEach {
            val booster = mapToBooster(it)

            if (!booster.endTime.isExpired) {
                _cache.add(booster)
            }
        }
    }

    private fun getGlobalBoosters(): List<BitBoosters> {
        _cache.removeIf { it.endTime.isExpired }
        return _cache.filter { !it.user }
    }

    fun getUserBoosters(uuid: String): List<BitBoosters> {
        _cache.removeIf { it.endTime.isExpired }
        // get al boosters if userOnly is true and the owner is the same as the uuid
        val userBooster = _cache.filter { it.user && it.owner.toString() == uuid }
        val globalBooster = getGlobalBoosters()
        return userBooster + globalBooster
    }

    fun addBooster(booster: BitBoosters) {
        _cache.add(booster)
    }
}