package net.blockventuremc.modules.structures.vehicle

import net.blockventuremc.extensions.lerp
import net.blockventuremc.modules.structures.deltaTime
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class AirplaneVehicle(name: String, position: Vector, rotation: Vector): CustomVehicle(name, position, rotation) {


    override fun spawn() {
        super.spawn()

        armorStand?.apply {
            getAttribute(Attribute.GENERIC_GRAVITY)?.baseValue = 0.04
        }

    }

    var forwardForce = 0.0f
    var lastYaw = 0.0f

    override fun vehicleMovement(player: Player, packet: ServerboundPlayerInputPacket) {

        if(player.uniqueId != owner) return

        armorStand?.let { armorStand ->
            val onGround = armorStand.isOnGround

            val verticalInput = (packet.zza) * 0.15f * (if (packet.zza < 0) 0.6f else 1.0f)

            forwardForce = lerp(forwardForce, verticalInput, deltaTime, 0.8f)

                var targetYaw = player.location.yaw
                var targetPitch = player.location.pitch
                var angle = lastYaw - targetYaw;
                if (angle > 90.0 || angle < -90.0) {
                    angle = 0.0f;
                }

                tilt += angle * -1.7f;
                tilt *= 0.8f;
                tilt = tilt.coerceIn(-40.0f, 40.0f);

                armorStand.setRotation(targetYaw, targetPitch)
                lastYaw = targetYaw

            armorStand.velocity = armorStand.velocity.add( armorStand.location.direction.multiply(forwardForce))

        }


    }

}