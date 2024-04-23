package net.blockventuremc.database.functions

import net.blockventuremc.database.functions.TableUsers.userUUID
import net.blockventuremc.database.model.DatabaseUser
import net.blockventuremc.database.smartTransaction
import net.blockventuremc.database.toCalendar
import net.blockventuremc.modules.general.model.Languages
import net.blockventuremc.modules.general.model.Ranks
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.*
import kotlin.time.Duration.Companion.seconds

object TableUsers : Table("users") {
    val userUUID = varchar("uuid", 45)
    val userName = varchar("username", 24)
    val userRank = enumerationByName("rank", 24, Ranks::class).default(Ranks.Default)
    val userLanguage = enumerationByName("language", 2, Languages::class).default(Languages.EN)

    val userFirstJoined = timestamp("firstJoined").defaultExpression(CurrentTimestamp())
    val userLastJoined = timestamp("lastTimeOnline").defaultExpression(CurrentTimestamp())
    val onlineTime = long("onlineTime").default(0)

    override val primaryKey = PrimaryKey(userUUID)
}


fun getDatabaseUserOrNull(uuid: UUID): DatabaseUser? = smartTransaction {
    return@smartTransaction TableUsers.selectAll().where { userUUID eq uuid.toString() }.firstOrNull()?.let(::mapToDatabaseUser)
}

private fun mapToDatabaseUser(row: ResultRow): DatabaseUser = with(row) {
    return DatabaseUser(
        uuid = UUID.fromString(this[userUUID]),
        username = this[TableUsers.userName],
        rank = this[TableUsers.userRank],
        language = this[TableUsers.userLanguage],
        firstJoined = this[TableUsers.userFirstJoined].toCalendar(),
        lastTimeJoined = this[TableUsers.userLastJoined].toCalendar(),
        onlineTime = this[TableUsers.onlineTime].seconds,
    )
}


fun createDatabaseUser(user: DatabaseUser): DatabaseUser = smartTransaction {
    TableUsers.insert {
        it[userUUID] = user.uuid.toString()
        it[userName] = user.username
    }
    return@smartTransaction user
}

fun updateDatabaseUser(user: DatabaseUser) = smartTransaction {
    TableUsers.update({ userUUID eq user.uuid.toString() }) {
        it[userName] = user.username
        it[userRank] = user.rank
        it[userLanguage] = user.language
        it[userFirstJoined] = user.firstJoined.javaInstant
        it[userLastJoined] = user.lastTimeJoined.javaInstant
        it[onlineTime] = user.onlineTime.inWholeSeconds
    }
}