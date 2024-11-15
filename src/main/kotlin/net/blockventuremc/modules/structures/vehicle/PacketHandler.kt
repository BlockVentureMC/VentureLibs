package net.blockventuremc.modules.structures.vehicle

import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.blockventuremc.consts.NAMESPACE_CUSTOMENTITY_IDENTIFIER
import net.blockventuremc.modules.structures.StructureManager
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.AABB
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.lang.reflect.Field
import java.util.UUID

object PacketHandler {

    fun vehicleMovement(player: Player, packet: ServerboundPlayerInputPacket) {
        val vehicle = player.vehicle
        val uuidString =  vehicle?.persistentDataContainer?.get(NAMESPACE_CUSTOMENTITY_IDENTIFIER, PersistentDataType.STRING) ?: return
        val uuid = UUID.fromString(uuidString)

        StructureManager.vehicles[uuid]?.vehicleMovement(player, packet)
    }

    fun setEntityHitbox(entity: Entity, width: Double, height: Double) {
        val nmsEntity = (entity as CraftEntity).handle
        // Setze eine neue AABB (Axis-Aligned Bounding Box) für die Hitbox-Größe

        val newBoundingBox = AABB(
            nmsEntity.boundingBox.minX,
            nmsEntity.boundingBox.minY,
            nmsEntity.boundingBox.minZ,
            nmsEntity.boundingBox.minX + width,
            nmsEntity.boundingBox.minY + height,
            nmsEntity.boundingBox.minZ + width
        )
        nmsEntity.setBoundingBox(newBoundingBox)
    }

    fun removeEntityPacket(player: Player, entity: Entity) {
        val entityID = entity.entityId
        val entityPlayer = (player as org.bukkit.craftbukkit.entity.CraftPlayer).handle as ServerPlayer
        val removeEntityPacket = ClientboundRemoveEntitiesPacket(entityID)
        entityPlayer.connection.send(removeEntityPacket)
    }

    fun movementPacketCheck(player: Player) {
        val channelDuplexHandler = object : ChannelDuplexHandler() {
            override fun channelRead(channelHandlerContext: ChannelHandlerContext, packet: Any) {
                if (packet is ServerboundPlayerInputPacket) {
                    super.channelRead(channelHandlerContext, packet)
                    vehicleMovement(player, packet)
                    return
                }
                super.channelRead(channelHandlerContext, packet)
            }
        }
        var channel: Channel? = null
        try {
            val entityPlayer = (player as org.bukkit.craftbukkit.entity.CraftPlayer).handle
            val playerConnectionField = entityPlayer.javaClass.getField("c")
            val playerConnection =
                playerConnectionField.get(entityPlayer) as net.minecraft.server.network.ServerPlayerConnection
            val networkManagerField =
                net.minecraft.server.network.ServerCommonPacketListenerImpl::class.java.getDeclaredField("e")
            networkManagerField.isAccessible = true
            val networkManager = networkManagerField.get(playerConnection) as net.minecraft.network.Connection

            val channelField = networkManager.javaClass.getField("n")
            channel = channelField.get(networkManager) as Channel

            channel.pipeline().addBefore("packet_handler", player.name, channelDuplexHandler)
        } catch (e: IllegalArgumentException) {
            // Bei Plugin-Neuladen, um doppelte Handler-Namen-Ausnahme zu verhindern
            if (channel == null) {
                return
            }
            if (!channel.pipeline().names().contains(player.name)) return
            channel.pipeline().remove(player.name)
            movementPacketCheck(player)
        } catch (_: IllegalAccessException) {
        } catch (_: NoSuchFieldException) {
        }
    }
}