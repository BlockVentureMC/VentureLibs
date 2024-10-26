package net.blockventuremc.modules.titles

import net.blockventuremc.utils.itembuilder.ItemBuilder
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
    private val icon: ItemBuilder,
    private val award: Int = 0
) {

    FIRST_TIME_VISITOR(
        TitleCategory.GENERIC,
        { player -> player.translate("title.first_time_visitor.display")?.message ?: "<#dff9fb>First Time Visitor" },
        { player -> player.translate("title.first_time_visitor.description")?.message ?: "First time visitor" },
        ItemBuilder(Material.BOOK)
    ),
    ONE_YEAR_VISITOR(
        TitleCategory.GENERIC,
        { player -> player.translate("title.one_year_visitor.display")?.message ?: "<#dff9fb>One Year Visitor" },
        { player -> player.translate("title.one_year_visitor.description")?.message ?: "You have visited the server over one year ago" },
        ItemBuilder(Material.BOOK),
        50
    ),
    TWO_YEAR_VISITOR(
        TitleCategory.GENERIC,
        { player -> player.translate("title.two_year_visitor.display")?.message ?: "<#dff9fb>Two Year Visitor" },
        { player -> player.translate("title.two_year_visitor.description")?.message ?: "You have visited the server over two years ago" },
        ItemBuilder(Material.BOOK),
        100
    ),
    THREE_YEAR_VISITOR(
        TitleCategory.GENERIC,
        { player -> player.translate("title.three_year_visitor.display")?.message ?: "<#dff9fb>Three Year Visitor" },
        { player -> player.translate("title.three_year_visitor.description")?.message ?: "You have visited the server over three years ago" },
        ItemBuilder(Material.BOOK),
        150
    ),
    FOUR_YEAR_VISITOR(
        TitleCategory.GENERIC,
        { player -> player.translate("title.four_year_visitor.display")?.message ?: "<#dff9fb>Four Year Visitor" },
        { player -> player.translate("title.four_year_visitor.description")?.message ?: "You have visited the server over four years ago" },
        ItemBuilder(Material.BOOK),
        200
    ),
    FIVE_YEAR_VISITOR(
        TitleCategory.GENERIC,
        { player -> player.translate("title.five_year_visitor.display")?.message ?: "<#dff9fb>Five Year Visitor" },
        { player -> player.translate("title.five_year_visitor.description")?.message ?: "You have visited the server over five years ago" },
        ItemBuilder(Material.BOOK),
        250
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
        PlayerCache.updateCached(blockPlayer.copy(ventureBits = blockPlayer.ventureBits + award))


        player.sendMessageBlock(
            "<gradient:#45aaf2:#3867d6>${player.translate("title.unlocked")?.message ?: "Title unlocked!"}</gradient>",
            " ",
            "    <b>${display(player)}</b>",
            "    <dark_gray>»</dark_gray>   <#95afc0>${description(player)}",
            if(award > 0) "    <dark_gray>»</dark_gray>   <#95afc0>${player.translate("title.unlocked.award", mapOf("award" to award))?.message ?: "You have been awarded <color:#f78fb3>${award} VentureBits</color>."}" else "",
            " ",
            "<i><#778ca3>${player.translate("title.unlocked.command")?.message ?: "Change your selected title with <color:#eaff94>/title</color>."}"
        )
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1.3f)
        player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.3f, 1.3f)
    }

}