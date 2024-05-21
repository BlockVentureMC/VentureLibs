package net.blockventuremc.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.VentureLibs
import net.blockventuremc.database.functions.*
import net.blockventuremc.utils.Environment
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
        jdbcUrl = Environment.getEnv("DATABASE_URL")
        driverClassName = Environment.getEnv("DATABASE_DRIVER")
        username = Environment.getEnv("DATABASE_USER")
        password = Environment.getEnv("DATABASE_PASSWORD")
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