package net.blockventuremc.database.functions

import net.blockventuremc.database.functions.BlockUserTable.userUUID
import net.blockventuremc.database.model.BlockUser
import net.blockventuremc.database.smartTransaction
import net.blockventuremc.database.toCalendar
import net.blockventuremc.modules.general.model.Languages
import net.blockventuremc.modules.general.model.Ranks
import net.blockventuremc.modules.titles.Title
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.*
import kotlin.time.Duration.Companion.seconds

object BlockUserTable : Table("users") {
    val userUUID = varchar("uuid", 45)
    val userName = varchar("username", 24)
    val userLanguage = enumerationByName("language", 2, Languages::class).default(Languages.EN)

    val xp = long("xp").default(0)

    val ventureBits = long("ventureBits").default(0) // Currency

    val userFirstJoined = timestamp("firstJoined").defaultExpression(CurrentTimestamp())
    val userLastJoined = timestamp("lastTimeOnline").defaultExpression(CurrentTimestamp())
    val onlineTime = long("onlineTime").default(0)

    val selectedTitle = varchar("selected_title", 255).nullable()

    override val primaryKey = PrimaryKey(userUUID)
}


fun getDatabaseUserOrNull(uuid: UUID): BlockUser? = smartTransaction {
    return@smartTransaction BlockUserTable.selectAll().where { userUUID eq uuid.toString() }.firstOrNull()
        ?.let(::mapToDatabaseUser)
}

private fun mapToDatabaseUser(row: ResultRow): BlockUser = with(row) {
    return BlockUser(
        uuid = UUID.fromString(this[userUUID]),
        username = this[BlockUserTable.userName],
        language = this[BlockUserTable.userLanguage],
        xp = this[BlockUserTable.xp],
        ventureBits = this[BlockUserTable.ventureBits],
        firstJoined = this[BlockUserTable.userFirstJoined].toCalendar(),
        lastTimeJoined = this[BlockUserTable.userLastJoined].toCalendar(),
        onlineTime = this[BlockUserTable.onlineTime].seconds,
        selectedTitle = this[BlockUserTable.selectedTitle]?.let { Title.valueOf(it) }
    )
}


fun createDatabaseUser(user: BlockUser): BlockUser = smartTransaction {
    BlockUserTable.insert {
        it[userUUID] = user.uuid.toString()
        it[userName] = user.username
    }
    return@smartTransaction user
}

fun updateDatabaseUser(user: BlockUser) = smartTransaction {
    BlockUserTable.update({ userUUID eq user.uuid.toString() }) {
        it[userName] = user.username
        it[userLanguage] = user.language
        it[xp] = user.xp
        it[ventureBits] = user.ventureBits
        it[userFirstJoined] = user.firstJoined.javaInstant
        it[userLastJoined] = user.lastTimeJoined.javaInstant
        it[onlineTime] = user.onlineTime.inWholeSeconds
        it[selectedTitle] = user.selectedTitle?.name
    }
    bulkReplacePlayerTitlesDB(user.uuid, user.titles)
}