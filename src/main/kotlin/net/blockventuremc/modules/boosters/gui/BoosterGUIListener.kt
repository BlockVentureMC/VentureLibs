package net.blockventuremc.modules.boosters.gui

import net.blockventuremc.cache.BoosterCache
import net.blockventuremc.extensions.identifier
import net.blockventuremc.extensions.isIdentifiedAs
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.translate
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

class BoosterGUIListener: Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent): Unit = with(event) {
        val inv = clickedInventory ?: return
        if (!inv.isIdentifiedAs("booster_gui")) return

        if (currentItem == null) return

        isCancelled = true

        if (currentItem!!.identifier == null) return

        val booster = currentItem!!.identifier!!

        // shift right click
        if (isShiftClick && isRightClick) {
            BoosterCache.getByEndTime(booster.toLong())?.let {
                BoosterCache.invalidateBooster(it)

                inventory.setItem(slot, null)

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