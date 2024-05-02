package net.blockventuremc.modules.general.commands.club

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.isRankOrHigher
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.toBlockUser
import net.blockventuremc.extensions.translate
import net.blockventuremc.modules.general.model.Ranks
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

/**
 * Class representing a command for toggling fly mode.
 *
 * @property name The name of the command.
 * @property description The description of the command.
 * @property permission The permission required to use the command.
 * @property permissionDefault The default permission level for the command.
 * @property usage The usage of the command.
 */
@VentureCommand(
    name = "fly",
    description = "Toggle fly mode",
    permission = "blockventure.fly",
    permissionDefault = PermissionDefault.TRUE,
    usage = "/fly",
)
class FlyCommand : CommandExecutor {

    /**
     * Executes the command when it is invoked.
     *
     * @param sender The command sender.
     * @param command The command being executed.
     * @param label The command label.
     * @param args The command arguments.
     * @return True if the command was executed successfully, false otherwise.
     */
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isNotEmpty() && sender.isRankOrHigher(Ranks.Trial)) {
            val target = sender.server.getPlayerExact(args[0])
            if (target == null) {
                sender.sendMessagePrefixed("Player not found.")
                return true
            }
            changeFlyModeOfUser(sender, target)
            return true
        }

        if (sender !is Player) {
            sender.sendMessagePrefixed("This command is only available to players. Use /fly <player> to toggle fly mode for another player.")
            return true
        }

        if (!sender.isRankOrHigher(Ranks.ClubMember)) {
            sender.sendMessagePrefixed(
                sender.translate("no_permission.club_member")?.message
                    ?: "This command is only available to Club Members."
            )
            sender.allowFlight = false
            return true
        }

        changeFlyModeOfUser(sender, sender)

        return true
    }

    /**
     * Toggles the fly mode of a given player.
     *
     * @param commandExecutor The command executor.
     * @param targetPlayer The player whose fly mode will be changed.
     */
    private fun changeFlyModeOfUser(commandExecutor: CommandSender, targetPlayer: Player) {
        val wasFlightAllowed = targetPlayer.allowFlight

        targetPlayer.allowFlight = !wasFlightAllowed
        val flightModeStatusMessage = if (targetPlayer.allowFlight) "enabled" else "disabled"

        sendFlightModeToggleMessage(targetPlayer, flightModeStatusMessage)

        if (targetPlayer != commandExecutor) {
            sendNotifyExecutorMessage(commandExecutor, targetPlayer, flightModeStatusMessage)
        }
    }

    /**
     * Sends a flight mode toggle message to the player.
     *
     * @param player The player who will receive the message.
     * @param flightModeStatusMessage The status message indicating whether the flight mode is enabled or disabled.
     */
    private fun sendFlightModeToggleMessage(player: Player, flightModeStatusMessage: String) {
        val message = player.toBlockUser().translate(
            "fly_mode_toggled",
            mapOf("enabled" to flightModeStatusMessage)
        )?.message ?: "Fly mode is now $flightModeStatusMessage"
        player.sendMessagePrefixed(message)
    }

    /**
     * Sends a notification message to the command executor.
     *
     * @param commandExecutor The command sender who executed the command.
     * @param targetPlayer The player for which the flight mode was toggled.
     * @param flightModeStatusMessage The status message indicating whether the flight mode is enabled or disabled.
     */
    private fun sendNotifyExecutorMessage(
        commandExecutor: CommandSender,
        targetPlayer: Player,
        flightModeStatusMessage: String
    ) {
        val message = if (commandExecutor is Player) {
            val translatedMessage = commandExecutor.toBlockUser().translate(
                "fly_mode_toggled_by",
                mapOf(
                    "enabled" to flightModeStatusMessage,
                    "player" to targetPlayer.name
                )
            )?.message
            translatedMessage ?: "Fly mode of ${targetPlayer.name} is now $flightModeStatusMessage"
        } else {
            "Fly mode of ${targetPlayer.name} is now $flightModeStatusMessage"
        }
        commandExecutor.sendMessagePrefixed(message)
    }
}