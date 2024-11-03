package net.blockventuremc.modules.structures.vehicle

import io.papermc.paper.entity.TeleportFlag
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.sin

class WaterVehicle(name: String, position: Vector, rotation: Vector): CustomVehicle(name, position, rotation) {

    lateinit var groundBlock: Block
    lateinit var lastGroundBlock: Block

    override fun vehicleMovement(player: Player, packet: ServerboundPlayerInputPacket) {

        if(packet.zza == 0.0f && packet.xxa == 0.0f && (!packet.isJumping)) return

        //if(player.uniqueId != uuid) return

        armorStand?.let { armorStand ->
            val onGround = armorStand.isOnGround || groundBlock.isSolid
            val verticalInput = (packet.zza) * 0.35f * (if (onGround) 0.03f else 0.4f) * (if (packet.zza < 0) 0.6f else 1.0f)

            val targetYaw = armorStand.location.yaw + (-packet.xxa * (if (onGround) 2 else 7))
            armorStand.setRotation(targetYaw, 0.0f)

            val jumpVelocity = Vector(0.0, if (packet.isJumping && groundBlock.type == Material.WATER) 0.3 else 0.0, 0.0)

            armorStand.velocity = armorStand.velocity.add(armorStand.location.direction.multiply(verticalInput).add(jumpVelocity))
        }

    }

    override fun update() {
        armorStand?.let { armorStand ->
            val velocity = armorStand.velocity

            val targetRotationPosition = Vector(0.0, 3.9, 0.0).add(velocity.clone().multiply(-1))
            val normalizedTargetDirection = targetRotationPosition.subtract(Vector()).toVector3f().normalize()

            val quaternion = Quaternionf().rotationTo(Vector3f(0f, 1f, 0f).normalize(), normalizedTargetDirection)
            //quaternion.rotateY(Math.toRadians(-targetLocation.yaw.toDouble()).toFloat())

            //val transform = itemDisplay!!.transformation
            //transform.rightRotation.set(quaternion)

            //itemDisplay!!.interpolationDelay = 0
            //itemDisplay!!.transformation = transform
            groundBlock = armorStand.location.add(0.0,-0.1,0.0).block

            if(groundBlock.isSolid || armorStand.isOnGround) {
                localPosition = Vector(0.0, 0.43, 0.0)
            } else {
                val offset = sin(armorStand.ticksLived * 0.1) * 0.09
                localPosition = Vector(0.0, offset, 0.0)
                if(groundBlock.type == Material.AIR && lastGroundBlock.type != Material.WATER) {
                    armorStand.velocity = armorStand.velocity.add(Vector(0.0, -0.07, 0.0))
                } else if(groundBlock.type == Material.WATER && lastGroundBlock.type != Material.AIR) {
                    armorStand.velocity = armorStand.velocity.add(Vector(0.0,0.07,0.0))
                }
            }
        }
        lastGroundBlock = groundBlock
        super.update()
    }

    var lastUpdateWater = false

    override fun spawn() {
        super.spawn()

        armorStand?.apply {
            getAttribute(Attribute.GENERIC_GRAVITY)?.baseValue = 0.0
            groundBlock = location.add(0.0,-0.1,0.0).block
            lastGroundBlock = groundBlock
        }

    }

    override val localTransform: Matrix4f
        get() {
            var matrix = Matrix4f().translate(localPosition.toVector3f())

            val targetRotationPosition = Vector(0.0, 3.9, 0.0).add(velocity.multiply(-1))
            val normalizedTargetDirection = targetRotationPosition.toVector3f().normalize()

            val quaternion = Quaternionf().rotationTo(Vector3f(0f, 1f, 0f).normalize(), normalizedTargetDirection)
            val yaw = Math.toRadians(localRotation.y).toFloat()
            quaternion.rotateY(-yaw)

            //val pitch = Math.toRadians(localRotation.x).toFloat()
            //val roll = Math.toRadians(localRotation.z).toFloat()
            //quaternion.rotateX(pitch)
            //quaternion.rotateZ(roll)

            matrix.rotate(quaternion)
            return matrix
        }


}