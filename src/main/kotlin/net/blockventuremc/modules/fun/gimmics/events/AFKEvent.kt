package net.blockventuremc.modules.`fun`.gimmics.events

import net.blockventuremc.VentureLibs
import net.blockventuremc.modules.general.events.custom.AFKChangeEvent
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class AFKEvent: Listener {
    val afkPlayers = mutableSetOf<UUID>()

    init {
        runTask()
    }

    @EventHandler
    fun onAFKChange(event: AFKChangeEvent) {

        val bukkitPlayer = Bukkit.getPlayer(event.blockUser.uuid) ?: return

        if (event.afk) {
            afkPlayers.add(event.blockUser.uuid)

            bukkitPlayer.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, Int.MAX_VALUE, 1, false, false, false))
        } else {
            afkPlayers.remove(event.blockUser.uuid)

            // remove blindness effect
            bukkitPlayer.removePotionEffect(PotionEffectType.DARKNESS)
        }
    }

    private fun runTask() {
        Bukkit.getScheduler().runTaskTimer(VentureLibs.instance, Runnable {
            for (uuid in afkPlayers) {
                val player = Bukkit.getPlayer(uuid) ?: continue
                // TODO: Add custom particle with zzzz
                player.spawnParticle(Particle.SPLASH, player.location.add(0.0, 2.0, 0.0), 1)
            }
        }, 0, 20) // 20 ticks = 1 second
    }


}