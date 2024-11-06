package net.blockventuremc.modules.structures.vehicle

import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.blockventuremc.consts.NAMESPACE_CUSTOMENTITY_IDENTIFIER
import net.blockventuremc.modules.structures.StructureManager
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

object PacketHandler {

    fun vehicleMovement(player: Player, packet: ServerboundPlayerInputPacket) {
        val vehicle = player.vehicle
        val uuidString =  vehicle?.persistentDataContainer?.get(NAMESPACE_CUSTOMENTITY_IDENTIFIER, PersistentDataType.STRING) ?: return
        val uuid = UUID.fromString(uuidString)

        StructureManager.vehicles[uuid]?.vehicleMovement(player, packet)
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