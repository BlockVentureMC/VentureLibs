package net.blockventuremc.database.functions

import net.blockventuremc.database.functions.TableUsers.userUUID
import net.blockventuremc.database.model.DatabaseAchievement
import net.blockventuremc.database.smartTransaction
import net.blockventuremc.database.toCalendar
import net.blockventuremc.modules.archievements.model.Achievement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.*

object TableAchievements: Table("achievements") {
    val userUUID = varchar("uuid", 45)

    val achievement = enumerationByName("achievement", 24, Achievement::class)

    val gottenAt = timestamp("gottenAt").defaultExpression(CurrentTimestamp())
}

private fun mapToDatabaseAchievement(row: ResultRow): DatabaseAchievement = with(row) {
    return DatabaseAchievement(
        uuid = UUID.fromString(this[userUUID]),
        achievement = this[TableAchievements.achievement],

        gottenAt = this[TableAchievements.gottenAt].toCalendar()
    )
}

fun addAchievementToUser(achievement: DatabaseAchievement) = smartTransaction {
    TableAchievements.insert {
        it[userUUID] = achievement.uuid.toString()
        it[TableAchievements.achievement] = achievement.achievement
        it[gottenAt] = achievement.gottenAt.javaInstant
    }
}

fun getAchievementOfUser(uuid: UUID, achievement: Achievement): DatabaseAchievement? = smartTransaction {
    return@smartTransaction TableAchievements.selectAll().where { TableAchievements.userUUID eq uuid.toString() and (TableAchievements.achievement eq achievement) }.firstOrNull()?.let(::mapToDatabaseAchievement)
}