package net.blockventuremc.modules.general.model

enum class Ranks(val color: String, val bitsPerMinute: Long = 1) {
    Crew("#54a0ff", 3),
    Trial("#576574", 2),
    ClubMember("#ea8685", 2),
    Guest("#c8d6e5");

    fun isHigherOrEqual(rank: Ranks): Boolean {
        return this.ordinal <= rank.ordinal
    }
}