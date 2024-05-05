package net.blockventuremc.modules.general.model

import net.blockventuremc.modules.general.manager.RankManager


enum class Ranks(val rank: Rank) {
    ADMIN(RankManager.getRankByName("admin")),
    DEVELOPER(RankManager.getRankByName("developer")),
    MODERATOR(RankManager.getRankByName("moderator")),
    BUILDER(RankManager.getRankByName("builder")),
    TEAM(RankManager.getRankByName("team")),
    CREATOR(RankManager.getRankByName("creator")),
    CLUBMEMBER(RankManager.getRankByName("clubmember")),
    GUEST(RankManager.getRankByName("default"));
}