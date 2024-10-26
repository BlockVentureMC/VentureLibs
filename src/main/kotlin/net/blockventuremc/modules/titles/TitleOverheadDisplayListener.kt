package net.blockventuremc.modules.titles

import dev.fruxz.stacked.text
import net.blockventuremc.extensions.toBlockUser
import net.blockventuremc.modules.titles.events.TitleChangedEvent
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.util.Vector

class TitleOverheadDisplayListener : Listener {

    private val textDisplays = mutableMapOf<Player, TextDisplay>()
    private val offsetVector = Vector(0.0, 0.8, 0.0)

    private fun createTextDisplay(player: Player) {
        val textDisplay = player.world.spawnEntity(player.location, EntityType.TEXT_DISPLAY) as TextDisplay
        textDisplays[player] = textDisplay
        textDisplay.text(text(player.toBlockUser().selectedTitle?.display(player) ?: "<color:#4b6584>No title"))
        textDisplay.alignment = TextDisplay.TextAlignment.CENTER
        textDisplay.isSeeThrough = true
        textDisplay.billboard = Display.Billboard.CENTER
        textDisplay.teleport(player.eyeLocation.add(offsetVector))
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        createTextDisplay(player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerJoinEvent) {
        val player = event.player
        textDisplays[player]?.remove()
    }

    @EventHandler
    fun onPlayerMove(event: PlayerJoinEvent) {
        val player = event.player
        textDisplays[player]?.teleport(player.eyeLocation.add(offsetVector))
    }

    @EventHandler
    fun onPlayerEnterVehicle(event: VehicleEnterEvent) {
        if (event.entered !is Player) return

        val player = event.entered as Player
        textDisplays[player]?.remove()
    }

    @EventHandler
    fun onPlayerLeaveVehicle(event: VehicleEnterEvent) {
        if (event.entered !is Player) return

        val player = event.entered as Player
        createTextDisplay(player)
    }

    @EventHandler
    fun onTitleChanged(event: TitleChangedEvent) {
        val player = event.player
        textDisplays[player]?.text(text(event.newTitle?.display(player) ?: "<color:#4b6584>No title"))
    }
}