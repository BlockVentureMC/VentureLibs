package net.blockventuremc.modules.general.model

import java.util.*

enum class Languages(val locale: Locale) {
    DE(Locale.GERMAN),
    EN(Locale.US),
    FR(Locale.FRENCH),
    NL(Locale("nl"));

    val code: String
        get() = getLanguageCode()

    fun getLanguageCode(): String {
        return when (this) {
            DE -> "de-DE"
            EN -> "en-US"
            FR -> "fr-FR"
            NL -> "nl-NL"
        }
    }
}