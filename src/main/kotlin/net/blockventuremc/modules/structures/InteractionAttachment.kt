package net.blockventuremc.modules.structures

import io.papermc.paper.entity.TeleportFlag
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.util.Vector

class InteractionAttachment(name: String, localPosition: Vector, var height: Float, var with: Float) : Attachment(name, localPosition, Vector()) {

    var interaction: Interaction? = null

    override fun spawn() {
        val location = bukkitLocation

        interaction = location.world.spawnEntity(location, EntityType.INTERACTION) as Interaction
        interaction?.apply {
            isCustomNameVisible = false
            setCustomType(StructureType.GENERIC, root.uuid.toString())
        }
        interaction!!.interactionHeight = height
        interaction!!.interactionWidth = with
    }

    override fun updateTransform() {
        interaction?.teleport(
            bukkitLocation.add(Vector(0.0, -height * 0.5, 0.0)),
            TeleportFlag.EntityState.RETAIN_PASSENGERS
        )
    }

}