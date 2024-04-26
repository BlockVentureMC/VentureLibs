package net.blockventuremc.modules.titles

import com.rainbowislands.utility.utils.ItemBuilder
import com.rainbowislands.utility.utils.toItemBuilder
import net.blockventuremc.extensions.translate
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Represents a category for titles.
 *
 * @property display The display name of the title category.
 * @property description The description of the title category.
 * @property icon The icon representing the title category in a GUI.
 */
enum class TitleCategory(val display: (Player) -> String, private val description: (Player) -> String, private val icon: ItemBuilder) {
    GENERIC({ player -> player.translate("title.categories.generic.display")?.message ?: "<#dff9fb>Generic" }, { player -> player.translate("title.categories.generic.description")?.message ?: "Generic titles" }, Material.BOOK.toItemBuilder()),
    RIDE_COUNTER({ player -> player.translate("title.categories.ride_counter.display")?.message ?: "<#dff9fb>Ride Counters" }, { player -> player.translate("title.categories.ride_counter.description")?.message ?: "Ride counter titles" }, Material.SADDLE.toItemBuilder()),
    ;

    /**
     * Returns the graphical representation of the GUI icon.
     *
     * @param player The player to get the icon for.
     * @return The item stack representing the GUI icon.
     */
    fun getIcon(player: Player): ItemBuilder {
        return icon.display(display(player)).lore(*description(player).split("\n").map { "<#778ca3>$it" }.toTypedArray())
    }
}