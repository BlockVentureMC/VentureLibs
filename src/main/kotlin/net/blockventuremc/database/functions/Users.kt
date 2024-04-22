package net.blockventuremc.database.functions

import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.modules.general.model.Ranks
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object TableUsers : Table("users") {
    val userUUID = varchar("uuid", 45)
    val userName = varchar("username", 24)
    val userRank = enumerationByName("rank", 24, Ranks::class).default(Ranks.Default)

    val userFirstJoined = timestamp("firstJoined")
    val userLastJoined = timestamp("lastTimeOnline")
    val onlineTime = long("onlineTime").default(0)

    override val primaryKey = PrimaryKey(userUUID)
}