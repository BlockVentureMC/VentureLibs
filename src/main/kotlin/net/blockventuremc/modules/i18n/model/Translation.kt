package net.blockventuremc.modules.i18n.model

import kotlinx.serialization.Serializable

@Serializable
data class Translation(
    val languageCode: String = "en-US",
    val messageKey: String,
    val message: String,
) {

    override fun toString(): String {
        return message
    }

}