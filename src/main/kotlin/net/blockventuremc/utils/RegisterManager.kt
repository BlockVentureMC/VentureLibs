package net.blockventuremc.utils

import com.google.common.reflect.ClassPath
import io.sentry.Sentry
import net.blockventuremc.VentureLibs
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.consts.NAMESPACE_PLUGIN
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.extensions.sendInfo
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.modules.discord.annotations.PermissionScope
import net.blockventuremc.modules.discord.annotations.SlashCommand
import net.blockventuremc.modules.discord.events.SentryWrapperEventListener
import net.blockventuremc.modules.discord.interfaces.HasOptions
import net.blockventuremc.modules.discord.interfaces.HasSubcommandGroups
import net.blockventuremc.modules.discord.interfaces.HasSubcommands
import net.blockventuremc.modules.general.model.Languages
import net.blockventuremc.modules.i18n.TranslationCache
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.PluginCommand
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.time.measureTime


object RegisterManager {


    /**
     * Stores a map of loaded classes.
     *
     * The keys represent the simple names of the loaded classes,
     * and the values represent instances of the loaded classes.
     */
    private var loadedClasses = mapOf<String, Any>()
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

    /**
     * Loads all classes in a given package that are annotated with the specified annotation.
     *
     * @param packageName The name of the package.
     * @param annotation The annotation class.
     * @return A list of loaded classes annotated with the specified annotation.
     */
    private fun loadClassesInPackageWithAnnotation(
        packageName: String,
        annotation: KClass<out Annotation>
    ): List<KClass<out Any>> {
        try {
            val classLoader = VentureLibs.instance.javaClass.classLoader
            val allClasses = ClassPath.from(classLoader).allClasses
            val classes = mutableListOf<KClass<out Any>>()
            for (classInfo in allClasses) {
                if (!classInfo.name.startsWith("net.blockventuremc.modules")) continue
                if (classInfo.packageName.startsWith(packageName) && !classInfo.name.contains('$')) {
                    try {
                        val loadedClass = classInfo.load().kotlin
                        if (loadedClass.annotations.any { it.annotationClass == annotation }) {
                            classes.add(loadedClass)
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

    /**
     * Registers all ListenerAdapters and EventListeners in the given package.
     *
     * @return The JDABuilder object.
     */
    fun JDABuilder.registerAll(): JDABuilder {
        val reflections = loadClassesInPackage("net.blockventuremc.modules", ListenerAdapter::class)

        // Registering both ListenerAdapters and EventListeners
        val listenerTime = measureTime {
            for (clazz in reflections) {
                val clazzName = clazz.simpleName ?: clazz.qualifiedName ?: continue

                if (clazzName == "ListenerAdapter") continue

                val constructor = clazz.primaryConstructor ?: continue
                val listener = constructor.call()
                loadedClasses += clazzName to listener

                addEventListeners(SentryWrapperEventListener(listener))
                getLogger().info("Registered listener: ${listener.javaClass.simpleName}")
            }
        }
        getLogger().info("Registered listeners in $listenerTime")

        return this
    }

    /**
     * Registers slash commands for the JDA instance.
     * It scans for classes annotated with `SlashCommand` in the specified package
     * and registers the commands with their localized names and descriptions.
     *
     * @return The updated JDA instance after registering the commands.
     */
    fun JDA.registerCommands(): JDA {
        val reflections = loadClassesInPackageWithAnnotation("net.blockventuremc.modules", SlashCommand::class)

        // Registering both ListenerAdapters and EventListeners
        val listenerTime = measureTime {
            for (clazz in reflections) {
                val clazzName = clazz.simpleName ?: clazz.qualifiedName ?: continue
                val annotation: SlashCommand = clazz.annotations.first { it is SlashCommand } as SlashCommand

                val localizedNames = mutableMapOf<DiscordLocale, String>()
                for (language in Languages.entries) {
                    val desc = TranslationCache.get(language.code, "discord.commands.${annotation.name}.name")
                    if (desc != null) {
                        localizedNames[DiscordLocale.from(language.locale)] = desc.message
                    }
                }

                val localizedDescriptions = mutableMapOf<DiscordLocale, String>()
                for (language in Languages.entries) {
                    val desc = TranslationCache.get(language.code, "discord.commands.${annotation.name}.description")
                    if (desc != null) {
                        localizedDescriptions[DiscordLocale.from(language.locale)] = desc.message
                    }
                }

                val data = Commands.slash(annotation.name, annotation.description)
                    .apply {
                        when (annotation.permissionScope) {
                            PermissionScope.MODERATOR -> {
                                defaultPermissions =
                                    DefaultMemberPermissions.enabledFor(net.dv8tion.jda.api.Permission.MODERATE_MEMBERS)
                            }

                            PermissionScope.ADMIN -> {
                                defaultPermissions =
                                    DefaultMemberPermissions.enabledFor(net.dv8tion.jda.api.Permission.ADMINISTRATOR)
                            }

                            else -> {}
                        }
                        setNameLocalizations(localizedNames)
                        setDescriptionLocalizations(localizedDescriptions)
                    }

                if (clazz.simpleName !in loadedClasses) {
                    val constructor = clazz.primaryConstructor ?: continue
                    val command = constructor.call()
                    loadedClasses += clazzName to command
                    getLogger().info("Registered command class: $clazzName")
                }

                val command = loadedClasses[clazz.simpleName]

                if (command is HasOptions) {
                    val options = command.getOptions()

                    for (option in options) {
                        val localizedOptionNames = mutableMapOf<DiscordLocale, String>()
                        for (language in Languages.entries) {
                            val desc = TranslationCache.get(
                                language.code,
                                "discord.commands.${annotation.name}.option.${option.name}.name"
                            )
                            if (desc != null) {
                                localizedNames[DiscordLocale.from(language.locale)] = desc.message
                            }
                        }

                        val localizedOptionDescriptions = mutableMapOf<DiscordLocale, String>()
                        for (language in Languages.entries) {
                            val desc = TranslationCache.get(
                                language.code,
                                "discord.commands.${annotation.name}.option.${option.name}.description"
                            )
                            if (desc != null) {
                                localizedDescriptions[DiscordLocale.from(language.locale)] = desc.message
                            }
                        }

                        option.setNameLocalizations(localizedOptionNames)
                        option.setDescriptionLocalizations(localizedOptionDescriptions)
                    }

                    data.addOptions(options)
                }

                if (command is HasSubcommandGroups) {
                    val subcommandGroups = command.getSubcommandGroups()

                    for (group in subcommandGroups) {
                        val localizedGroupNames = mutableMapOf<DiscordLocale, String>()
                        for (language in Languages.entries) {
                            val desc = TranslationCache.get(
                                language.code,
                                "discord.commands.${annotation.name}.group.${group.name}.name"
                            )
                            if (desc != null) {
                                localizedNames[DiscordLocale.from(language.locale)] = desc.message
                            }
                        }

                        val localizedGroupDescriptions = mutableMapOf<DiscordLocale, String>()
                        for (language in Languages.entries) {
                            val desc = TranslationCache.get(
                                language.code,
                                "discord.commands.${annotation.name}.group.${group.name}.description"
                            )
                            if (desc != null) {
                                localizedDescriptions[DiscordLocale.from(language.locale)] = desc.message
                            }
                        }

                        group.setNameLocalizations(localizedGroupNames)
                        group.setDescriptionLocalizations(localizedGroupDescriptions)
                    }

                    data.addSubcommandGroups(subcommandGroups)
                }

                if (command is HasSubcommands) {
                    val subcommands = command.getSubCommands()

                    for (subcommand in subcommands) {
                        val localizedSubcommandNames = mutableMapOf<DiscordLocale, String>()
                        for (language in Languages.entries) {
                            val desc = TranslationCache.get(
                                language.code,
                                "discord.commands.${annotation.name}.subcommand.${subcommand.name}.name"
                            )
                            if (desc != null) {
                                localizedNames[DiscordLocale.from(language.locale)] = desc.message
                            }
                        }

                        val localizedSubcommandDescriptions = mutableMapOf<DiscordLocale, String>()
                        for (language in Languages.entries) {
                            val desc = TranslationCache.get(
                                language.code,
                                "discord.commands.${annotation.name}.subcommand.${subcommand.name}.description"
                            )
                            if (desc != null) {
                                localizedDescriptions[DiscordLocale.from(language.locale)] = desc.message
                            }
                        }

                        subcommand.setNameLocalizations(localizedSubcommandNames)
                        subcommand.setDescriptionLocalizations(localizedSubcommandDescriptions)
                    }

                    data.addSubcommands(subcommands)
                }

                upsertCommand(data).queue()
                getLogger().info("Registered global command: ${annotation.name}")
            }
        }
        getLogger().info("Registered listeners in $listenerTime")
        return this
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
            Bukkit.getConsoleSender().sendInfo("Command ${command.name} registered")
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
}