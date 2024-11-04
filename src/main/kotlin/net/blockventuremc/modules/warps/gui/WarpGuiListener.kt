package net.blockventuremc.modules.warps.gui

import net.blockventuremc.extensions.isIdentifiedAs
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

class WarpGuiListener : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent): Unit = with(event) {
        val inv = clickedInventory ?: return
        if (!inv.isIdentifiedAs("warp_gui", 1)) return

        if (currentItem == null) return
        isCancelled = true
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent): Unit = with(event) {
        val inv = inventory
        if (!inv.isIdentifiedAs("warp_gui", 1)) return

        isCancelled = true
    }

}