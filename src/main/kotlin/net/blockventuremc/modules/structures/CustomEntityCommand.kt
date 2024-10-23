package net.blockventuremc.modules.structures

import net.blockventuremc.annotations.VentureCommand
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

        val customEntity = CustomEntity("test", player.world,player.location.toVector(), Vector())
        customEntity.addChild(ItemAttachment("test", ItemStack(Material.COMMAND_BLOCK), Vector(0.0, 0.0, 0.0), Vector(0.0, 0.0, 0.0)))

        customEntity.initialize()
        player.sendMessage("§eCustom Entity Spawned!")
        return true
    }

}