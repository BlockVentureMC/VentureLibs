package net.blockventuremc.extensions

import java.text.SimpleDateFormat
import java.util.*
import dev.fruxz.ascend.tool.time.calendar.Calendar


fun calendarFromDateString(dateFormat: String): Calendar {
    val cal: java.util.Calendar = java.util.Calendar.getInstance()
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    cal.time = sdf.parse(dateFormat) // all done
    return Calendar.fromLegacy(cal)
}

fun Calendar.formatToDay(): String {
    return SimpleDateFormat.getDateInstance(Calendar.FormatStyle.FULL.ordinal, Locale.GERMANY).format(javaDate)
}
