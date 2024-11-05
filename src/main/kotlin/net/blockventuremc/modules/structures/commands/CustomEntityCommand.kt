package net.blockventuremc.modules.structures.commands

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendSuccess
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
        val ball = Ball("ball", player.location.toVector(), Vector())
        ball.addChild(
            ItemAttachment(
                "base",
                ItemBuilder(Material.PLAYER_HEAD).textureFromMineSkin("02b00dc2105c439bac5a89a09b78ee4b").build(),
                Vector(0.0, 0.76, 0.0),
                Vector()
            ).setScale(1.5f)
        )
        ball.world = player.world

        ball.initialize()
        ball.animation = object : Animation() {
            override fun animate() {
                //ball.localRotation.add(Vector(0.0, 1.0, 0.0))
            }
        }
        StructureManager.structures[ball.uuid] = ball
        player.sendSuccess("Ball Spawned!")
        return true
    }

}