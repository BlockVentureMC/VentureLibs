package net.blockventuremc.modules.`fun`.baloon

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendError
import net.blockventuremc.extensions.sendInfo
import net.blockventuremc.modules.structures.StructureManager
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@VentureCommand(
    name = "balloon",
    description = "Spawn your Balloon!",
    permission = "blockventure.balloon",
    permissionDefault = PermissionDefault.TRUE,
    usage = "/balloon",
    aliases = ["b", "balloon"]
)
class BalloonCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false

        val player = sender
        val item = player.inventory.itemInMainHand

        val currentBalloon = StructureManager.balloons[player]

        if (item.type == Material.AIR) {
            if(currentBalloon != null) {
                player.sendInfo("byeee ballloon! :C")
                return true
            }
            player.sendError("duu musst ein Block in der Hand haben um einen Balloon zu spawnen :3")
            return true
        }

        currentBalloon?.let { balloon ->
            balloon.remove()
            StructureManager.balloons.remove(player)
        }

        val balloon = ItemBalloon(player, item)
        balloon.stiffness = 0.02
        balloon.damping = 0.91
        balloon.ydamping = 0.91
        balloon.test = 0.01

        balloon.spawn()
        StructureManager.balloons[player] = balloon

        player.sendInfo("balloon spawned! :)")

        return true
    }

}