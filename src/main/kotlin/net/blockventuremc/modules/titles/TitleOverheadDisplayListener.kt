package net.blockventuremc.modules.titles

import dev.fruxz.stacked.text
import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.extensions.toBlockUser
import net.blockventuremc.modules.structures.events.TrainEnterEvent
import net.blockventuremc.modules.structures.events.TrainExitEvent
import net.blockventuremc.modules.titles.events.TitleChangedEvent
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.util.Transformation
import org.joml.Vector3f

class TitleOverheadDisplayListener : Listener {

    private val textDisplays = mutableMapOf<Player, TextDisplay>()
    private val offsetVector = Vector3f(0.0f, 0.5f, 0.0f)

    init {
        VentureLibs.instance.shutDownHooks.add {
            textDisplays.forEach { (_, textDisplay) ->
                textDisplay.remove()
            }
        }
    }

    private fun createTextDisplay(player: Player) {
        val textDisplay = player.world.spawnEntity(player.eyeLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        textDisplays[player] = textDisplay
        textDisplay.text(text(player.toBlockUser().selectedTitle?.display?.let { it(player) }
            ?: "<color:#4b6584>No title"))
        textDisplay.alignment = TextDisplay.TextAlignment.CENTER
        textDisplay.isSeeThrough = true
        textDisplay.isDefaultBackground = true
        textDisplay.billboard = Display.Billboard.CENTER
        textDisplay.teleportDuration = 3

        textDisplay.transformation = Transformation(
            textDisplay.transformation.translation.add(offsetVector),
            textDisplay.transformation.leftRotation,
            Vector3f(
                1f,
                1f,
                1f
            ),
            textDisplay.transformation.rightRotation,
        )


        player.addPassenger(textDisplay)
        getLogger().info("Created textDisplay for ${player.name} with uuid ${textDisplay.uniqueId}")
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
    fun onPlayerEnterVehicle(event: VehicleEnterEvent) {
        if (event.entered !is Player) return

        val player = event.entered as Player
        textDisplays[player]?.remove()
    }

    @EventHandler
    fun onTrainExit(event: TrainExitEvent) {
        createTextDisplay(event.player)
    }

    @EventHandler
    fun onTrainEnter(event: TrainEnterEvent) {
        textDisplays[event.player]?.remove()
    }

    @EventHandler
    fun onPlayerLeaveVehicle(event: VehicleExitEvent) {
        if (event.exited !is Player) return

        val player = event.exited as Player
        if (!textDisplays.containsKey(player)) {
            createTextDisplay(player)
        }
    }

    @EventHandler
    fun onTitleChanged(event: TitleChangedEvent) {
        val player = event.player
        textDisplays[player]?.text(text(event.newTitle?.display?.let { it(player) } ?: "<color:#4b6584>No title"))
    }
}