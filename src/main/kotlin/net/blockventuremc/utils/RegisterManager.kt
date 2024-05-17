package net.blockventuremc.utils

import dev.kord.common.Locale
import dev.kord.common.entity.Permissions
import dev.kord.core.Kord
import io.sentry.Sentry
import net.blockventuremc.VentureLibs
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.consts.NAMESPACE_PLUGIN
import net.blockventuremc.extensions.code
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.modules.discord.model.AbstractCommand
import net.blockventuremc.modules.discord.model.AbstractEvent
import net.blockventuremc.modules.i18n.TranslationCache
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.PluginCommand
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.bukkit.plugin.Plugin
import com.google.common.reflect.ClassPath
import net.blockventuremc.extensions.getLogger
import kotlin.math.log
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor



object RegisterManager {
    val dcCommands = mutableListOf<AbstractCommand>()
    private val logger = getLogger()

    private fun <T : Any> loadClassesInPackage(packageName: String, clazzType: KClass<T>): List<KClass<out T>> {
        try {
            val classLoader = VentureLibs.instance.javaClass.classLoader
            val allClasses = ClassPath.from(classLoader).allClasses
            val classes = mutableListOf<KClass<out T>>()
            for (classInfo in allClasses) {
                if (!classInfo.name.startsWith("net.blockventuremc.modules")) continue
                if (classInfo.packageName.startsWith(packageName) && !classInfo.name.contains('$')) {
                    try {
                        val loadedClass = classInfo.load().kotlin
                        if (clazzType.isInstance(loadedClass.javaObjectType.getDeclaredConstructor().newInstance())) {
                            classes.add(loadedClass as KClass<out T>)
                        }
                    } catch (_: Exception) {
                        // Ignore
                    }
                }
            }
            return classes
        } catch (e: Exception) {
            logger.error("Failed to load classes: ${e.message}")
            return emptyList()
        }
    }


    private suspend fun registerDiscordCommands(kord: Kord, pkg: String) {
        val commandClasses = loadClassesInPackage(pkg, AbstractCommand::class)

        commandClasses.forEach {
            val command = it.primaryConstructor?.call() as AbstractCommand

            val desc = TranslationCache.get(
                Locale.ENGLISH_UNITED_STATES.code,
                "discord.commands.${command.name}.description"
            )

            kord.createGlobalChatInputCommand(
                command.name,
                desc?.message ?: "No description"
            ) {
                if (command.permission != null) {
                    defaultMemberPermissions = Permissions(command.permission!!)
                }

                command.options.invoke(this)

                val deDesc =
                    TranslationCache.get(Locale.GERMAN.code, "discord.commands.${command.name}.description")

                if (deDesc != null) {
                    description(Locale.GERMAN, deDesc.message)
                }
            }

            dcCommands.add(command)
        }

        logger.info("Registered ${dcCommands.size} discord commands")


    }

    private suspend fun registerDiscordListeners(kord: Kord, pkg: String) {
        val eventClasses = loadClassesInPackage(pkg, AbstractEvent::class)

        eventClasses.forEach {
            val event = it.primaryConstructor?.call() as AbstractEvent
            event.execute(kord)
        }

        logger.info("Registered ${eventClasses.size} discord events")
    }

    private fun registerCommands(pkg: String) {
        val commandClasses = loadClassesInPackage(pkg, CommandExecutor::class)

        commandClasses.forEach {
            val annotation: VentureCommand = it.annotations.first { it is VentureCommand } as VentureCommand

            val pluginClass: Class<PluginCommand> = PluginCommand::class.java

            val constructor = pluginClass.getDeclaredConstructor(String::class.java, Plugin::class.java)

            constructor.isAccessible = true

            val command: PluginCommand = constructor.newInstance(annotation.name, VentureLibs.instance)


            command.aliases = annotation.aliases.toList()
            command.description = annotation.description
            command.permission = Permission(annotation.permission, annotation.permissionDefault).name
            command.usage = annotation.usage
            val commandInstance = it.primaryConstructor?.call() as CommandExecutor
            command.setExecutor { sender, command, label, args ->
                try {
                    commandInstance.onCommand(sender, command, label, args)
                } catch (e: Exception) {
                    sender.sendMessagePrefixed("An error occurred while executing the command.")
                    Sentry.captureException(e)
                    throw e
                }
            }
            command.tabCompleter = commandInstance as? org.bukkit.command.TabCompleter


            Bukkit.getCommandMap().register(NAMESPACE_PLUGIN, command)
            Bukkit.getConsoleSender().sendMessage("Command ${command.name} registered")
        }

        logger.info("Registered ${commandClasses.size} minecraft commands")
    }

    private fun registerListeners(pkg: String) {
        val listenerClasses = loadClassesInPackage(pkg, Listener::class)
        var amountListeners = 0
        listenerClasses.forEach {
            val listener = it.primaryConstructor?.call() as Listener
            Bukkit.getPluginManager().registerEvents(listener, VentureLibs.instance)
            amountListeners++
        }
        logger.info("Registered $amountListeners listeners")
    }


    fun registerMC() {
        val reflections = "net.blockventuremc.modules"

        registerListeners(reflections)
        registerCommands(reflections)
    }

    suspend fun registerDiscord(kord: Kord) {
        val reflections = "net.blockventuremc.modules.discord"

        registerDiscordCommands(kord, reflections)

        registerDiscordListeners(kord, reflections)
    }
}