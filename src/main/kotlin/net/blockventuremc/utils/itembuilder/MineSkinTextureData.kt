package net.blockventuremc.utils.itembuilder

import kotlinx.serialization.Serializable

@Serializable
data class MineSkinTextureData(
    val texture: Texture = Texture(),
    val uuid: String = ""
)
