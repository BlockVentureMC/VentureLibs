package net.blockventuremc.modules.general.events

import com.destroystokyo.paper.MaterialSetTag
import net.blockventuremc.VentureLibs
import net.blockventuremc.consts.BLOCKVENTURE_DOOR_LOCKS
import net.blockventuremc.extensions.canBuild
import net.blockventuremc.extensions.isRankOrHigher
import net.blockventuremc.modules.customblockdata.CustomBlockData
import net.blockventuremc.modules.general.model.Ranks
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class LockableDoorListener : Listener {


    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player.canBuild) return
        val block = event.clickedBlock ?: return

        val clickedType = block.type
        if (!MaterialSetTag.DOORS.isTagged(clickedType)) return

        val bottomBlock =
            if ((block.blockData as Door).half == Bisected.Half.TOP) block.getRelative(org.bukkit.block.BlockFace.DOWN) else block

        val customBlockData = CustomBlockData(bottomBlock, VentureLibs.instance)
        if (!customBlockData.has(BLOCKVENTURE_DOOR_LOCKS, PersistentDataType.BYTE)) return

        val customRankData = customBlockData[BLOCKVENTURE_DOOR_LOCKS, PersistentDataType.BYTE] ?: return
        val rank = Ranks.entries[customRankData.toInt()]
        if (event.player.isRankOrHigher(rank)) return

        event.isCancelled = true
    }

}