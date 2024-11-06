package net.blockventuremc.modules.games

import net.blockventuremc.extensions.sendSuccess
import net.blockventuremc.modules.structures.Animation
import net.blockventuremc.modules.structures.EffectAttachment
import net.blockventuremc.modules.structures.ItemAttachment
import net.blockventuremc.modules.structures.StructureManager
import net.blockventuremc.modules.structures.impl.Seat
import net.blockventuremc.modules.structures.vehicle.WaterVehicle
import net.blockventuremc.utils.itembuilder.ItemBuilder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.entity.Vehicle
import org.bukkit.util.Vector
import kotlin.collections.set

class JetskiGame: Game(GameData("Jetski", "This is a jetski game", 8, 2)) {

    var jetskis = mutableListOf<WaterVehicle>()

    override fun start() {
        super.start()

        //spawn all jetskis

    }


    fun cleanUpJetskis() {
        jetskis.forEach { jetski ->
            jetski.despawnAttachmentsRecurse()
            StructureManager.vehicles.remove(jetski.uuid)
        }
        jetskis.clear()
    }

    fun spawnJetski(location: Location, player: Player) {
        player.teleport(location)
        val vehicle = WaterVehicle("jetski vehicle", location.toVector(), Vector(0.0f,location.yaw,0.0f))
        vehicle.world = player.world
        vehicle.owner = player.uniqueId
        vehicle.addChild(
            ItemAttachment("jetski", ItemBuilder(Material.LEATHER_HORSE_ARMOR).customModelData(10000).build(), Vector(0.0, 0.2, 0.0), Vector()).setScale(0.8f)
        )
        vehicle.addChild(Seat("seat1", Vector(0.0,0.2,0.0), Vector()))
        //effect
        val back = EffectAttachment("back", Vector(0.0,0.0,-0.8), Vector())
        val left = EffectAttachment("left", Vector(-0.7,0.0,0.8), Vector())
        val right = EffectAttachment("right", Vector(0.7,0.0,0.8), Vector())
        vehicle.addChild(back)
        vehicle.addChild(left)
        vehicle.addChild(right)
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
        StructureManager.vehicles[vehicle.uuid] = vehicle
        jetskis.add(vehicle)
        player.sendSuccess("Jetski Vehicle Spawned!")
    }

}