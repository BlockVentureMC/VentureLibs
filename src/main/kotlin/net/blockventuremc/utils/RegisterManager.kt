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
import kotlin.time.measureTime
import com.google.common.reflect.ClassPath
import dev.kord.core.event.Event
import net.blockventuremc.extensions.getLogger
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor



object RegisterManager {
    val dcCommands = mutableListOf<AbstractCommand>()
    private val logger = getLogger()

    private suspend fun registerDiscordCommands(kord: Kord, pkg: String) {

        val timeDiscordCommands = measureTime {
            val reflections = ClassPath.from(Thread.currentThread().contextClassLoader).getTopLevelClasses(pkg)
            for (clazz in reflections) {
                val kclass = Class.forName(clazz.name).kotlin
                if (kclass.isSubclassOf(AbstractCommand::class)) {
                    val command = kclass.primaryConstructor?.call() as AbstractCommand

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

                    logger.info("Registered command ${command.name}")
                }
            }
        }
    }

    private suspend fun registerDiscordListeners(kord: Kord, pkg: String) {
        val timeDiscordEvents = measureTime {
            val reflections = ClassPath.from(Thread.currentThread().contextClassLoader).getTopLevelClasses(pkg)
            for (clazz in reflections) {
                val kclass = Class.forName(clazz.name).kotlin
                if (kclass.isSubclassOf(AbstractEvent::class)) {
                    val event = kclass.primaryConstructor?.call() as AbstractEvent

                    event.execute(kord)

                    logger.info("Registered event ${event.javaClass.simpleName}")
                }
            }
        }
    }

    private fun registerCommands(pkg: String) {
        val reflections = ClassPath.from(Thread.currentThread().contextClassLoader).getTopLevelClasses(pkg)
        var amountCommands = 0
        for (clazz in reflections) {
            val kclass = Class.forName(clazz.name).kotlin
            if (kclass.isSubclassOf(CommandExecutor::class)) {

                val annotation: VentureCommand = kclass.annotations.first { it is VentureCommand } as VentureCommand

                val pluginClass: Class<PluginCommand> = PluginCommand::class.java

                val constructor = pluginClass.getDeclaredConstructor(String::class.java, Plugin::class.java)

                constructor.isAccessible = true

                val command: PluginCommand = constructor.newInstance(annotation.name, VentureLibs.instance)


                command.aliases = annotation.aliases.toList()
                command.description = annotation.description
                command.permission = Permission(annotation.permission, annotation.permissionDefault).name
                command.usage = annotation.usage
                val commandInstance = kclass.primaryConstructor?.call() as CommandExecutor
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
                amountCommands++
            }
        }

        logger.info("Registered $amountCommands commands")
    }

    private fun registerListeners(pkg: String) {
        val reflections = ClassPath.from(Thread.currentThread().contextClassLoader).getTopLevelClasses(pkg)
        var amountListeners = 0
        for (clazz in reflections) {
            val kclass = Class.forName(clazz.name).kotlin
            if (kclass.isSubclassOf(Listener::class)) {
                val listener = kclass.primaryConstructor?.call() as Listener
                Bukkit.getPluginManager().registerEvents(listener, VentureLibs.instance)
                amountListeners++
            }
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