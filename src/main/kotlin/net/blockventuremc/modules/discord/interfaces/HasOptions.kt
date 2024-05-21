package net.blockventuremc.modules.discord.interfaces

import net.dv8tion.jda.api.interactions.commands.build.OptionData

/**
 * This interface represents an object that has options. Implementing classes need to provide
 * implementation for the method `getOptions()` which returns a list of [OptionData] objects.
 */
fun interface HasOptions {
    fun getOptions(): List<OptionData>
}