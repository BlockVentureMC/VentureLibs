package net.blockventuremc.modules.structures

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.utils.itembuilder.ItemBuilder
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.PermissionDefault
import org.bukkit.util.Vector
import java.util.UUID

@VentureCommand(
    name = "customentity",
    description = "Spawn Custom Entities!",
    permission = "blockventure.customentity",
    permissionDefault = PermissionDefault.TRUE,
    usage = "/customentity",
    aliases = ["ce", "customentity", "custom"]
)
class CustomEntityCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false

        val player = sender
//
        val customEntity = CustomEntity("test", player.world,player.location.toVector(), Vector())
        customEntity.addChild(ItemAttachment("base", ItemBuilder(Material.DIAMOND_SWORD).customModelData(100).build(), Vector(0.0, 0.4, 0.0), Vector()))
        val rotator = Attachment("rotator", Vector(), Vector())
        customEntity.addChild(rotator)

        rotator.addChild(Seat("seat1", Vector(0.39, 0.6, 0.3), Vector()))
        rotator.addChild(Seat("seat2", Vector(-0.39, 0.6, 0.3), Vector()))
        rotator.addChild(Seat("seat3", Vector(0.39, 0.6, -0.3), Vector()))
        rotator.addChild(Seat("seat4", Vector(-0.39, 0.6, -0.3), Vector()))

        rotator.addChild(ItemAttachment("model", ItemBuilder(Material.DIAMOND_SWORD).customModelData(99).build(), Vector(0.0, 1.0, 0.0), Vector()))
        rotator.addChild(ItemAttachment("test", ItemStack(Material.COMMAND_BLOCK), Vector(2.0, 0.0, 0.0), Vector(0.0, 0.0, 0.0)))

        customEntity.initialize()
        customEntity.animation = object : Animation() {
            override fun animate() {

                customEntity.position = player.location.toVector()
                customEntity.localRotation = Vector(player.location.pitch.toDouble(), player.location.yaw.toDouble(), 0.0)
                rotator.localRotation.add(Vector(0.0, 1.0, 0.0))
            }
        }
        StructureManager.structures[customEntity.uuid] = customEntity
        player.sendMessage("§eCustom Entity Spawned!")
        return true
    }

}