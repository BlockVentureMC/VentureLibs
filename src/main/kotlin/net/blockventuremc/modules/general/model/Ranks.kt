package net.blockventuremc.modules.general.model

enum class Ranks(val color: String) {
    Crew("#54a0ff"),
    Staff("#576574"),
    Plus("#f368e0"),
    Default("#c8d6e5");

    fun isHigherOrEqual(rank: Ranks): Boolean {
        return this.ordinal <= rank.ordinal
    }
}