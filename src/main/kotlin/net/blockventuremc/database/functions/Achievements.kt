package net.blockventuremc.database.functions

import net.blockventuremc.database.model.DatabaseAchievement
import net.blockventuremc.database.smartTransaction
import net.blockventuremc.database.toCalendar
import net.blockventuremc.modules.achievements.model.Achievement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.*

object TableAchievements : Table("achievements") {
    val userUUID = varchar("uuid", 45)

    val achievement = enumerationByName("achievement", 24, Achievement::class)
    val receivedAt = timestamp("receivedAt")

    val counter = integer("counter").default(1)
    val lastReceivedAt = timestamp("lastReceivedAt")


    override val primaryKey = PrimaryKey(userUUID, achievement)
}

private fun mapToDatabaseAchievement(row: ResultRow): DatabaseAchievement = with(row) {
    return DatabaseAchievement(
        uuid = UUID.fromString(this[TableAchievements.userUUID]),
        achievement = this[TableAchievements.achievement],
        receivedAt = this[TableAchievements.receivedAt].toCalendar(),
        counter = this[TableAchievements.counter],
        lastReceivedAt = this[TableAchievements.lastReceivedAt].toCalendar()
    )
}

fun addAchievementToUser(achievement: DatabaseAchievement) = smartTransaction {
    TableAchievements.replace {
        it[userUUID] = achievement.uuid.toString()
        it[TableAchievements.achievement] = achievement.achievement
        it[receivedAt] = achievement.receivedAt.javaInstant
        it[counter] = achievement.counter
        it[lastReceivedAt] = achievement.lastReceivedAt.javaInstant
    }
}

fun getAchievementOfUser(uuid: UUID, achievement: Achievement): DatabaseAchievement? = smartTransaction {
    return@smartTransaction TableAchievements.selectAll()
        .where { TableAchievements.userUUID eq uuid.toString() and (TableAchievements.achievement eq achievement) }
        .firstOrNull()?.let(::mapToDatabaseAchievement)
}