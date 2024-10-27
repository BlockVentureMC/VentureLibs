package net.blockventuremc.database.functions

import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.database.functions.TitleTable.awardedAt
import net.blockventuremc.database.functions.TitleTable.title
import net.blockventuremc.database.smartTransaction
import net.blockventuremc.database.toCalendar
import net.blockventuremc.modules.titles.Title
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchReplace
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import java.util.*

object TitleTable : Table("player_titles") {
    val id = uuid("id")
    val title = varchar("title", 255)
    val awardedAt = timestamp("awarded_at")

    override val primaryKey = PrimaryKey(id, title)
}

/**
 * Retrieves the titles of a player identified by their UUID.
 *
 * @param uuid The UUID of the player.
 * @return A map containing the titles owned by the player, mapped to the calendar when they were awarded.
 */
fun getPlayerTitles(uuid: UUID): MutableMap<Title, Calendar> = smartTransaction {
    TitleTable.selectAll().where { TitleTable.id eq uuid }
        .associate { Title.valueOf(it[title]) to it[awardedAt].toCalendar() }.toMutableMap()
}

/**
 * Awards a player with a title and stores it in the database.
 *
 * @param uuid the UUID of the player to award the title
 * @param titles a map where the key is the title to award the player, and the value is the timestamp when it was awarded
 */
fun bulkReplacePlayerTitlesDB(uuid: UUID, titles: Map<Title, Calendar>) = smartTransaction {
    TitleTable.batchReplace(titles.entries) { (title, awardedAt) ->
        this[TitleTable.id] = uuid
        this[TitleTable.title] = title.name
        this[TitleTable.awardedAt] = awardedAt.javaInstant
    }
}