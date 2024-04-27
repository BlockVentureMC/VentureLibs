package net.blockventuremc.modules.titles

import com.rainbowislands.utility.utils.ItemBuilder
import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.extensions.sendMessageBlock
import net.blockventuremc.extensions.toBlockUser
import net.blockventuremc.extensions.translate
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

enum class Title(
    val category: TitleCategory,
    val display: (Player) -> String,
    private val description: (Player) -> String,
    private val icon: ItemBuilder
) {

    FIRST_TIME_VISITOR(
        TitleCategory.GENERIC,
        { player -> player.translate("title.first_time_visitor.display")?.message ?: "<#dff9fb>First Time Visitor" },
        { player -> player.translate("title.first_time_visitor.description")?.message ?: "First time visitor" },
        ItemBuilder(Material.BOOK)
    );


    fun getGuiIcon(player: Player): ItemStack {
        val blockPlayer = player.toBlockUser()

        val lore = description(player).split("\n").map { "<#778ca3>$it" }.toMutableList()
        lore.add(" ")

        if (blockPlayer.titles.containsKey(this)) {
            val unlockedAt = blockPlayer.titles[this]?.getFormatted(blockPlayer.language.locale)
            lore.add(
                blockPlayer.translate("title.gui.item.unlocked", mapOf("unlocked" to unlockedAt))?.message
                    ?: "Unlocked on: <#fed330>${unlockedAt}"
            )
        } else {
            lore.add(blockPlayer.translate("title.gui.item.locked")?.message ?: "<#ff4757>Locked")
        }

        return icon
            .clone()
            .display(display(player))
            .condition(!blockPlayer.titles.containsKey(this)) {
                it.type(Material.PLAYER_HEAD)
                it.textureFromMineSkin("c9cec94d5f9e41088003afb8bbb23549")
            }
            .lore(*lore.toTypedArray())
            .enchantIf(mapOf(Enchantment.KNOCKBACK to 1), blockPlayer.selectedTitle == this)
            .build()
    }


    fun award(player: Player) {
        val blockPlayer = PlayerCache.getOrNull(player.uniqueId) ?: return
        if (blockPlayer.titles.containsKey(this)) return

        blockPlayer.titles[this] = Calendar.now()
        if (blockPlayer.selectedTitle == null) {
            blockPlayer.selectedTitle = this
        }
        PlayerCache.updateCached(blockPlayer)


        player.sendMessageBlock(
            "<gradient:#45aaf2:#3867d6>${player.translate("title.unlocked")?.message ?: "Title unlocked!"}</gradient>",
            " ",
            "    <b>${display(player)}</b>",
            "    <dark_gray>»</dark_gray>   <#95afc0>${description(player)}",
            " ",
            "<i><#778ca3>${player.translate("title.unlocked.command")?.message ?: "Change your selected title with <color:#eaff94>/title</color>."}"
        )
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1.3f)
        player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.3f, 1.3f)
    }

}