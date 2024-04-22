package net.blockventuremc.utils


import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import dev.fruxz.stacked.text
import net.blockventuremc.extensions.toOfflinePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.lang.reflect.Field
import java.util.*
import java.util.Base64

class ItemBuilder(material: Material, count: Int = 1, dsl: ItemBuilder.() -> Unit = {}) {

    /**
     * The itemStack to get
     * equals to build()
     */
    var itemStack = ItemStack(material, count)

    init {
        dsl.invoke(this)
    }

    /**
     * Change the displayname of the item
     * @param displayName The new displayname (could be rgb)
     */
    fun display(displayName: String): ItemBuilder {
        val meta = itemStack.itemMeta
        meta.displayName(text(displayName).decoration(TextDecoration.ITALIC, false))
        itemStack.itemMeta = meta
        return this
    }

    fun <T: ItemMeta> meta(dsl: T.() -> Unit): ItemBuilder {
        val meta = itemStack.itemMeta as T
        dsl.invoke(meta)
        itemStack.itemMeta = meta
        return this
    }

    fun addPersistentData(key: NamespacedKey, value: String): ItemBuilder {
        val meta = itemStack.itemMeta
        meta.persistentDataContainer.set(key, PersistentDataType.STRING, value)
        itemStack.itemMeta = meta
        return this
    }

    fun <T : Any> addPersistentData(key: NamespacedKey, persistentDataType: PersistentDataType<T, T>, value: T): ItemBuilder {
        val meta = itemStack.itemMeta
        meta.persistentDataContainer.set(key, persistentDataType, value)
        itemStack.itemMeta = meta
        return this
    }

    fun addPersistentDataIf(key: NamespacedKey, value: String, condition: Boolean = false): ItemBuilder {
        if (condition) {
            val meta = itemStack.itemMeta
            meta.persistentDataContainer.set(key, PersistentDataType.STRING, value)
            itemStack.itemMeta = meta
            return this
        }
        return this
    }

    fun removePersistantDataIf(key: NamespacedKey, condition: Boolean = false): ItemBuilder {
        if (condition) {
            val meta = itemStack.itemMeta
            meta.persistentDataContainer.remove(key)
            itemStack.itemMeta = meta
            return this
        }
        return this
    }

    @FunctionalInterface
    fun interface Performer<T> {
        fun perform(itemBuilder: T): T
    }

    fun condition(condition: Boolean, consumer: Performer<ItemBuilder>): ItemBuilder {
        if (condition) {
            return consumer.perform(this)
        }
        return this
    }


    fun setOwner(uuid: UUID): ItemBuilder {
        if (itemStack.type != Material.PLAYER_HEAD) return this
        val skullMeta = itemStack.itemMeta as SkullMeta
        skullMeta.owningPlayer = Bukkit.getOfflinePlayer(uuid)
        itemStack.itemMeta = skullMeta
        return this
    }

    fun owner(name: String): ItemBuilder {
        if (itemStack.type != Material.PLAYER_HEAD) return this
        val skullMeta = itemStack.itemMeta as SkullMeta
        skullMeta.owningPlayer = Bukkit.getOfflinePlayer(name)
        itemStack.itemMeta = skullMeta
        return this
    }

    fun owner(offlinePlayer: OfflinePlayer): ItemBuilder {
        if (itemStack.type != Material.PLAYER_HEAD) return this
        val skullMeta = itemStack.itemMeta as SkullMeta
        skullMeta.owningPlayer = offlinePlayer
        itemStack.itemMeta = skullMeta
        return this
    }

    fun owner(uuid: UUID): ItemBuilder {
        if (itemStack.type != Material.PLAYER_HEAD) return this
        val skullMeta = itemStack.itemMeta as SkullMeta
        skullMeta.owningPlayer = uuid.toOfflinePlayer()
        itemStack.itemMeta = skullMeta
        return this
    }

