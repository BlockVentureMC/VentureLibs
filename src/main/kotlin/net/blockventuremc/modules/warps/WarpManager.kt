package net.blockventuremc.modules.warps

import dev.fruxz.stacked.extension.asPlainString
import io.sentry.Sentry
import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.translate
import net.blockventuremc.modules.general.events.custom.*
import net.blockventuremc.modules.general.model.Ranks
import net.blockventuremc.utils.FileConfig
import org.bukkit.Bukkit
import org.bukkit.WorldCreator

object WarpManager {

    const val WARP_CONFIG_FILE = "warps.yml"

    /**
     * This variable `warps` is a mutable map that stores instances of the `Warp` class.
     * It represents a collection of location warps that players can teleport to.
     *
     * @property warps The mutable map that holds the warp name as the key and the corresponding `Warp` instance as the value.
     * @see Warp
     */
    private val warps = mutableMapOf<String, Warp>()

    init {
        reloadWarps()
    }

    /**
     * Reloads the list of warps from the "warps.yml" configuration file.
     * Existing warps will be cleared before loading new ones.
     * Errors encountered during the loading process will be logged and captured using Sentry.
     */
    fun reloadWarps() {
        warps.clear()

        // Load warps from config
        val warpConfig = FileConfig(WARP_CONFIG_FILE)

        for (warpKey in warpConfig.getKeys(false)) {
            try {
                val warpSection =
                    warpConfig.getConfigurationSection(warpKey) ?: error("Invalid warp section for $warpKey")

                if (warpSection.contains("old_location")) {
                    migrateLocation(warpConfig, warpKey)
                }


                val locationString = warpSection.getString("location") ?: error("Invalid location for warp $warpKey")
                val ventureLocation = convertToBlockLocation(locationString)
                val world = Bukkit.getWorld(ventureLocation.world)

                if (world == null) {
                    Bukkit.createWorld(WorldCreator(ventureLocation.world))
                    getLogger().info("World ${ventureLocation.world} created successfully")
                }

                Bukkit.getScheduler().runTaskLater(VentureLibs.instance, Runnable {
                    val warp = Warp(
                        warpKey,
                        ventureLocation.toBukkitLocation(),
                        warpSection.getString("rankNeeded")?.let { Ranks.valueOf(it) } ?: Ranks.TEAM
                    )
                    warps[warpKey] = warp
                }, 1L)
            } catch (exception: Exception) {
                // Log error
                exception.printStackTrace()
                Sentry.captureException(exception)
            }
        }

        Bukkit.getScheduler().runTaskLater(VentureLibs.instance, Runnable {
            getLogger().info("Warps reloaded. ${warps.size} warps loaded.")
        }, 10L)
    }

    private fun migrateLocation(warpConfig: FileConfig, warpKey: String) {
        val warpSection =
            warpConfig.getConfigurationSection(warpKey) ?: error("Invalid warp section for $warpKey")

        val oldWorld = warpSection.getString("old_location.world") ?: error("Invalid old location for warp $warpKey")
        val oldX = warpSection.getDouble("old_location.x")
        val oldY = warpSection.getDouble("old_location.y")
        val oldZ = warpSection.getDouble("old_location.z")
        val oldYaw = warpSection.getDouble("old_location.yaw").toFloat()
        val oldPitch = warpSection.getDouble("old_location.pitch").toFloat()

        val ventureLocation = VentureLocation(
            x = oldX,
            y = oldY,
            z = oldZ,
            yaw = oldYaw,
            pitch = oldPitch,
            world = oldWorld,
            server = Bukkit.getServer().motd().asPlainString
        )

        warpConfig.set("${warpKey}.location", ventureLocation.toSimpleString())
        warpConfig.set("${warpKey}.old_location", null)
        warpConfig.saveConfig()

        getLogger().info("Warp $warpKey migrated.")
    }

    /**
     * Retrieves the Warp with the specified name.
     *
     * @param name the name of the Warp to retrieve
     * @return the Warp object with the specified name, or null if no Warp is found
     */
    fun getWarp(name: String): Warp? {
        return warps[name]
    }

    /**
     * Retrieves all available warps.
     *
     * @return a collection of Warp objects representing the available warps.
     *
     * @see Warp
     */
    fun getWarps(): Collection<Warp> {
        return warps.values
    }

    /**
     * Adds a warp to the warps collection and saves it to the config file.
     *
     * @param warp the warp to add
     * @return true if the warp was added and saved successfully, false otherwise
     */
    fun addWarp(warp: Warp): Boolean {
        warps[warp.name] = warp

        // Save warp to config
        try {
            val warpConfig = FileConfig(WARP_CONFIG_FILE)
            warpConfig.set(warp.name, null)
            warpConfig.set("${warp.name}.location", warp.location.toVentureLocation().toSimpleString())
            warpConfig.set("${warp.name}.rankNeeded", warp.rankNeeded.name)
            warpConfig.saveConfig()
            getLogger().info("Warp ${warp.name} added.")
            return true
        } catch (exception: Exception) {
            // Log error
            exception.printStackTrace()
            Sentry.captureException(exception)
            return false
        }
    }

    /**
     * Removes a warp from the warps collection and saves the updated list to the config file.
     *
     * @param name the name of the warp to remove
     * @return true if the warp was removed and saved successfully, false otherwise
     */
    fun removeWarp(name: String): Boolean {
        warps.remove(name)

        // Save updated warps to config
        try {
            val warpConfig = FileConfig(WARP_CONFIG_FILE)
            warpConfig.set(name, null)
            warpConfig.saveConfig()
            getLogger().info("Warp $name removed.")
            return true
        } catch (exception: Exception) {
            // Log error
            exception.printStackTrace()
            Sentry.captureException(exception)
            return false
        }
    }
}