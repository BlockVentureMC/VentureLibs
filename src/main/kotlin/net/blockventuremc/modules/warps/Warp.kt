package net.blockventuremc.modules.warps

import net.blockventuremc.modules.general.model.Ranks
import org.bukkit.Location
import org.bukkit.Material

/**
 * Warp represents a location that players can teleport to.
 *
 * @property name the name of the warp
 * @property location the location coordinates of the warp
 * @property rankNeeded the minimum rank required to access the warp, default is [Ranks.Trial]
 *
 * @see [Ranks]
 */
data class Warp(
    val name: String,
    val location: Location,
    val rankNeeded: Ranks = Ranks.TEAM,
    val type: WarpType = WarpType.GENERIC,
    val parkArea: ParkArea = ParkArea.PARADISE_BAY,
    val material: Material = Material.DIAMOND,
    val customModelData: Int = 0
)
