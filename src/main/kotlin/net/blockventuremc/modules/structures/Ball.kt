package net.blockventuremc.modules.structures

import net.blockventuremc.consts.NAMESPACE_BALL_IDENTIFIER
import net.blockventuremc.consts.NAMESPACE_CUSTOMENTITY_IDENTIFIER
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class Ball(name: String, position: Vector, rotation: Vector) : RootAttachment(name, position, rotation)  {

    var armorStand: ArmorStand? = null
    var interaction: Interaction? = null
    var lastVelocity = Vector()
    var yaw = 0.0f

    override fun spawn() {
        val location = bukkitLocation
        armorStand = location.world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand
        armorStand?.apply {
            isSilent = true
            isInvulnerable = true
            setBasePlate(false)
            isVisible = true
            isSmall = true
            setCustomType(StructureType.VEHICLE)
            persistentDataContainer[NAMESPACE_BALL_IDENTIFIER, PersistentDataType.STRING] = uuid.toString()
            getAttribute(Attribute.GENERIC_STEP_HEIGHT)?.baseValue = 0.0
        }
        interaction = location.world.spawnEntity(location, EntityType.INTERACTION) as Interaction
        interaction?.apply {
            interactionHeight = 0.6f
            interactionWidth = 0.6f
            isCustomNameVisible = false
            armorStand?.addPassenger(this)
            setCustomType(StructureType.GENERIC, uuid.toString())
        }
    }

    override fun despawn() {
        armorStand?.remove()
    }

    override fun update() {
        movementUpdate()
        super.update()
    }

    fun movementUpdate() {
        armorStand?.let { armorStand ->


            var currentVelocity = armorStand.velocity
            if (currentVelocity.x == 0.0) {
                currentVelocity.x = -lastVelocity.x * 0.9
                this.yaw = currentVelocity.z.toFloat()
            } else if (kotlin.math.abs(lastVelocity.x - currentVelocity.x) < 0.15) {
                currentVelocity.x = lastVelocity.x * 0.975
            }

            if (currentVelocity.z == 0.0) {
                currentVelocity.z = -lastVelocity.z * 0.9
                this.yaw = currentVelocity.z.toFloat()
            } else if (kotlin.math.abs(lastVelocity.z - currentVelocity.z) < 0.15) {
                currentVelocity.z = lastVelocity.z * 0.975
            }

            armorStand.location.direction = currentVelocity
            armorStand.velocity = currentVelocity


            val targetPosition = armorStand.location
            position = targetPosition.toVector()

            lastVelocity = currentVelocity
        }

    }


}