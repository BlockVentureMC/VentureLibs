package net.blockventuremc.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.Plugin
import org.jetbrains.exposed.sql.Database
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

internal object DatabaseManager {

    val dbConfig = HikariConfig().apply {
        jdbcUrl = Plugin.instance.dotenv["DATABASE_URL"]
        driverClassName = Plugin.instance.dotenv["DATABASE_DRIVER"]
        username = Plugin.instance.dotenv["DATABASE_USER"]
        password = Plugin.instance.dotenv["DATABASE_PASSWORD"]
        maximumPoolSize = 100
    }
    val database = Database.connect(HikariDataSource(dbConfig))
}

internal fun Instant.toCalendar() =
    Calendar(GregorianCalendar.from(ZonedDateTime.from(this.atZone(ZoneId.systemDefault()))))
