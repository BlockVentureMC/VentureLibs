package net.blockventuremc.modules.general.events

import dev.fruxz.ascend.tool.time.calendar.Calendar
import dev.fruxz.stacked.text
import net.blockventuremc.VentureLibs
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.extensions.addToHistory
import net.blockventuremc.modules.titles.Title
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerLoadSaveListener : Listener {


    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent): Unit = with(event) {
        player.sendActionBar(text("<green>" + "Loading userdata..."))
        Bukkit.getScheduler().runTaskLater(VentureLibs.instance, Runnable {
            val pixelPlayer = PlayerCache.reloadPlayer(player.uniqueId).copy(
                username = player.name,
                lastTimeJoined = Calendar.now()
            )
            PlayerCache.updateCached(
                pixelPlayer
            )
            player.sendActionBar(text("<green>" + "Loaded userdata..."))

            // Award first time visitor title (if applicable)
            Title.FIRST_TIME_VISITOR.award(player)
        }, 10L)

        event.joinMessage(text("<color:#95a5a6>[ <color:#2ecc71>\uD83D\uDC49</color> ] <color:#c8d6e5>${player.name}").addToHistory())
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onQuit(event: PlayerQuitEvent): Unit = with(event) {
        val databaseUser = PlayerCache.get(player.uniqueId)
        PlayerCache.saveToDB(
            databaseUser.copy(
                username = player.name
            )
        )
        PlayerCache.remove(player.uniqueId)

        event.quitMessage(text("<color:#95a5a6>[ <color:#e74c3c>\uD83D\uDC48</color> ] <color:#c8d6e5>${player.name}").addToHistory())
    }
}