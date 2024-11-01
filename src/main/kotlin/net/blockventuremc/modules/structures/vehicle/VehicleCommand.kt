package net.blockventuremc.modules.structures.vehicle

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.sendSuccess
import net.blockventuremc.modules.structures.Animation
import net.blockventuremc.modules.structures.Attachment
import net.blockventuremc.modules.structures.EffectAttachment
import net.blockventuremc.modules.structures.ItemAttachment
import net.blockventuremc.modules.structures.RootAttachment
import net.blockventuremc.modules.structures.StructureManager
import net.blockventuremc.modules.structures.impl.Seat
import net.blockventuremc.utils.itembuilder.ItemBuilder
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
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
class VehicleCommand : CommandExecutor, TabExecutor {



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
                vehicle.owner = player.uniqueId
                vehicle.addChild(
                    ItemAttachment(
                        "model",
                        ItemBuilder(Material.DIAMOND_SWORD).customModelData(98).build(),
                        Vector(0.0, 0.3, 0.0),
                        Vector()
                    )
                )
                vehicle.addChild(Seat("seat1", Vector(0.0,0.4,0.0), Vector()))

                //effect
                val wheel1 = EffectAttachment("wheel1", Vector(0.55,0.0,1.2), Vector())
                val wheel2 = EffectAttachment("wheel2", Vector(-0.55,0.0,1.2), Vector())
                val wheel3 = EffectAttachment("wheel3", Vector(0.55,0.0,-0.25), Vector())
                val wheel4 = EffectAttachment("wheel4", Vector(-0.55,0.0,-0.25), Vector())
                vehicle.addChild(wheel1)
                vehicle.addChild(wheel2)
                vehicle.addChild(wheel3)
                vehicle.addChild(wheel4)
                vehicle.animation = object : Animation() {
                    var time = 0.0
                    override fun animate() {
                        time++
                        if(vehicle.armorStand?.isOnGround == false) return

                        val velocity = vehicle.armorStand?.velocity ?: return

                        if(velocity.length() < 0.15) return

                        val blockData = vehicle.bukkitLocation.add(0.0,-0.5,0.0).block.blockData

                        vehicle.world.spawnParticle(Particle.BLOCK, wheel1.lerpPosition.x, wheel1.lerpPosition.y, wheel1.lerpPosition.z, 10, 0.1, 0.1, 0.1, 1.0, blockData)
                        vehicle.world.spawnParticle(Particle.BLOCK, wheel2.lerpPosition.x, wheel2.lerpPosition.y, wheel2.lerpPosition.z, 10, 0.1, 0.1, 0.1, 1.0, blockData)
                        vehicle.world.spawnParticle(Particle.BLOCK, wheel3.lerpPosition.x, wheel3.lerpPosition.y, wheel3.lerpPosition.z, 10, 0.1, 0.1, 0.1, 1.0, blockData)
                        vehicle.world.spawnParticle(Particle.BLOCK, wheel4.lerpPosition.x, wheel4.lerpPosition.y, wheel4.lerpPosition.z, 10, 0.1, 0.1, 0.1, 1.0, blockData)

                    }
                }

                vehicle.initialize()

                StructureManager.vehicles[vehicle.uuid] = vehicle
                player.sendSuccess("Custom Vehicle Spawned!")
            }
            "roller" -> {
                val vehicle = CustomVehicle("roller vehicle", player.location.toVector(), Vector(0.0f,player.location.yaw,0.0f))
                vehicle.world = player.world
                vehicle.owner = player.uniqueId
                vehicle.addChild(
                    ItemAttachment(
                        "model",
                        ItemBuilder(Material.DIAMOND_SWORD).customModelData(145).build(),
                        Vector(0.0, 1.04, 0.0),
                        Vector()
                    )
                )
                vehicle.addChild(Seat("seat1", Vector(0.0,1.1,0.0), Vector()))
                vehicle.addChild(Seat("seat2", Vector(-0.7,0.4,-0.1), Vector()))

                //effect
                val wheel1 = EffectAttachment("wheel1", Vector(0.0,0.0,1.0), Vector())
                val wheel2 = EffectAttachment("wheel2", Vector(0.0,0.0,-0.86), Vector())
                val wheel3 = EffectAttachment("wheel3", Vector(-1.1,0.0,0.0), Vector())
                vehicle.addChild(wheel1)
                vehicle.addChild(wheel2)
                vehicle.addChild(wheel3)
                vehicle.animation = object : Animation() {
                    var time = 0.0
                    override fun animate() {
                        time++
                        if(vehicle.armorStand?.isOnGround == false) return

                        val velocity = vehicle.armorStand?.velocity ?: return

                        if(velocity.length() < 0.15) return

                        val blockData = vehicle.bukkitLocation.add(0.0,-0.5,0.0).block.blockData

                        vehicle.world.spawnParticle(Particle.BLOCK, wheel1.lerpPosition.x, wheel1.lerpPosition.y, wheel1.lerpPosition.z, 10, 0.1, 0.1, 0.1, 1.0, blockData)
                        vehicle.world.spawnParticle(Particle.BLOCK, wheel2.lerpPosition.x, wheel2.lerpPosition.y, wheel2.lerpPosition.z, 10, 0.1, 0.1, 0.1, 1.0, blockData)
                        vehicle.world.spawnParticle(Particle.BLOCK, wheel3.lerpPosition.x, wheel3.lerpPosition.y, wheel3.lerpPosition.z, 10, 0.1, 0.1, 0.1, 1.0, blockData)

                    }
                }

                vehicle.initialize()

                StructureManager.vehicles[vehicle.uuid] = vehicle
                player.sendSuccess("Custom Vehicle Spawned!")
            }
            "airplane" -> {
                val vehicle = AirplaneVehicle("airplane", player.location.toVector(), Vector(0.0f,player.location.yaw,0.0f))
                vehicle.owner = player.uniqueId
                vehicle.smoothFactor = 8
                vehicle.world = player.world
                vehicle.addChild(
                    ItemAttachment(
                        "model",
                        ItemBuilder(Material.DIAMOND_SWORD).customModelData(153).build(),
                        Vector(0.0, 0.0, 0.0),
                        Vector()
                    )
                )
                val seat1 = Seat("seat1", Vector(0.0,0.2,0.0), Vector())
                seat1.smoothCoaster = false
                vehicle.addChild(seat1)
                vehicle.initialize()

                StructureManager.vehicles[vehicle.uuid] = vehicle
                player.sendSuccess("Custom Vehicle Spawned!")
            }
        }


        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("cart", "airplane", "roller").filter { it.startsWith(args[0]) }
            else -> emptyList()
        }
    }



}