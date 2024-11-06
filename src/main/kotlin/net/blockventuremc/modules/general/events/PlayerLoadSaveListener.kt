package net.blockventuremc.modules.general.events

import dev.fruxz.ascend.tool.time.calendar.Calendar
import dev.fruxz.stacked.text
import net.blockventuremc.VentureLibs
import net.blockventuremc.database.model.BlockUser
import net.blockventuremc.modules.general.cache.PlayerCache
import net.blockventuremc.modules.general.manager.RankManager
import net.blockventuremc.modules.titles.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.time.Duration.Companion.days

class PlayerLoadSaveListener : Listener {


    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent): Unit = with(event) {
        player.sendActionBar(text("<green>" + "Loading userdata..."))
        Bukkit.getScheduler().runTaskLater(VentureLibs.instance, Runnable {
            val blockUser = PlayerCache.reloadPlayer(player.uniqueId).copy(
                username = player.name,
                lastTimeJoined = Calendar.now()
            )
            PlayerCache.updateCached(
                blockUser
            )

            Bukkit.getOnlinePlayers().forEach { RankManager.updateScoreBoardForPlayer(it) }

            setTabList(player)
            player.sendActionBar(text("<green>" + "Loaded userdata..."))

            awardTimedTitles(player, blockUser)
        }, 10L)

        event.joinMessage(text("<color:#95a5a6>[ <color:#2ecc71>\uD83D\uDC49</color> ] <color:#c8d6e5>${player.name}"))
    }

    private fun setTabList(player: Player) {
        player.sendPlayerListHeaderAndFooter(
            text(
                "<#ff8aff>┌                                                  <#10eefe>┐\n" +
                "<gradient:#70b5ff:#6fbaf7><b>BlockVenture</b></gradient>\n" +
                " "
            ),
            text(
                " \n" +
                "<color:#266ee0>Sᴇʀᴠᴇʀ: <color:#b2bec3>ʙʟᴏᴄᴋᴠᴇɴᴛᴜʀᴇᴍᴄ.ɴᴇᴛ\n" +
                "<color:#266ee0>Wᴇʙsɪᴛᴇ: <color:#b2bec3>ʙʟᴏᴄᴋᴠᴇɴᴛᴜʀᴇᴍᴄ.ɴᴇᴛ\n" +
                "<color:#266ee0>Sᴛᴏʀᴇ: <color:#b2bec3>Sᴛᴏʀᴇ.ʙʟᴏᴄᴋᴠᴇɴᴛᴜʀᴇᴍᴄ.ɴᴇᴛ\n" +
                "<#10eefe>└                                                  <#ff8aff>┘ "
            )
        )
    }

    private fun awardTimedTitles(player: Player, blockUser: BlockUser) {
        // Award first time visitor title (if applicable)
        Title.FIRST_TIME_VISITOR.award(player)

        val timeJoined = blockUser.firstJoined.durationToNow()

        if (timeJoined > 365.days) {
            Title.ONE_YEAR_VISITOR.award(player)
        }

        if (timeJoined > (2 * 365).days) {
            Title.TWO_YEAR_VISITOR.award(player)
        }

        if (timeJoined > (3 * 365).days) {
            Title.THREE_YEAR_VISITOR.award(player)
        }

        if (timeJoined > (4 * 365).days) {
            Title.FOUR_YEAR_VISITOR.award(player)
        }

        if (timeJoined > (5 * 365).days) {
            Title.FIVE_YEAR_VISITOR.award(player)
        }
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

        Bukkit.getOnlinePlayers().forEach { RankManager.updateScoreBoardForPlayer(it) }

        event.quitMessage(text("<color:#95a5a6>[ <color:#e74c3c>\uD83D\uDC48</color> ] <color:#c8d6e5>${player.name}"))
    }
}