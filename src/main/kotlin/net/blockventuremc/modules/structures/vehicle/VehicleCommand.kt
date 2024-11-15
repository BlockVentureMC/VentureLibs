package net.blockventuremc.modules.structures.vehicle

import net.blockventuremc.VentureLibs
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.lerp
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.sendSuccess
import net.blockventuremc.modules.structures.Animation
import net.blockventuremc.modules.structures.Locator
import net.blockventuremc.modules.structures.ItemAttachment
import net.blockventuremc.modules.structures.StructureManager
import net.blockventuremc.modules.structures.deltaTime
import net.blockventuremc.modules.structures.impl.Seat
import net.blockventuremc.modules.structures.vehicle.PacketHandler.removeEntityPacket
import net.blockventuremc.utils.itembuilder.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.PermissionDefault
import org.bukkit.util.Vector
import kotlin.collections.set
import kotlin.math.asin


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
                val wheel1 = vehicle.addChild(Locator("wheel1", Vector(0.55,0.0,1.2), Vector()))
                val wheel2 = vehicle.addChild(Locator("wheel2", Vector(-0.55,0.0,1.2), Vector()))
                val wheel3 = vehicle.addChild(Locator("wheel3", Vector(0.55,0.0,-0.25), Vector()))
                val wheel4 = vehicle.addChild(Locator("wheel4", Vector(-0.55,0.0,-0.25), Vector()))

                val front = vehicle.addChild(Locator("raytestfront", Vector(0.0,0.0,1.2), Vector()))
                val back = vehicle.addChild(Locator("raytestback", Vector(-0.0,0.0,-0.25), Vector()))

                val downVector = Vector(0,-1,0)
                vehicle.animation = object : Animation() {
                    var time = 0.0
                    override fun animate() {
                        time++
                        if(vehicle.armorStand?.isOnGround == false) return

                        val velocity = vehicle.armorStand?.velocity ?: return

                        if(velocity.length() < 0.07) return

                        val frontPosition = Vector(front.lerpPosition.x, vehicle.position.y + 0.75, front.lerpPosition.z).toLocation(vehicle.world)
                        val frontCheck = vehicle.world.rayTraceBlocks(frontPosition, downVector, 1.8, FluidCollisionMode.NEVER, true)
                        val frontOnGround = frontCheck?.hitBlock != null
                        val frontHitPosition = if(frontOnGround) frontCheck.hitPosition else front.lerpPosition

                        val backPosition = Vector(back.lerpPosition.x, vehicle.position.y + 0.75, back.lerpPosition.z).toLocation(vehicle.world)
                        val backCheck = vehicle.world.rayTraceBlocks(backPosition, downVector, 1.8, FluidCollisionMode.NEVER, true)
                        val backOnGround = backCheck?.hitBlock != null
                        val backtHitPosition = if(backOnGround) backCheck.hitPosition else back.lerpPosition

                        if(backtHitPosition.y == frontHitPosition.y) {
                            vehicle.pitch = lerp(vehicle.pitch, 0.0f, deltaTime, 3.4f)
                        } else {
                            val frontDirection = frontHitPosition.clone().subtract(backtHitPosition).normalize()
                            val pitchRadians = asin(-frontDirection.y)
                            vehicle.pitch = lerp(vehicle.pitch, Math.toDegrees(pitchRadians).toFloat(), deltaTime, 3.6f)
                        }
                        if(velocity.length() < 0.17) return
                        val blockData = vehicle.bukkitLocation.add(0.0,-0.5,0.0).block.blockData

                        vehicle.world.spawnParticle(Particle.BLOCK, wheel1.lerpPosition.x, wheel1.lerpPosition.y, wheel1.lerpPosition.z, 10, 0.1, 0.1, 0.1, 1.0, blockData)
                        vehicle.world.spawnParticle(Particle.BLOCK, wheel2.lerpPosition.x, wheel2.lerpPosition.y, wheel2.lerpPosition.z, 10, 0.1, 0.1, 0.1, 1.0, blockData)
                        vehicle.world.spawnParticle(Particle.BLOCK, wheel3.lerpPosition.x, wheel3.lerpPosition.y, wheel3.lerpPosition.z, 10, 0.1, 0.1, 0.1, 1.0, blockData)
                        vehicle.world.spawnParticle(Particle.BLOCK, wheel4.lerpPosition.x, wheel4.lerpPosition.y, wheel4.lerpPosition.z, 10, 0.1, 0.1, 0.1, 1.0, blockData)
                    }
                }

                vehicle.initialize()

                StructureManager.vehicles[vehicle.uuid] = vehicle
                removeEntityPacket(player, vehicle.armorStand!!)
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
                val wheel1 = Locator("wheel1", Vector(0.0,0.0,1.0), Vector())
                val wheel2 = Locator("wheel2", Vector(0.0,0.0,-0.86), Vector())
                val wheel3 = Locator("wheel3", Vector(-1.1,0.0,0.0), Vector())
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
            "jetski" -> {
                val vehicle = WaterVehicle("jetski vehicle", player.location.toVector(), Vector(0.0f,player.location.yaw,0.0f))
                vehicle.world = player.world
                vehicle.owner = player.uniqueId
                vehicle.smoothFactor = 4
                vehicle.addChild(
                    ItemAttachment("jetski", ItemBuilder(Material.LEATHER_HORSE_ARMOR).customModelData(10000).build(), Vector(0.0, 0.2, 0.0), Vector()).setScale(0.8f)
                )
                vehicle.addChild(Seat("seat1", Vector(0.0,0.2,0.0), Vector()))

                //effect
                val back = vehicle.addChild(Locator("back", Vector(0.0,0.0,-0.8), Vector()))
                val left = vehicle.addChild(Locator("left", Vector(-0.7,0.0,0.8), Vector()))
                val right = vehicle.addChild(Locator("right", Vector(0.7,0.0,0.8), Vector()))
                vehicle.animation = object : Animation() {
                    val waterBlockData = Material.WATER.createBlockData()

                    override fun animate() {
                        val velocity = vehicle.armorStand?.velocity ?: return

                        if(vehicle.groundBlock.isSolid) {
                            if(velocity.length() < 0.02) return
                            vehicle.world.spawnParticle(Particle.BLOCK, vehicle.position.x, vehicle.position.y, vehicle.position.z, 10, 0.5, 0.1, 0.5, 1.0, vehicle.groundBlock.blockData)
                        } else {
                            if(velocity.length() < 0.12) return
                            vehicle.world.spawnParticle(
                                Particle.BLOCK,
                                back.lerpPosition.x,
                                back.lerpPosition.y,
                                back.lerpPosition.z,
                                20,
                                0.1,
                                0.29,
                                0.1,
                                1.0,
                                waterBlockData
                            )
                            vehicle.world.spawnParticle(
                                Particle.SPLASH,
                                back.lerpPosition.x,
                                back.lerpPosition.y,
                                back.lerpPosition.z,
                                4,
                                0.24,
                                0.27,
                                0.24
                            )
                            vehicle.world.spawnParticle(
                                Particle.BLOCK,
                                left.lerpPosition.x,
                                left.lerpPosition.y,
                                left.lerpPosition.z,
                                10,
                                0.0,
                                0.3,
                                0.0,
                                1.0,
                                waterBlockData
                            )
                            vehicle.world.spawnParticle(
                                Particle.BLOCK,
                                right.lerpPosition.x,
                                right.lerpPosition.y,
                                right.lerpPosition.z,
                                10,
                                0.0,
                                0.3,
                                0.0,
                                1.0,
                                waterBlockData
                            )
                        }
                    }
                }

                vehicle.initialize()
                removeEntityPacket(player, vehicle.armorStand!!)
                StructureManager.vehicles[vehicle.uuid] = vehicle
                player.sendSuccess("Jetski Vehicle Spawned!")
            }
            "airplane" -> {
                val vehicle = AirplaneVehicle("airplane", player.location.toVector(), Vector(0.0f,player.location.yaw,0.0f))
                vehicle.owner = player.uniqueId
                vehicle.smoothFactor = 7
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

                val middle = vehicle.addChild(Locator("middle", Vector(), Vector()))
                val left = vehicle.addChild(Locator("left", Vector(-2.0,0.0,0.0), Vector()))
                val right = vehicle.addChild(Locator("right", Vector(2.0,0.0,0.0), Vector()))

                vehicle.animation = object : Animation() {

                    override fun animate() {
                        val velocity = vehicle.armorStand?.velocity ?: return

                            if (velocity.length() < 0.01) return
                            vehicle.world.spawnParticle(
                                Particle.FIREWORK,
                                left.lerpPosition.x,
                                left.lerpPosition.y,
                                left.lerpPosition.z,
                                1,
                                0.0,
                                0.0,
                                0.0,
                                0.0
                            )
                            vehicle.world.spawnParticle(
                                Particle.FIREWORK,
                                right.lerpPosition.x,
                                right.lerpPosition.y,
                                right.lerpPosition.z,
                                1,
                                0.0,
                                0.0,
                                0.0,
                                0.0
                            )
                        val onGround = vehicle.groundCollision?.hitBlock?: return
                            vehicle.world.spawnParticle(
                                Particle.BLOCK,
                                middle.lerpPosition.x,
                                vehicle.groundCollision!!.hitPosition.y + 0.4,
                                middle.lerpPosition.z,
                                14,
                                1.2,
                                0.3,
                                1.2,
                                1.0,
                                vehicle.groundCollision!!.hitBlock!!.blockData
                            )

                    }
                }

                val shootUp = Vector(0.0,0.1,0.0)
                val speed = 2.3
                vehicle.steeringTask = { player, packet ->
                    if(packet.isJumping) {
                        Bukkit.getScheduler().runTask(VentureLibs.instance, Runnable {

                            val targetPosition = player.eyeLocation.toVector().add(player.eyeLocation.direction.multiply(40))

                            val leftDirection = targetPosition.clone().subtract(left.lerpPosition).normalize().multiply(speed)
                            val rightDirection = targetPosition.clone().subtract(right.lerpPosition).normalize().multiply(speed)

                            val snowballLeft = player.world.spawn(
                                left.lerpPosition.toLocation(vehicle.world), Snowball::class.java
                            ).apply {
                                setGravity(false)
                                velocity = leftDirection
                                item = ItemStack(Material.KELP)
                            }
                            val snowballRight = player.world.spawn(
                                right.lerpPosition.toLocation(vehicle.world), Snowball::class.java
                            ).apply {
                                setGravity(false)
                                velocity = rightDirection
                                item = ItemStack(Material.KELP)
                            }
                        })
                    }
                }

                seat1.smoothCoaster = false
                vehicle.addChild(seat1)
                vehicle.initialize()
                removeEntityPacket(player, vehicle.armorStand!!)
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
            1 -> listOf("cart", "airplane", "roller", "jetski").filter { it.startsWith(args[0]) }
            else -> emptyList()
        }
    }



}