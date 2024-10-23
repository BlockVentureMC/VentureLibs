package net.blockventuremc.database.functions

import net.blockventuremc.database.model.BitBoosters
import net.blockventuremc.database.toCalendar
import net.blockventuremc.modules.boosters.BoosterCategory
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import net.blockventuremc.database.smartTransaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.*

object BoosterTable : IntIdTable("boosters") {

    val userUUID = varchar("uuid", 45)

    val modifier = long("modifier").default(1)

    val startTime = timestamp("startTime")
    val endTime = timestamp("endTime")
    val userOnly = bool("userOnly").default(false)
    val category = enumerationByName("category", 20, BoosterCategory::class).default(BoosterCategory.USER_ACTIVATED)
}

fun mapToBooster(row: ResultRow): BitBoosters = with(row) {
    return BitBoosters(
        owner = UUID.fromString(this[BoosterTable.userUUID]),
        modifier = this[BoosterTable.modifier],
        startTime = this[BoosterTable.startTime].toCalendar(),
        endTime = this[BoosterTable.endTime].toCalendar(),
        user = this[BoosterTable.userOnly],
        category = this[BoosterTable.category]
    )
}

fun makeBooster(booster: BitBoosters) = smartTransaction {
    BoosterTable.insert {
        it[userUUID] = booster.owner.toString()
        it[modifier] = booster.modifier
        it[startTime] = booster.startTime.javaInstant
        it[endTime] = booster.endTime.javaInstant
        it[userOnly] = booster.user
        it[category] = booster.category
    }
}