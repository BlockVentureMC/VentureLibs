package net.blockventuremc.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.VentureLibs
import net.blockventuremc.database.functions.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

internal object DatabaseManager {

    val dbConfig = HikariConfig().apply {
        jdbcUrl = VentureLibs.instance.dotenv["DATABASE_URL"]
        driverClassName = VentureLibs.instance.dotenv["DATABASE_DRIVER"]
        username = VentureLibs.instance.dotenv["DATABASE_USER"]
        password = VentureLibs.instance.dotenv["DATABASE_PASSWORD"]
        maximumPoolSize = 100
    }
    val database = Database.connect(HikariDataSource(dbConfig))

    fun register() = smartTransaction {
        SchemaUtils.createMissingTablesAndColumns(
            BlockUserTable,
            TableAchievements,
            TitleTable,
            TableLink,
            BoosterTable
        )
    }
}

internal fun Instant.toCalendar() =
    Calendar(GregorianCalendar.from(ZonedDateTime.from(this.atZone(ZoneId.systemDefault()))))

internal fun <T> smartTransaction(block: Transaction.() -> T): T {
    return transaction {
        return@transaction block()
    }
}