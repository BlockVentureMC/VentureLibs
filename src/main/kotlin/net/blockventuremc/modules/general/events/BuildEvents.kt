package net.blockventuremc.modules.general.events

import com.destroystokyo.paper.MaterialSetTag
import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.canBuild
import net.blockventuremc.extensions.sendDeniedSound
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.*

class BuildEvents : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!event.player.canBuild) {
            event.player.sendDeniedSound()
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (!event.player.canBuild) {
            event.player.sendDeniedSound()
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onHangItemFrame(event: HangingPlaceEvent) {
        if (event.player == null) return
        if (!event.player!!.canBuild) {
            event.player!!.sendDeniedSound()
            event.isCancelled = true
        }
    }


    @EventHandler
    fun onBuild(event: BlockPlaceEvent) {
        if (!event.player.canBuild) {
            event.player.sendDeniedSound()
            event.isCancelled = true
        }
    }


    @EventHandler
    fun onLiquidPlace(event: PlayerBucketEmptyEvent) {
        if (!event.player.canBuild) {
            event.player.sendDeniedSound()
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onLiquidGet(event: PlayerBucketFillEvent) {
        if (!event.player.canBuild && event.block.type == Material.WATER) {
            Bukkit.getScheduler().runTaskLater(VentureLibs.instance, Runnable {
                event.block.type = Material.WATER
            }, 1)
        }
    }

    @EventHandler
    fun onEggThrownEvent(event: PlayerEggThrowEvent) {
        if (!event.player.canBuild) {
            event.player.sendDeniedSound()
            event.isHatching = false
        }
    }

    @EventHandler
    fun onHangingBreak(event: HangingBreakByEntityEvent) {
        if (event.remover !is Player) return
        val player = event.remover as Player
        if (!player.canBuild) {
            event.isCancelled = true
            player.sendDeniedSound()
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player.canBuild) return

        val clickedType = event.clickedBlock?.type ?: return
        with(clickedType) {
            event.isCancelled = when {
                this == Material.FARMLAND -> true
                this == Material.ITEM_FRAME -> true
                this == Material.GLOW_ITEM_FRAME -> true
                MaterialSetTag.BEDS.isTagged(this) -> true
                MaterialSetTag.TRAPDOORS.isTagged(this) -> true
                else -> return
            }
        }
    }

    @EventHandler
    fun onInteractAtEntity(event: PlayerInteractEntityEvent) {
        if (event.player.canBuild) return

        val clickedType = event.rightClicked.type
        with(clickedType) {
            event.isCancelled = when {
                this == EntityType.ITEM_FRAME -> true
                this == EntityType.GLOW_ITEM_FRAME -> true
                else -> return
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.damager is Player) {
            val player = event.damager as Player
            if (!player.canBuild) {
                player.sendDeniedSound()
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        if (event.entity !is Player) return
        event.foodLevel = (event.entity.foodLevel + 2) % 20
    }
}