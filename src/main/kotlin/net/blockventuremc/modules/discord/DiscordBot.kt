package net.blockventuremc.modules.discord

import dev.kord.core.Kord
import net.blockventuremc.utils.RegisterManager

class DiscordBot(private val kord: Kord) {
    suspend fun start() {
        RegisterManager.registerDiscord(kord)

        kord.login()
    }
}