package net.blockventuremc.extensions

import dev.kord.common.Locale
import dev.kord.rest.builder.interaction.OptionsBuilder
import net.blockventuremc.modules.i18n.TranslationCache


fun OptionsBuilder.translate() {
    val enUsDesc = TranslationCache.get(Locale.ENGLISH_UNITED_STATES.code, "discord.options.${name}.description")
    if (enUsDesc != null) {
        description = enUsDesc.message
    }

    val deDesc = TranslationCache.get(Locale.GERMAN.code, "discord.options.${name}.description")
    if (deDesc != null) {
        description(Locale.GERMAN, deDesc.message)
    }
}