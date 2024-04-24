package net.blockventuremc.utils

import dev.kord.common.Locale

import dev.kord.core.Kord
import dev.kord.rest.builder.interaction.*
import io.sentry.Sentry
import net.blockventuremc.BlockVenture
import net.blockventuremc.annotations.BlockCommand
import net.blockventuremc.consts.NAMESPACE_PLUGIN
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.modules.discord.model.AbstractCommand
import net.blockventuremc.modules.i18n.TranslationCache
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.PluginCommand
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.bukkit.plugin.Plugin
import org.reflections8.Reflections
import kotlin.time.measureTime

fun OptionsBuilder.translate() {

    val enUs = TranslationCache.get(Locale.ENGLISH_UNITED_STATES.language, "discord.options.${name}")
    if (enUs != null) {
        name(Locale.ENGLISH_UNITED_STATES, enUs.message)
    }

    val de = TranslationCache.get(Locale.GERMAN.language, "discord.options.${name}")

    if (de != null) {
        name(Locale.GERMAN, de.message)
    }


}

/**
 *
 *                     kord.createGlobalChatInputCommand(
 *                         "sum",
 *                         "A slash command that sums two numbers"
 *                     ) {
 *
 *                         integer("second", "The second number to sum") {
 *                             required = true
 *
 *                             translate()
 *                         }
 *
 *                         string("first", "The first number to sum") {
 *                             required = true
 *
 *                             translate()
 *                         }
 *
 *
 *                         if (TranslationCache.get(Locale.ENGLISH_UNITED_STATES.language, "de") != null) {
 *                             name(Locale.ENGLISH_GREAT_BRITAIN, TranslationCache.get(Locale.ENGLISH_UNITED_STATES.language, "de")!!.message)
 *                         }
 *
 *                         if (TranslationCache.get(Locale.GERMAN.language, "de") != null) {
 *                             name(Locale.GERMAN, TranslationCache.get(Locale.GERMAN.language, "de")!!.message)
 *                         }
 *                     }
 */

object RegisterManager {

    val dcCommands = mutableListOf<AbstractCommand>()

    private suspend fun registerDiscordCommands(kord: Kord) {

        val reflections = Reflections("net.blockventuremc.modules.discord")

        val timeDiscordCommands = measureTime {
            for (clazz in reflections.getSubTypesOf(AbstractCommand::class.java)) {
                try {
                    val constructor = clazz.declaredConstructors.find { it.parameterCount == 0 } ?: continue

                    constructor.isAccessible = true

                    val command = constructor.newInstance() as AbstractCommand

                    val cmd = kord.createGlobalChatInputCommand(
                        command.name,
                        command.description
                    ) {

                        command.options.invoke(this)

                        val enUS = TranslationCache.get(Locale.ENGLISH_UNITED_STATES.language, "discord.commands.${command.name}")
                        if (enUS != null) {
                            name(Locale.ENGLISH_UNITED_STATES, enUS.message)
                        }

                        val de = TranslationCache.get(Locale.GERMAN.language, "discord.commands.${command.name}")
                        if (de != null) {
                            name(Locale.GERMAN, de.message)
                        }

                        val enUSDesc = TranslationCache.get(Locale.ENGLISH_UNITED_STATES.language, "discord.commands.${command.name}.description")

                        if (enUSDesc != null) {
                            description(Locale.ENGLISH_UNITED_STATES, enUSDesc.message)
                        }

                        val deDesc = TranslationCache.get(Locale.GERMAN.language, "discord.commands.${command.name}.description")

                        if (deDesc != null) {
                            description(Locale.GERMAN, deDesc.message)
                        }
                    }

                    dcCommands.add(command)

                    println("Command ${command.name} registered")

                } catch (exception: InstantiationError) {
                    exception.printStackTrace()
                    Sentry.captureException(exception)
                } catch (exception: IllegalAccessException) {
                    exception.printStackTrace()
                    Sentry.captureException(exception)
                }
            }
        }

        println("Registered discord commands in $timeDiscordCommands")
    }
    private fun registerCommands(reflections: Reflections) {

        val timeCommands = measureTime {
            for (clazz in reflections.getTypesAnnotatedWith(BlockCommand::class.java)) {
                try {
                    val annotation: BlockCommand = clazz.getAnnotation(BlockCommand::class.java)

                    val pluginClass: Class<PluginCommand> = PluginCommand::class.java
                    val constructor = pluginClass.getDeclaredConstructor(String::class.java, Plugin::class.java)

                    constructor.isAccessible = true

                    val command: PluginCommand = constructor.newInstance(annotation.name, BlockVenture.instance)

                    command.aliases = annotation.aliases.toList()
                    command.description = annotation.description
                    command.permission = Permission(annotation.permission, annotation.permissionDefault).name
                    command.usage = annotation.usage
                    val commandInstance = clazz.getDeclaredConstructor().newInstance() as CommandExecutor
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
                } catch (exception: InstantiationError) {
                    exception.printStackTrace()
                } catch (exception: IllegalAccessException) {
                    exception.printStackTrace()
                }
            }
        }
        println("Registered commands in $timeCommands")
    }

    private fun registerListeners(reflections: Reflections) {
        val timeListeners = measureTime {
            for (clazz in reflections.getSubTypesOf(Listener::class.java)) {
                try {
                    val constructor = clazz.declaredConstructors.find { it.parameterCount == 0 } ?: continue

                    if (clazz.`package`.name.contains("conversations")) continue

                    constructor.isAccessible = true

                    val event = constructor.newInstance() as Listener

                    Bukkit.getPluginManager().registerEvents(event, BlockVenture.instance)
                    Bukkit.getConsoleSender()
                        .sendMessage("Listener ${event.javaClass.simpleName} registered")
                } catch (exception: InstantiationError) {
                    exception.printStackTrace()
                    Sentry.captureException(exception)
                } catch (exception: IllegalAccessException) {
                    exception.printStackTrace()
                    Sentry.captureException(exception)
                }
            }
        }
        println("Registered listeners in $timeListeners")
    }
    fun registerAll() {
        val reflections = Reflections("net.blockventuremc.modules")

        registerListeners(reflections)

        registerCommands(reflections)

    }
}