    fun texture(texture: String): ItemBuilder {
        if (itemStack.type != Material.PLAYER_HEAD) return this
        val url = "https://textures.minecraft.net/texture/$texture"
        val profile = GameProfile(UUID.randomUUID(), null)
        val encodedData =
            Base64.getEncoder()
                .encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url)
                    .toByteArray())
        profile.properties.put("textures", Property("textures", String(encodedData)))
        try {
            val skullMeta = itemStack.itemMeta as SkullMeta
            val profileField: Field = skullMeta.javaClass.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField.set(skullMeta, profile)
            itemStack.setItemMeta(skullMeta)
        } catch (e1: NoSuchFieldException) {
            e1.printStackTrace()
        } catch (e1: IllegalArgumentException) {
            e1.printStackTrace()
        } catch (e1: IllegalAccessException) {
            e1.printStackTrace()
        }
        return this
    }


    /**
     * Change the displayname of the item if condition is true
     * @param displayName The new displayname (could be rgb)
     */
    fun displayIf(displayName: String, condition: Boolean = false): ItemBuilder {
        if (condition) {
            return display(displayName)
        }
        return this
    }

    /**
     * ClearDisplayIf
     */
    fun clearDisplayIf(condition: Boolean = false): ItemBuilder {
        if (condition) {
            return clearDisplay()
        }
        return this
    }

    /**
     * Clear the displayname of the item
     */
    fun clearDisplay(): ItemBuilder {
        val meta = itemStack.itemMeta
        meta.displayName(null)
        itemStack.itemMeta = meta
        return this
    }

    fun lore(vararg lores: String): ItemBuilder {
        val meta = itemStack.itemMeta
        var lore = listOf<Component>()

        lores.forEach {
            val lines = it.split("\n")
            for(line in lines) {
                lore += text(line)
            }
        }

        meta.lore(lore.map { Component.text().decoration(TextDecoration.ITALIC, false).append(it).build() })
        itemStack.itemMeta = meta
        return this
    }

    fun loreIf(vararg lores: String, condition: Boolean = false): ItemBuilder {
        if (condition) {
            return lore(*lores)
        }
        return this
    }

    fun clearLore(): ItemBuilder {
        val meta = itemStack.itemMeta
        meta.lore(null)
        itemStack.itemMeta = meta
        return this
    }

    fun clearLoreIf(condition: Boolean = false): ItemBuilder {
        if (condition) {
            return clearLore()
        }
        return this
    }

    /**
     * Add flags
     * @param flags
     */
    fun flag(vararg flags: ItemFlag): ItemBuilder {
        val meta = itemStack.itemMeta
        meta.addItemFlags(*flags)
        itemStack.itemMeta = meta
        return this
    }

    /**
     * Clear all flags
     */
    fun clearFlags(): ItemBuilder {
        val meta = itemStack.itemMeta
        meta.removeItemFlags(*ItemFlag.values())
        itemStack.itemMeta = meta
        return this
    }

    fun clearFlagsIf(condition: Boolean = false): ItemBuilder {
        if (condition) {
            return clearFlags()
        }
        return this
    }

    /**
     * Add enchants
     * @param enchants
     */
    fun enchant(enchants: Map<Enchantment, Int>): ItemBuilder {
        val meta = itemStack.itemMeta
        enchants.forEach {
            meta.addEnchant(it.key, it.value, true)
        }
        itemStack.itemMeta = meta
        return this
    }

    /**
     * Add enchants if condition is true
     * @param enchants
     * @param condition
     */
    fun enchantIf(enchants: Map<Enchantment, Int>, condition: Boolean = false): ItemBuilder {
        if (condition) {
            return enchant(enchants)
        }
        return this
    }

    /**
     * Clear all enchants
     */
    fun clearEnchants(): ItemBuilder {
        val meta = itemStack.itemMeta
        meta.enchants.forEach {
            meta.removeEnchant(it.key)
        }
        itemStack.itemMeta = meta
        return this
    }

    fun clearEnchantsIf(condition: Boolean = false): ItemBuilder {
        if (condition) {
            return clearEnchants()
        }
        return this
    }


    /**
     * Set material if condition is true
     * @param material
     * @param condition
     */
    fun type(material: Material): ItemBuilder {
        itemStack.type = material
        return this
    }

    /**
     * Set material if condition is true
     * @param material
     * @param condition
     */
    fun typeIf(material: Material, condition: Boolean = false): ItemBuilder {
        if (condition) {
            itemStack.type = material
        }
        return this
    }

    /**
     * The itemStack to get
     * equals to .itemStack
     */
    fun build(): ItemStack {
        return itemStack
    }

    companion object {

        val invalidMaterials = arrayListOf(
            Material.AIR,
            Material.CAVE_AIR,
            Material.VOID_AIR,
            Material.LAVA,
            Material.WATER,
        )

        fun fromItemStack(itemStack: ItemStack): ItemBuilder {
            var mat = if (ItemBuilder.invalidMaterials.contains(itemStack.type)) Material.GRASS_BLOCK else itemStack.type
            val builder = ItemBuilder(mat)
            builder.itemStack = itemStack
            return builder
        }
    }
}

fun Material.toItemBuilder(dsl: ItemBuilder.() -> Unit = {}): ItemBuilder {
    var mat = if (ItemBuilder.invalidMaterials.contains(this)) Material.GRASS_BLOCK else this
    return ItemBuilder(mat).apply(dsl)
}

fun ItemStack.toItemBuilder(dsl: ItemBuilder.() -> Unit = {}): ItemBuilder {
    var mat = if (ItemBuilder.invalidMaterials.contains(this.type)) Material.GRASS_BLOCK else this.type
    val builder = ItemBuilder(mat)
    builder.itemStack = this
    builder.dsl()
    return builder
}