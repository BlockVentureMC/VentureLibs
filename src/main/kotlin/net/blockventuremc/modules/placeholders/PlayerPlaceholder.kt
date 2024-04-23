package net.blockventuremc.modules.placeholders

import me.neznamy.tab.api.TabAPI
import net.blockventuremc.extensions.toDatabaseUser
import org.bukkit.entity.Player


fun registerPlaceholders() {

    val placeholderManager = TabAPI.getInstance().placeholderManager

    placeholderManager.registerPlayerPlaceholder("%rank%", 5000) { player ->
        (player.player as Player).toDatabaseUser().rank
    }

    placeholderManager.registerPlayerPlaceholder("%rankord%", 5000) { player ->
        (player.player as Player).toDatabaseUser().rank.ordinal
    }

    placeholderManager.registerPlayerPlaceholder("%color%", 5000) { player ->
        (player.player as Player).toDatabaseUser().rank.color
    }
}
