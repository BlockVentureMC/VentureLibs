package net.blockventuremc

import io.github.cdimascio.dotenv.dotenv
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.database.DatabaseManager
import net.blockventuremc.modules.i18n.TranslationCache
import net.blockventuremc.utils.RegisterManager.registerAll
import org.bukkit.plugin.java.JavaPlugin

class Plugin: JavaPlugin() {
    companion object {
        lateinit var instance: Plugin
    }

    val dotenv = dotenv()

    init {
        instance = this
    }

    override fun onEnable() {
        logger.info("Loading database...")
        DatabaseManager.database

        DatabaseManager.register()

        logger.info("Loading translations...")
        TranslationCache.loadAll()

        logger.info("Registering modules...")
        registerAll()

        PlayerCache.runOnlineTimeScheduler()

        logger.info("Hello, Minecraft!")
    }
}