package net.blockventuremc.modules.`fun`.baloon

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.modules.structures.StructureManager
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
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

        StructureManager.balloons[player]?.let { balloon ->
            balloon.remove()
            StructureManager.balloons.remove(player)

            if (item.type == Material.AIR) {
                player.sendMessage("byeee ballloon! :C")
                return true
            }
        }

        val balloon = Balloon(player, item)
        balloon.stiffness = 0.02
        balloon.damping = 0.91
        balloon.ydamping = 0.91
        balloon.test = 0.01

        balloon.spawn()
        StructureManager.balloons[player] = balloon

        player.sendMessage("balloon spawned! :)")

        return true
    }

}