package net.blockventuremc.modules.general.model

enum class Ranks(val color: String) {
    Crew("#54a0ff"),
    Trial("#576574"),
    ClubMember("#ea8685"),
    Guest("#c8d6e5");

    fun isHigherOrEqual(rank: Ranks): Boolean {
        return this.ordinal <= rank.ordinal
    }
}