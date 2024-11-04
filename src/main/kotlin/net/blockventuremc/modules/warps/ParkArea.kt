package net.blockventuremc.modules.warps

import org.bukkit.Material

enum class ParkArea(val material: Material, val customModelData: Int = 0) {

    ASHEN_SHORES(Material.CAMPFIRE, 10000),
    PARADISE_BAY(Material.TRIDENT, 10000),
    SERPENT_JUNGLE(Material.TOTEM_OF_UNDYING, 10000),
    UNKNOWN_AREA(Material.MACE, 10000),
    UNKNOWN_AREA_2(Material.WOLF_ARMOR, 10000),
    UNKNOWN_AREA_3(Material.WIND_CHARGE, 10000),

}