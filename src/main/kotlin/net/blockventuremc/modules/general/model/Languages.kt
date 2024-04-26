package net.blockventuremc.modules.general.model

import java.util.Locale

enum class Languages(val locale: Locale) {
    DE(Locale.GERMAN),
    EN(Locale.US);

    fun getLanguageCode(): String {
        return when (this) {
            DE -> "de-DE"
            EN -> "en-US"
        }
    }
}