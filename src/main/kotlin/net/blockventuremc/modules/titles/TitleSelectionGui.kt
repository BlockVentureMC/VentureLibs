package net.blockventuremc.modules.titles

import net.blockventuremc.utils.itembuilder.toItemBuilder
import dev.fruxz.stacked.extension.asPlainString
import dev.fruxz.stacked.text
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.translate
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class TitleSelectionGui : Listener {

    private val placeHolder = Material.GRAY_STAINED_GLASS_PANE.toItemBuilder { display(" ") }.build()

    companion object {
        lateinit var instance: TitleSelectionGui
    }

    init {
        instance = this
    }

    /**
     * Opens an inventory for the specified player to select titles.
     *
     * @param player The player for whom to open the inventory.
     * @param titleCategory The category of titles to be displayed in the inventory. Default is XP.
     */
    fun openInventory(player: Player, titleCategory: TitleCategory = TitleCategory.GENERIC) {
        val blockPlayer = PlayerCache.getOrNull(player.uniqueId) ?: return

        val inventory = Bukkit.createInventory(
            null,
            9 * 6,
            text("<gradient:#45aaf2:#8854d0>Titles</gradient> <gray>» ${titleCategory.display(player)}")
        )
        (0..53).forEach {
            val row = it / 9
            val column = it % 9

            if (row in 1..4 && column in 1..5) {
                return@forEach
            }

            inventory.setItem(it, placeHolder)
        }

        inventory.setItem(
            16,
            TitleCategory.GENERIC.getIcon(player).clone()
                .setGlinting(titleCategory == TitleCategory.GENERIC).build()
        )
        inventory.setItem(
            25,
            TitleCategory.RIDE_COUNTER.getIcon(player).clone()
                .setGlinting(titleCategory == TitleCategory.RIDE_COUNTER).build()
        )
        inventory.setItem(34, Material.BLACK_STAINED_GLASS_PANE.toItemBuilder { display("Soon...") }.build())
        inventory.setItem(43, Material.BLACK_STAINED_GLASS_PANE.toItemBuilder { display("Soon...") }.build())

        // 5x4 Grid
        val titles = Title.entries.filter { it.category == titleCategory }.sortedBy {
            (if (blockPlayer.titles.containsKey(it)) -100 else 0) + it.ordinal
        }

        titles.forEachIndexed { index, title ->
            val row = index / 5
            val column = index % 5

            inventory.setItem(10 + row * 9 + column, title.getGuiIcon(player))
        }

        player.openInventory(inventory)
        player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 0.4f, 1.3f)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent): Unit = with(event) {
        if (currentItem == null || currentItem!!.type == Material.AIR) return
        if (!view.title().asPlainString.startsWith("Titles")) return
        val player = whoClicked as Player

        event.isCancelled = true

        when (slot) {
            16 -> openInventory(whoClicked as Player, TitleCategory.GENERIC)
            25 -> openInventory(whoClicked as Player, TitleCategory.RIDE_COUNTER)
//            34 -> openInventory(whoClicked as Player, TitleCategory.GENERIC)
//            43 -> openInventory(whoClicked as Player, TitleCategory.CREATIVE)
        }

        val itemTitle = currentItem!!.displayName().asPlainString.replace("[", "").replace("]", "")
        val title = Title.entries.find { text(it.display(player)).asPlainString == itemTitle } ?: return

        val blockPlayer = PlayerCache.getOrNull(player.uniqueId) ?: return
        if (blockPlayer.titles.containsKey(title)) {
            blockPlayer.selectedTitle = title
            PlayerCache.updateCached(blockPlayer)
            player.playSound(whoClicked, Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 0.4f, 1.3f)
            player.sendMessagePrefixed(
                blockPlayer.translate(
                    "title.changed",
                    mapOf("title" to title.display(player))
                )?.message ?: "<green>Your title has been changed to <yellow>${title.display(player)}</yellow> <green>!"
            )
            player.closeInventory()
        }
    }
}