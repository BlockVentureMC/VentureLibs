package net.blockventuremc.modules.discord

import dev.fruxz.ascend.extension.logging.getItsLogger
import dev.kord.core.Kord
import net.blockventuremc.utils.RegisterManager

class DiscordBot(private val kord: Kord) {
    suspend fun start() {

        getItsLogger().info("Starting Discord bot")

        RegisterManager.registerDiscord(kord)

        kord.login()
    }
}