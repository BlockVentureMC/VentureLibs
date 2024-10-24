package net.blockventuremc.database.functions

import net.blockventuremc.database.model.Link
import net.blockventuremc.database.smartTransaction
import net.blockventuremc.database.toCalendar
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.*

object TableLink : Table("dc_link") {
    val userUUID = varchar("uuid", 45)

    val discordID = varchar("discordID", 255)

    val linkedAt = timestamp("linkedAt")

    override val primaryKey = PrimaryKey(userUUID, discordID)
}


private fun mapToLink(row: ResultRow): Link = with(row) {
    return Link(
        uuid = UUID.fromString(this[TableLink.userUUID]),
        discordID = this[TableLink.discordID],

        linkedAt = this[TableLink.linkedAt].toCalendar()
    )
}

fun linkUser(link: Link) = smartTransaction {
    TableLink.insert {
        it[userUUID] = link.uuid.toString()
        it[discordID] = link.discordID
        it[linkedAt] = link.linkedAt.javaInstant
    }
}

fun getLinkOfUser(uuid: UUID): Link? = smartTransaction {
    return@smartTransaction TableLink.selectAll()
        .where { TableLink.userUUID eq uuid.toString() }
        .firstOrNull()?.let(::mapToLink)
}

fun getLinkOfDiscord(discordID: String): Link? = smartTransaction {
    return@smartTransaction TableLink.selectAll()
        .where { TableLink.discordID eq discordID }
        .firstOrNull()?.let(::mapToLink)
}

fun unlinkUser(uuid: UUID) = smartTransaction {
    TableLink.deleteWhere { userUUID eq uuid.toString() }
}
