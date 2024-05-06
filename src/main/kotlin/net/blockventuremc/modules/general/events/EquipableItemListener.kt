package net.blockventuremc.modules.general.events

import dev.lone.fastnbt.nms.nbt.NItem
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack


class EquipableItemListener : Listener {


    @EventHandler
    fun onEquip(event: PlayerInteractEvent): Unit = with(event) {
        if (hand != EquipmentSlot.HAND) return@with
        val item = event.item ?: return@with
        val itemMeta = if (item.hasItemMeta()) item.itemMeta else return@with
        if (!itemMeta.hasCustomModelData()) return@with

        val nItem = NItem(item)
        val equipable = nItem.getString("vl_equipable") ?: return@with
        try {
            changeItem(player, EquipmentSlot.valueOf(equipable.uppercase()), item)
        } catch (e: IllegalArgumentException) {
            return@with
        }
    }

    private fun changeItem(player: Player, equipementSlot: EquipmentSlot, item: ItemStack) {
        val itemInEquipmentSlot = player.equipment.getItem(equipementSlot)
        player.equipment.setItem(equipementSlot, item)
        player.inventory.setItemInMainHand(itemInEquipmentSlot)
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.4f, 0.9f)
    }
}