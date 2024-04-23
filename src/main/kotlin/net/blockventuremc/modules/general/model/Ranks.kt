package net.blockventuremc.modules.general.model

enum class Ranks(val color: String) {
    Staff("#FF0000"),
    Plus("#71368a"),
    Default("#2ecc71");

    fun isHigherOrEqual(rank: Ranks): Boolean {
        return this.ordinal <= rank.ordinal
    }
}