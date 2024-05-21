package net.blockventuremc.modules.boosters.gui

import net.blockventuremc.cache.BoosterCache
import net.blockventuremc.extensions.*
import net.blockventuremc.utils.itembuilder.toItemBuilder
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack

class BoosterGUIListener: Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent): Unit = with(event) {
        val inv = clickedInventory ?: return
        if (!inv.isIdentifiedAs("booster_gui")) return

        if (currentItem == null) return

        isCancelled = true

        if (currentItem!!.identifier == null) return

        val booster = currentItem!!.identifier!!

        if (isShiftClick && isRightClick) {
            BoosterCache.getByEndTime(booster.toLong())?.let {
                BoosterCache.invalidateBooster(it)

                inventory.identify("booster_gui")

                whoClicked.translate("booster.deleted")?.message?.let { it1 -> whoClicked.sendMessagePrefixed(it1) }
            }
        }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent): Unit = with(event) {
        val inv = inventory
        if (!inv.isIdentifiedAs("booster_gui")) return

        isCancelled = true
    }
}