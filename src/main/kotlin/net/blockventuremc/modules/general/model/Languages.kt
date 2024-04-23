package net.blockventuremc.modules.general.model

enum class Languages{
    DE, EN;

    fun getLanguageCode(): String {
        return when(this) {
            DE -> "de-DE"
            EN -> "en-US"
        }
    }
}