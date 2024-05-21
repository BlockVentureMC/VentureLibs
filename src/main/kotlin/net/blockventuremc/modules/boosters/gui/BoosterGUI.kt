package net.blockventuremc.modules.boosters.gui

import dev.fruxz.stacked.text
import net.blockventuremc.cache.BoosterCache
import net.blockventuremc.consts.BOOSTER_IDENTIFIER
import net.blockventuremc.consts.NAMESPACE_ITEM_IDENTIFIER
import net.blockventuremc.database.model.BitBoosters
import net.blockventuremc.extensions.*
import net.blockventuremc.utils.itembuilder.toItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object BoosterGUI {
    fun openBoosterGUI(player: Player) {
        val inv = Bukkit.createInventory(null, 9 * 6, text("<gradient:#45aaf2:#8854d0>Boosters</gradient>"))

        val boosters = BoosterCache.getUserBoosters(player.uniqueId.toString())

        // fill until booster size, dont fill the rest
        for (i in 0 until boosters.size.coerceAtMost(54)) {
            inv.setItem(i, getBoosterItem(boosters[i], player))
        }

        fillEmptyAndOpenInventory(player, inv, "booster_gui")

    }

    private fun getBoosterItem(booster: BitBoosters, player: Player): ItemStack {
        val lang = player.toBlockUser().language.locale
        return Material.GOLD_NUGGET.toItemBuilder {
            display(
                player.translate("booster.category.${booster.category.toString().lowercase()}")?.message ?: ("Unknown Booster"),
            )

            addPersistentData(
                NAMESPACE_ITEM_IDENTIFIER,
                booster.endTime.javaInstant.toEpochMilli().toString(),
            )

            val gray = "<color:#dfe6e9>"
            val owner = booster.owner.toOfflinePlayer()



            lore(
                gray + (player.translate("booster.until", mapOf("time" to booster.endTime.formatToDay(lang)))?.message
                    ?: (booster.endTime.formatToDay(lang))),
                gray + (player.translate("booster.modifier", mapOf("modifier" to booster.modifier.toString()))?.message
                    ?: (booster.modifier.toString())),
                gray + (if (booster.user) player.translate("booster.user_only", mapOf("user" to owner.name))?.message ?: ("Only for ${owner.name}") else player.translate("booster.global")?.message ?: ("Global Booster")),

                if (player.hasPermission("booster.delete")) {
                    gray + player.translate("booster.remove")?.message
                } else {
                   gray + ""
                }
            )

        }.build()
    }
}