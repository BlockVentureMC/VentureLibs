package net.blockventuremc.modules.structures.vehicle

import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class AirplaneVehicle(name: String, position: Vector, rotation: Vector): CustomVehicle(name, position, rotation) {


    override fun spawn() {
        super.spawn()

        armorStand?.apply {
            getAttribute(Attribute.GENERIC_GRAVITY)?.baseValue = 0.0
        }

    }

    override fun vehicleMovement(player: Player, packet: ServerboundPlayerInputPacket) {
        armorStand?.let { armorStand ->
            val onGround = armorStand.isOnGround
            val verticalInput = (packet.zza) * 0.35f * (if (onGround) 1.0f else 0.4f) * (if (packet.zza < 0) 0.6f else 1.0f)
            val horizontalInput = (packet.xxa * -1.0f) * 0.14f * if (onGround) 1.0f else 0.4f

            val targetYaw = armorStand.location.yaw + (-packet.xxa * 6)
            armorStand.setRotation(targetYaw, 0.0f)

            armorStand.velocity = armorStand.velocity.add (armorStand.location.direction.multiply(verticalInput))
        }


    }

}