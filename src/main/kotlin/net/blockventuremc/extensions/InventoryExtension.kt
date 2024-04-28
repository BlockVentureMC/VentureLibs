package net.blockventuremc.extensions

import net.blockventuremc.consts.*
import net.blockventuremc.utils.itembuilder.ItemBuilder
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import net.blockventuremc.utils.itembuilder.toItemBuilder

fun fillEmptyAndOpenInventory(player: Player, inv: Inventory, identifier: String? = null, vararg identifiers: Map<NamespacedKey, String>? = arrayOf()) {
    fillEmpty(inv)
    if (identifier != null) inv.identify(identifier, *identifiers)
    player.openInventory(inv)
}

fun openWithIdentifier(player: Player, inv: Inventory, identifier: String? = null, vararg identifiers: Map<NamespacedKey, String>? = arrayOf()) {
    if (identifier != null) inv.identify(identifier, *identifiers)
    player.openInventory(inv)
}

fun fillEmpty(inventory: Inventory, identifier: String? = null, vararg identifiers: Map<NamespacedKey, String>? = arrayOf()) {
    val item = PLACEHOLDER_GRAY
    for (i in 0 until inventory.size) {
        if (inventory.getItem(i) == null || inventory.getItem(i)!!.type == Material.AIR) {
            inventory.setItem(i, item)
        }
    }
    if (identifier != null) inventory.identify(identifier, *identifiers)
}

fun Inventory.fillEmpty(filler: ItemStack, identifier: String? = null, vararg identifiers: Map<NamespacedKey, String>? = arrayOf()) {
    for (i in 0 until this.size) {
        if (this.getItem(i) == null || this.getItem(i)!!.type == Material.AIR) {
            this.setItem(i, filler)
        }
    }
    if (identifier != null) this.identify(identifier, *identifiers)
}

fun Inventory.identify(identifier: String, vararg identifiers: Map<NamespacedKey, String>? = arrayOf()) {
    this.setItem(0, this.getItem(0)?.toItemBuilder {
        addPersistentData(NAMESPACE_GUI_IDENTIFIER, identifier)
        identifiers.forEach { map ->
            map?.forEach { (key, value) ->
                addPersistentData(key, value)
            }
        }
    }?.build() ?: Material.LIGHT_GRAY_STAINED_GLASS_PANE.toItemBuilder {
        addPersistentData(NAMESPACE_GUI_IDENTIFIER, identifier)
        identifiers.forEach { map ->
            map?.forEach { (key, value) ->
                addPersistentData(key, value)
            }
        }
    }.build())
}

fun Inventory.isIdentifiedAs(identifier: String): Boolean {
    return this.getItem(0)?.itemMeta?.persistentDataContainer?.get(
        NAMESPACE_GUI_IDENTIFIER,
        PersistentDataType.STRING
    ) == identifier
}

val Inventory.identifier: String?
    get() = this.getItem(0)?.itemMeta?.persistentDataContainer?.get(
        NAMESPACE_GUI_IDENTIFIER,
        PersistentDataType.STRING
    )

fun Inventory.identifier(namespacedKey: NamespacedKey): String? {
    return this.getItem(0)?.itemMeta?.persistentDataContainer?.get(
        namespacedKey,
        PersistentDataType.STRING
    )
}

fun ItemStack.isIdentifiedAs(identifier: String): Boolean {
    return this.itemMeta?.persistentDataContainer?.get(
        NAMESPACE_ITEM_IDENTIFIER,
        PersistentDataType.STRING
    ) == identifier
}

val ItemStack.identifier: String?
    get() = this.itemMeta?.persistentDataContainer?.get(
        NAMESPACE_ITEM_IDENTIFIER,
        PersistentDataType.STRING
    )

fun ItemStack.identifier(namespacedKey: NamespacedKey): String? {
    return this.itemMeta?.persistentDataContainer?.get(
        namespacedKey,
        PersistentDataType.STRING
    )
}

fun ItemStack.hasKey(namespacedKey: NamespacedKey): Boolean {
    return this.itemMeta?.persistentDataContainer?.has(
        namespacedKey,
        PersistentDataType.STRING
    ) ?: false
}

fun ItemStack.getKey(namespacedKey: NamespacedKey): String? {
    return this.itemMeta?.persistentDataContainer?.get(
        namespacedKey,
        PersistentDataType.STRING
    )
}

val PLACEHOLDER_GRAY = ItemBuilder(Material.GRAY_STAINED_GLASS_PANE) {
    display(" ")
}.build()
val PLACEHOLDER_BACK = ItemBuilder(Material.STRUCTURE_VOID){
    display("${TEXT_GRADIENT_DEFAULT}Zurück")
    addPersistentData(NAMESPACE_ITEM_IDENTIFIER, "back")
}

fun Inventory.setItem(range: IntProgression, item: ItemStack) {
    range.forEach { this.setItem(it, item) }
}