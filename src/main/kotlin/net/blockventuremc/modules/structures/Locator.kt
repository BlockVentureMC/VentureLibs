package net.blockventuremc.modules.structures

import net.blockventuremc.extensions.lerpVector
import org.bukkit.util.Vector

class Locator(name: String, localPosition: Vector, localRotation: Vector) : Attachment(name, localPosition, localRotation) {

    var lerpPosition = Vector()

    override fun spawn() {
        lerpPosition = worldPosition
    }

    override fun updateTransform() {
        lerpPosition = lerpVector(lerpPosition, worldPosition, 1.0f/root.smoothFactor)
    }


}