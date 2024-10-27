package net.blockventuremc.modules.general.events

import dev.fruxz.stacked.extension.asPlainString
import dev.fruxz.stacked.text
import io.papermc.paper.event.player.AsyncChatEvent
import net.blockventuremc.modules.general.events.custom.*
import net.blockventuremc.utils.parsePlaceholders
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.time.Duration

class ChatEvent : Listener {

    private val chatFormat =
        "%luckperms_prefix%%luckperms_primary_group_name% <color:#3d3d3d>»</color> <color:#c8d6e5>%playername%</color> <color:#f6e58d>"

    @EventHandler
    fun onChat(event: AsyncChatEvent): Unit = with(event) {
        val messagePlain = message().asPlainString

        // Highlight player names in the chat message
        val highlightedMessage = highlightPlayerNames(messagePlain)

        var format =
            text(parsePlaceholders(chatFormat, player)).append(text(parsePlaceholders(highlightedMessage, player)))

        val area = player.location.getArea()?.let { area -> if (area.name.startsWith("chatroom_")) area else null }
        if (area != null) {
            format = (text("<color:#7593ff>[${area.chatRoomName}]</color> ")).append(format)
        }

        val audienceFiltered = event.viewers().filter {
            if (it is Player && area != null) {
                return@filter it.location.getArea()?.name == area.name
            }
            return@filter true
        }
        event.viewers().clear()
        event.viewers().addAll(audienceFiltered)

        event.renderer { _, _, _, _ ->
            return@renderer format
        }
    }

    private fun highlightPlayerNames(message: String): String {
        val playerNames = Bukkit.getOnlinePlayers().map { it.name }
        var highlightedMessage = message
        playerNames.forEach { playerName ->
            highlightedMessage = highlightedMessage.replace(playerName, "<color:#f6e58d>$playerName</color>")
        }
        return highlightedMessage
    }

    @EventHandler
    fun onWalkInChatRoom(event: AreaEnterEvent) {
        handleAreaEnterLeave(event.area, event.player, true)
    }

    @EventHandler
    fun onWalkOutChatRoom(event: AreaLeaveEvent) {
        handleAreaEnterLeave(event.area, event.player, false)
    }

    private fun handleAreaEnterLeave(
        area: Area,
        player: Player,
        enter: Boolean = false
    ) {
        if (!area.name.startsWith("chatroom_")) return
        player.sendTitlePart(
            TitlePart.TIMES,
            Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(1), Duration.ofMillis(200))
        )
        player.sendTitlePart(TitlePart.TITLE, text(area.chatRoomType))
        player.sendTitlePart(TitlePart.SUBTITLE, text("<color:#8395a7>${if(enter) "Entering" else "Leaving"}: <#c8d6e5>${area.chatRoomName}"))
        player.playSound(player.location, "globalsounds:world.chatroom_${if(enter) "enter" else "leave"}", SoundCategory.AMBIENT, 0.6f, 1f)
    }

}
