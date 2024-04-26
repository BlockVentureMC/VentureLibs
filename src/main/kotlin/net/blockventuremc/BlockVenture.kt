package net.blockventuremc

import io.github.cdimascio.dotenv.dotenv
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.database.DatabaseManager
import net.blockventuremc.modules.i18n.TranslationCache
import net.blockventuremc.modules.placeholders.PlayerPlaceholderManager
import net.blockventuremc.utils.RegisterManager.registerAll
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class BlockVenture : JavaPlugin() {
    companion object {
        lateinit var instance: BlockVenture
    }

    val dotenv = dotenv()

    init {
        instance = this
    }

    override fun onLoad() {
        server.spigot().spigotConfig["messages.unknown-command"] = "§c" + "Unknown Command"
        server.spigot().spigotConfig["messages.server-full"] = "${"server full"} - Club Members can join at any time"
        server.spigot().spigotConfig["messages.outdated-client"] =
            "Your client is outdated, please use the latest version of Minecraft"
        server.spigot().spigotConfig["messages.outdated-server"] =
            "Hold on! We are not that fast. We upgrade as soon as we can"
    }

    override fun onEnable() {
        logger.info("Loading database...")
        DatabaseManager.database

        DatabaseManager.register()

        logger.info("Loading translations...")
        TranslationCache.loadAll()

        logger.info("Registering placeholders...")
        PlayerPlaceholderManager()


        logger.info("Registering modules...")
        registerAll()

        PlayerCache.runOnlineTimeScheduler()

        logger.info("Hello, Minecraft!")
    }

    override fun onDisable() {
        PlayerCache.cleanup()

        for (player in Bukkit.getOnlinePlayers()) {
            val pixelPlayer = PlayerCache.getOrNull(player.uniqueId) ?: continue
            PlayerCache.saveToDB(pixelPlayer.copy(username = player.name))
        }

        logger.info("Plugin has been disabled")
    }
}