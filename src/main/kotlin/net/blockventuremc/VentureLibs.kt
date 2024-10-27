package net.blockventuremc

import me.m56738.smoothcoasters.api.DefaultNetworkInterface
import me.m56738.smoothcoasters.api.NetworkInterface
import me.m56738.smoothcoasters.api.SmoothCoastersAPI
import net.blockventuremc.audioserver.common.data.RabbitConfiguration
import net.blockventuremc.audioserver.minecraft.AudioServer
import net.blockventuremc.cache.BoosterCache
import net.blockventuremc.cache.PlayerCache
import net.blockventuremc.database.DatabaseManager
import net.blockventuremc.modules.i18n.TranslationCache
import net.blockventuremc.modules.rides.track.TrackManager
import net.blockventuremc.modules.structures.StructureManager
import net.blockventuremc.modules.warps.WarpManager
import net.blockventuremc.utils.Environment
import net.blockventuremc.utils.RegisterManager.registerAll
import net.blockventuremc.utils.RegisterManager.registerCommands
import net.blockventuremc.utils.RegisterManager.registerMC
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class VentureLibs : JavaPlugin() {
    companion object {
        lateinit var instance: VentureLibs
    }

    lateinit var jda: JDA
    lateinit var smoothCoastersAPI: SmoothCoastersAPI
    lateinit var networkInterface:  NetworkInterface

    val shutDownHooks = mutableListOf<() -> Unit>()

    init {
        instance = this
    }

    override fun onLoad() {
        server.spigot().spigotConfig["messages.unknown-command"] = "§c" + "Unknown Command"
        server.spigot().spigotConfig["messages.server-full"] = "server full - Club Members can join at any time"
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

        smoothCoastersAPI = SmoothCoastersAPI(this)
        networkInterface = DefaultNetworkInterface(this)

        logger.info("Connecting to audioserver...")
        AudioServer.connect(
            RabbitConfiguration(
                Environment.getEnv("RABBITMQ_HOST") ?: "localhost",
                Environment.getEnv("RABBITMQ_PORT")?.toInt() ?: 5672,
                Environment.getEnv("RABBITMQ_VHOST") ?: "/",
                Environment.getEnv("RABBITMQ_USER") ?: "guest",
                Environment.getEnv("RABBITMQ_PASSWORD") ?: "guest"
            )
        )

        logger.info("Registering placeholders...")

        PlayerCache.runOnlineTimeScheduler()

        logger.info("Registering modules...")
        registerMC()

        BoosterCache.load()

        logger.info("Preloading warps...")
        WarpManager

        logger.info("Starting bot")
        jda = JDABuilder.createDefault(Environment.getEnv("BOT_TOKEN") ?: error("No token provided"))
            .registerAll()
            .build()
            .awaitReady()
            .also {
                it.presence.setPresence(
                    OnlineStatus.IDLE, Activity.customStatus("☕ Starting up...")
                )
            }
            .registerCommands()

        TrackManager.loadTracks()

        logger.info("Custom Entity update...")
        StructureManager.update()


        logger.info("Plugin has been enabled.")
    }

    override fun onDisable() {
        PlayerCache.cleanup()
        TrackManager.cleanUp()

        for (player in Bukkit.getOnlinePlayers()) {
            val pixelPlayer = PlayerCache.getOrNull(player.uniqueId) ?: continue
            PlayerCache.saveToDB(pixelPlayer.copy(username = player.name))
        }

        shutDownHooks.forEach { it() }

        smoothCoastersAPI.unregister()

        AudioServer.disconnect()
        StructureManager.cleanup()

        jda.shutdown()

        logger.info("Plugin has been disabled")
    }
}