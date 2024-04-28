package net.blockventuremc.modules.boosters.gui

import net.blockventuremc.extensions.isIdentifiedAs
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class BoosterGUIListener: Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent): Unit = with(event) {
        val inv = clickedInventory ?: return
        if (!inv.isIdentifiedAs("booster_gui")) return

        isCancelled = true
    }
}