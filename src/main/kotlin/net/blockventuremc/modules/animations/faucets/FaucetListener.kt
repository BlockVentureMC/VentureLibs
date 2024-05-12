package net.blockventuremc.modules.animations.faucets

import org.bukkit.block.data.type.TripwireHook
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class FaucetListener : Listener {

    @EventHandler
    fun onFaucetInteract(event: PlayerInteractEvent) = with(event) {
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return@with
        if (hand != EquipmentSlot.HAND) return@with

        val block = clickedBlock ?: return@with
        if (block.blockData !is TripwireHook) return@with
        val cauldronBlock = block.location.add(0.0, -1.0, 0.0).block

        FaucetAnimation(block.location, cauldronBlock)
    }
}