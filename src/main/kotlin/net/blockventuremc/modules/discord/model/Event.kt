package net.blockventuremc.modules.discord.model

import dev.kord.core.Kord
import kotlinx.coroutines.Job

abstract class Event {
    abstract suspend fun execute(bot: Kord): Job
}