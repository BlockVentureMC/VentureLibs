package net.blockventuremc.modules.structures.vehicle

import net.blockventuremc.extensions.lerp
import net.blockventuremc.modules.structures.deltaTime
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import org.bukkit.FluidCollisionMode
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import kotlin.math.max

class AirplaneVehicle(name: String, position: Vector, rotation: Vector): CustomVehicle(name, position, rotation) {


    override fun spawn() {
        super.spawn()

        armorStand?.apply {
            getAttribute(Attribute.GENERIC_GRAVITY)?.baseValue = 0.00
            getAttribute(Attribute.GENERIC_STEP_HEIGHT)?.baseValue = 1.8
        }

    }

    var groundCollision: RayTraceResult? = null

    var forwardForce = 0.0f
    var lastYaw = 0.0f

    override fun vehicleMovement(player: Player, packet: ServerboundPlayerInputPacket) {

        if(player.uniqueId != owner) return

        armorStand?.let { armorStand ->

            groundCollision = armorStand.world.rayTraceBlocks(armorStand.location, Vector(0,-1,0), 1.0, FluidCollisionMode.SOURCE_ONLY, true)
            val onGround = groundCollision?.hitBlock != null

            val verticalInput = (packet.zza) * 0.90f * (if (packet.zza < 0) 0.6f else 1.0f)

            forwardForce = lerp(forwardForce, verticalInput, deltaTime, 0.8f)

                var targetYaw = player.location.yaw
                var targetPitch = player.location.pitch
                var angle = lastYaw - targetYaw;
                if (angle > 90.0 || angle < -90.0) {
                    angle = 0.0f;
                }

                tilt += angle * -2.0f;
                tilt *= 0.7f;
                tilt = tilt.coerceIn(-40.0f, 40.0f);
                yaw = targetYaw
            if(onGround) {
                pitch = if(targetPitch < 0.0f) targetPitch else 0.0f
            } else {
                pitch = targetPitch
            }

                lastYaw = targetYaw

            armorStand.setRotation(yaw, pitch)
            armorStand.velocity = armorStand.location.direction.multiply(forwardForce)

        }


    }

}