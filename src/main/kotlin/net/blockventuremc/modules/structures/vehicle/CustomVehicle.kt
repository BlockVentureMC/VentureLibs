package net.blockventuremc.modules.structures.vehicle

import net.blockventuremc.modules.structures.RootAttachment
import net.blockventuremc.modules.structures.StructureType
import net.blockventuremc.modules.structures.setCustomType
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.bukkit.attribute.Attribute

class CustomVehicle(name: String, position: Vector, rotation: Vector) : RootAttachment(name, position, rotation) {

    var armorStand: ArmorStand? = null

    override fun spawn() {
        val location = bukkitLocation
        armorStand = location.world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand
        armorStand?.apply {
            isInvulnerable = true
            setBasePlate(false)
            isVisible = false
            isSmall = true
            setCustomType(StructureType.VEHICLE)
            getAttribute(Attribute.GENERIC_STEP_HEIGHT)?.baseValue = 0.8
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
        val targetPosition = armorStand?.location ?: return
        position = targetPosition.toVector()
        localRotation = Vector(0.0f, targetPosition.yaw, 0.0f)
    }

    //asynchronous
    fun vehicleMovement(player: Player, packet: ServerboundPlayerInputPacket) {
        armorStand?.let { armorStand ->
            val onGround = armorStand.isOnGround
            val verticalInput = (packet.zza) * 4.8f * (if (onGround) 1.0f else 0.4f) * (if (packet.zza < 0) 0.6f else 1.0f)
            val horizontalInput = (packet.xxa * -1.0f) * 0.14f * if (onGround) 1.0f else 0.4f

            val targetYaw = armorStand.location.yaw + (-packet.xxa * 6)
            armorStand.setRotation(targetYaw, 0.0f)

            armorStand.velocity = armorStand.velocity.add (armorStand.location.direction.multiply(verticalInput))
        }
    }

}