package net.blockventuremc.modules.structures.vehicle

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.sendSuccess
import net.blockventuremc.modules.structures.Animation
import net.blockventuremc.modules.structures.Attachment
import net.blockventuremc.modules.structures.ItemAttachment
import net.blockventuremc.modules.structures.RootAttachment
import net.blockventuremc.modules.structures.StructureManager
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
import kotlin.collections.set


@VentureCommand(
    name = "vehicle",
    description = "Vehicles!",
    permission = "blockventure.vehicle",
    permissionDefault = PermissionDefault.TRUE,
    usage = "/vehicle",
    aliases = ["cart", "vehicle", "kart"]
)
class VehicleCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false

        val player = sender

        if (args.isEmpty()) {
            sender.sendMessagePrefixed("Usage: /vehicle <vehicle>")
            return true
        }

        val vehicleName = args[0]

        when (vehicleName) {
            "cart" -> {
                val vehicle = CustomVehicle("nicos vehicle", player.location.toVector(), Vector(0.0f,player.location.yaw,0.0f))
                vehicle.world = player.world
                vehicle.addChild(
                    ItemAttachment(
                        "model",
                        ItemBuilder(Material.DIAMOND_SWORD).customModelData(98).build(),
                        Vector(0.0, 0.3, 0.0),
                        Vector()
                    )
                )
                vehicle.addChild(Seat("seat1", Vector(0.0,0.4,0.0), Vector()))

                vehicle.initialize()

                StructureManager.vehicles[vehicle.uuid] = vehicle
                player.sendSuccess("Custom Vehicle Spawned!")
            }
            "airplane" -> {
                val vehicle = CustomVehicle("airplane", player.location.toVector(), Vector(0.0f,player.location.yaw,0.0f))


            }
        }


        return true
    }

}