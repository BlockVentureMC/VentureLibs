package net.blockventuremc.modules.general.events

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class ItemClickListener : Listener {

    companion object {
        val itemClickEvents: MutableMap<ItemStack, (event: InventoryClickEvent) -> Unit> = mutableMapOf()
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onInventoryClick(event: InventoryClickEvent) {
        val item = event.currentItem ?: return
        val action = itemClickEvents[item] ?: return
        action(event)
    }
}