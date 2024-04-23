package net.blockventuremc.modules.general.events

import dev.fruxz.ascend.tool.time.calendar.Calendar
import dev.fruxz.stacked.text
import net.blockventuremc.BlockVenture
import net.blockventuremc.cache.PlayerCache
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
        Bukkit.getScheduler().runTaskLater(BlockVenture.instance, Runnable {
            val pixelPlayer = PlayerCache.reloadPlayer(player.uniqueId).copy(
                username = player.name,
                lastTimeJoined = Calendar.now()
            )
            PlayerCache.updateCached(
                pixelPlayer
            )
            player.sendActionBar(text("<green>" + "Loaded userdata..."))
        }, 10L)
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
    }
}