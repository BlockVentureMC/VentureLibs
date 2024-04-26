package net.blockventuremc.modules.placeholders

import me.neznamy.tab.api.TabAPI
import net.blockventuremc.extensions.toDatabaseUser
import net.blockventuremc.extensions.translate
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

    placeholderManager.registerRelationalPlaceholder("%rel_title%", 5000) { player, viewer ->
        val title = (player.player as Player).toDatabaseUser().selectedTitle
        return@registerRelationalPlaceholder if (title == null) {
            (viewer.player as Player).translate("title.none")?.message ?: "<color:#4b6584>No title"
        } else {
            title.display(viewer.player as Player)
        }
    }
}
