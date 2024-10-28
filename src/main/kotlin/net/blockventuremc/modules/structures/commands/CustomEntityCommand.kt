package net.blockventuremc.modules.structures.commands

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.modules.structures.*
import net.blockventuremc.modules.structures.impl.Seat
import net.blockventuremc.utils.itembuilder.ItemBuilder
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.PermissionDefault
import org.bukkit.util.Vector

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
        val rootAttachment = RootAttachment("test", player.location.toVector(), Vector())
        rootAttachment.addChild(
            ItemAttachment(
                "base",
                ItemBuilder(Material.DIAMOND_SWORD).customModelData(100).build(),
                Vector(0.0, 0.4, 0.0),
                Vector()
            )
        )
        rootAttachment.world = player.world
        val rotator = Attachment("rotator", Vector(), Vector())
        rootAttachment.addChild(rotator)

        rotator.addChild(Seat("seat1", Vector(0.39, 0.6, 0.3), Vector()))
        rotator.addChild(Seat("seat2", Vector(-0.39, 0.6, 0.3), Vector()))
        rotator.addChild(Seat("seat3", Vector(0.39, 0.6, -0.3), Vector()))
        rotator.addChild(Seat("seat4", Vector(-0.39, 0.6, -0.3), Vector()))

        rotator.addChild(
            ItemAttachment(
                "model",
                ItemBuilder(Material.DIAMOND_SWORD).customModelData(99).build(),
                Vector(0.0, 1.0, 0.0),
                Vector()
            )
        )
        rotator.addChild(
            ItemAttachment(
                "test",
                ItemStack(Material.COMMAND_BLOCK),
                Vector(2.0, 0.0, 0.0),
                Vector(0.0, 0.0, 0.0)
            )
        )

        rootAttachment.initialize()
        rootAttachment.animation = object : Animation() {
            override fun animate() {

                rootAttachment.position = player.location.toVector()
                rootAttachment.localRotation =
                    Vector(player.location.pitch.toDouble(), player.location.yaw.toDouble(), 0.0)
                rotator.localRotation.add(Vector(0.0, 1.0, 0.0))
            }
        }
        StructureManager.structures[rootAttachment.uuid] = rootAttachment
        player.sendMessage("§eCustom Entity Spawned!")
        return true
    }

}