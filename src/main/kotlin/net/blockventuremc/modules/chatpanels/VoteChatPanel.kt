package net.blockventuremc.modules.chatpanels

import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.extensions.getInBox
import net.blockventuremc.extensions.translate
import org.bukkit.Bukkit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class VoteChatPanel(override val id: String, val voteMessageKey: String, duration: Duration = 30.seconds, private val positiveMessageKey: String = "chatpanel.vote.yes", private val negativeMessageKey: String = "chatpanel.vote.no") : ChatPanel {

    override val until: Calendar = Calendar.now().plus(duration)

    override fun display() {
        // Display the vote chat panel

        Bukkit.getOnlinePlayers().forEach { player ->
            val messages = player.getInBox(
                " ",
                "  " + (player.translate(voteMessageKey)?.message ?: "Vote"),
                " ",
                player.translate(positiveMessageKey)?.message ?: "Yes",
                player.translate(negativeMessageKey)?.message ?: "No",
                " "
            )

            messages.forEach { message ->
                player.sendMessage(message)
            }
        }
    }

}