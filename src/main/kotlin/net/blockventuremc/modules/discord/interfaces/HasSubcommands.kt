package net.blockventuremc.modules.discord.interfaces

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

/**
 * This interface represents an object that has subcommands. Implementing classes must provide
 * an implementation for the method `getSubCommands()` which returns a list of [SubcommandData] objects.
 */
fun interface HasSubcommands {
    fun getSubCommands(): List<SubcommandData>
}