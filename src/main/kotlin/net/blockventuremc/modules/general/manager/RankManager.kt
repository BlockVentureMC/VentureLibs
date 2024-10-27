package net.blockventuremc.modules.general.manager

import dev.fruxz.stacked.extension.asPlainString
import dev.fruxz.stacked.text
import net.blockventuremc.database.functions.getLinkOfDiscord
import net.blockventuremc.database.functions.getLinkOfUser
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.modules.general.model.Rank
import net.blockventuremc.utils.mcasyncBlocking
import net.kyori.adventure.text.format.NamedTextColor
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.NodeType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard
import java.util.*


object RankManager {

    private var ranks = emptyList<Rank>()
    private val luckPerms = LuckPermsProvider.get()

    init {
        reloadRanks()



        Bukkit.getOnlinePlayers().forEach { updateScoreBoardForPlayer(it) }
    }

    /**
     * Loads ranks from the `luckPerms.groupManager.loadedGroups` list
     * and populates the `ranks` list with Rank objects.
     * Each Rank object contains information about the rank's name, color, bitsPerMinute, weight, parent, and discordRoleID.
     * Once loaded, the `ranks` list is sorted by weight in ascending order.
     * This method also logs the loaded ranks and their count.
     */
    fun reloadRanks() {
        ranks = emptyList()
        luckPerms.groupManager.loadedGroups.forEach { rank ->
            val displayName = rank.displayName ?: rank.name
            val prefix = rank.getNodes(NodeType.PREFIX).firstOrNull()?.key
            val color = prefix?.substringAfterLast(".")?.substring(1, 8) ?: "#ffffff"
            val weight = rank.weight.orElse(0)
            val parent = rank.getNodes(NodeType.INHERITANCE).firstOrNull()?.key
                ?.let { parent -> ranks.find { it.name == parent } }

            val bitsPerMinute = rank.getNodes(NodeType.PERMISSION)
                .filter { it.key.startsWith("venturelibs.bitsPerMinute") }
                .maxOfOrNull { it.key.substringAfterLast(".").toLong() } ?: 1

            val discordRoleId = rank.getNodes(NodeType.PERMISSION)
                .firstOrNull { it.key.startsWith("venturelibs.discordRole") }?.key
                ?.substringAfterLast(".")

            ranks += Rank(rank.name, displayName, color, bitsPerMinute, weight, parent, discordRoleId)

            getLogger().info("Loaded rank ${rank.name}")
        }

        ranks = ranks.sortedBy { it.weight }
        Bukkit.getOnlinePlayers().forEach { updateScoreBoardForPlayer(it) }

        getLogger().info("Loaded ${ranks.size} ranks")
    }

    /**
     * Retrieves the [Rank] object matching the specified rank name.
     *
     * @param rank The name of the rank to retrieve.
     * @return The [Rank] object matching the specified rank name, or the first rank in the list if no match is found.
     */
    fun getRankByName(rank: String): Rank {
        return ranks.find { it.name == rank } ?: ranks.first()
    }

    /**
     * Returns the rank of a user based on their UUID.
     *
     * @param uuid the UUID of the user
     * @return the rank of the user
     */
    fun getRankOfUser(uuid: UUID): Rank {
        val userFuture = luckPerms.userManager.loadUser(uuid)

        userFuture.join().primaryGroup.let { primaryGroup ->
            return getRankByName(primaryGroup)
        }
    }


    /**
     * Updates the rank of a user by setting their primary group and updating their role in Discord.
     * This method modifies the user's rank in the LuckPerms plugin and updates their role in Discord if they are linked.
     *
     * @param rank The new rank to assign to the user.
     * @param blockUser The user to update the rank for.
     */
    fun updateRank(rank: Rank, uuid: UUID) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user $uuid parent set ${rank.name}")

        mcasyncBlocking {
            val link = getLinkOfUser(uuid) ?: return@mcasyncBlocking

            rank.updateRole(link.discordID)
        }
    }

    /**
     * Updates the Discord rank of a user based on their Discord ID.
     *
     * @param discordID the Discord ID of the user
     */
    fun updateDiscordRank(discordID: String) {
        val link = getLinkOfDiscord(discordID) ?: return
        val rank = getRankOfUser(link.uuid)

        rank.updateRole(discordID)
    }



    fun initScoreBoard(scoreboard: Scoreboard) {
        ranks.forEach { rank ->
            val team = scoreboard.getTeam(rank.weight.toString()) ?: scoreboard.registerNewTeam(rank.weight.toString())
            team.prefix(text("<color:${rank.color}>${rank.displayName}</color> <#3d3d3d>● <#c8d6e5>"))
            team.color(NamedTextColor.WHITE)
        }
    }

    fun updateScoreBoardForPlayer(player: Player) {
        val scoreboard = player.scoreboard

        initScoreBoard(scoreboard)

        Bukkit.getOnlinePlayers().forEach { setPlayerInScoreboard(it, scoreboard) }

    }

    private fun setPlayerInScoreboard(player: Player, scoreBoard: Scoreboard) {
        val userRank = getRankOfUser(player.uniqueId)
        val team = scoreBoard.getTeam(userRank.weight.toString()) ?: scoreBoard.registerNewTeam(userRank.weight.toString())
        team.addEntry(player.name)
    }
}
