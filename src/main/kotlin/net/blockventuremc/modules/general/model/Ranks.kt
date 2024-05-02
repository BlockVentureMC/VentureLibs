package net.blockventuremc.modules.general.model

import net.blockventuremc.modules.general.manager.RankManager


enum class Ranks(val rank: Rank) {
    OWNER(RankManager.getRankByName("owner")),
    ADMIN(RankManager.getRankByName("admin")),
    DEVELOPER(RankManager.getRankByName("developer")),
    MODERATOR(RankManager.getRankByName("moderator")),
    ENGINEER(RankManager.getRankByName("engineer")),
    BUILDER(RankManager.getRankByName("builder")),
    CONTENT(RankManager.getRankByName("content")),
    SUPPORT(RankManager.getRankByName("support")),
    TEAM(RankManager.getRankByName("team")),
    CREATOR(RankManager.getRankByName("creator")),
    CLUBMEMBER(RankManager.getRankByName("clubmember")),
    GUEST(RankManager.getRankByName("default"));
}