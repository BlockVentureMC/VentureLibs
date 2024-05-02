package net.blockventuremc.modules.general.events.custom

import dev.fruxz.stacked.extension.asPlainString
import org.bukkit.Bukkit
import org.bukkit.Location

data class VentureLocation(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val world: String,
    val server: String,
)

fun VentureLocation.toSimpleString() = convertFromBlockLocation(this)
fun String.toVentureLocation() = convertToBlockLocation(this)

fun Location.toVentureLocation() = VentureLocation(
    x = x,
    y = y,
    z = z,
    yaw = yaw,
    pitch = pitch,
    world = world.name,
    server = Bukkit.getServer().motd().asPlainString,
)

fun VentureLocation.toBukkitLocation() = Location(
    Bukkit.getWorld(world),
    x,
    y,
    z,
    yaw,
    pitch,
)

fun convertFromBlockLocation(location: VentureLocation) =
    "${location.x},${location.y},${location.z},${location.yaw},${location.pitch},${location.world},${location.server}"

fun convertToBlockLocation(string: String): VentureLocation {
    val split = string.split(",")
    return VentureLocation(
        x = split[0].toDouble(),
        y = split[1].toDouble(),
        z = split[2].toDouble(),
        yaw = split[3].toFloat(),
        pitch = split[4].toFloat(),
        world = split[5],
        server = split[6],
    )
}