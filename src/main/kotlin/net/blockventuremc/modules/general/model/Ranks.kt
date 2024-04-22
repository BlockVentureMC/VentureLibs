package net.blockventuremc.modules.general.model

enum class Ranks(val color: String) {
    Staff("#FF0000"),
    Plus("#FF0000"),
    Default("#FF0000");

    fun isHigherOrEqual(rank: Ranks): Boolean {
        return this.ordinal <= rank.ordinal
    }
}