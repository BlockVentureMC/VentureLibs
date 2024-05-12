package net.blockventuremc.modules.chatpanels

import dev.fruxz.ascend.tool.time.calendar.Calendar

interface ChatPanel {

    val id: String
    val until: Calendar

    fun display()
}