package net.blockventuremc.modules.warps.gui

import dev.fruxz.stacked.text
import io.papermc.paper.entity.TeleportFlag
import net.blockventuremc.consts.TEXT_GRAY
import net.blockventuremc.extensions.identify
import net.blockventuremc.extensions.isRankOrHigher
import net.blockventuremc.extensions.sendInfo
import net.blockventuremc.extensions.translate
import net.blockventuremc.modules.warps.ParkArea
import net.blockventuremc.modules.warps.Warp
import net.blockventuremc.modules.warps.WarpManager
import net.blockventuremc.modules.warps.WarpType
import net.blockventuremc.utils.itembuilder.toItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.inventory.ItemStack

object WarpGui {

    fun openMenu(player: Player, parkArea: ParkArea = ParkArea.PARADISE_BAY, sort: WarpType = WarpType.GENERIC, page: Int = 1) {
        val inventory = Bukkit.createInventory(null, 9 * 6, text("<gradient:#45aaf2:#8854d0>Warps</gradient>"))
        inventory.identify("warp_gui", 1)

        for (i in 1 until ParkArea.entries.size + 1) {
            val parkAreaSelected = ParkArea.entries[i - 1]
            inventory.setItem(i, createParkAreaItem(player, parkAreaSelected, sort, parkAreaSelected == parkArea))
        }

        val warps = WarpManager.getWarps().filter { player.isRankOrHigher(it.rankNeeded) }.filter { it.parkArea == parkArea }.sortedBy { it.type == sort }
        for (i in (page - 1) * 36 until page * 36) {
            if (i >= warps.size) break
            val warp = warps[i]
            val slot = (i % 36) + 9
            inventory.setItem(slot, createWarpItem(player, warp))
        }

        inventory.setItem(53, createSortItem(player, parkArea, sort, page))

        player.openInventory(inventory)

    }

    private fun createSortItem(player: Player, parkArea: ParkArea, sort: WarpType, page: Int): ItemStack {

        var sortLore = ""

        for (sortType in WarpType.entries) {
            sortLore += if (sortType == sort) "<color:#2ecc71>↪ "
            else "<color:#f5f6fa>・ "

            sortLore += player.translate("menus.warps.type.${sortType.name}")?.message ?: sortType.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(player.locale()) else it.toString() }
            sortLore += "\n"
        }

        val currentSortIndex = WarpType.entries.indexOf(sort)
        val nextSortIndex = (currentSortIndex + 1) % WarpType.entries.size
        val nextSort = WarpType.entries[nextSortIndex]

        return Material.NAME_TAG.toItemBuilder {
            display("<color:#e67e22>" + (player.translate("menus.warps.sorting")?.message ?: "Sorting"))
            lore(*sortLore.split("\n").toTypedArray())
            onClick {
                openMenu(player, parkArea, nextSort, page)
            }
        }.build()
    }

    private fun createParkAreaItem(player: Player, parkArea: ParkArea, sort: WarpType, selected: Boolean = false): ItemStack {
        return parkArea.material.toItemBuilder {
            display(player.translate("menus.park_areas.${parkArea.name}")?.message ?: parkArea.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(player.locale()) else it.toString() })
            lore(
                TEXT_GRAY + (player.translate("menus.park_areas.${parkArea.name}.description")?.message ?: "No description"),
                if (selected) "<color:#2ecc71>Selected</color>" else ""
            )
            customModelData(parkArea.customModelData)
            setGlinting(selected)
            onClick {
                if (selected) return@onClick
                openMenu(player, parkArea, sort, 1)
            }
        }.build()
    }

    private fun createWarpItem(player: Player, warp: Warp): ItemStack {
        return warp.material.toItemBuilder {
            display("<color:${warp.rankNeeded.rank.color}>${warp.name}")
            customModelData(warp.customModelData)
            onClick {
                player.sendInfo("Teleporting to ${warp.name}")
                player.teleportAsync(warp.location, TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_PASSENGERS,
                    TeleportFlag.EntityState.RETAIN_VEHICLE)
            }
        }.build()
    }

}