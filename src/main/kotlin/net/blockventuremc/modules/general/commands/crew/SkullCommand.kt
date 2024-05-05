package net.blockventuremc.modules.general.commands.crew

import com.rainbowislands.utility.utils.toItemBuilder
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendMessagePrefixed
import net.blockventuremc.extensions.translate
import org.bukkit.Material
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@VentureCommand(
    name = "skull",
    aliases = ["sk", "head"],
    permission = "blockventure.skull",
    permissionDefault = PermissionDefault.OP,
    description = "Give yourself a skull",
)
class SkullCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: org.bukkit.command.Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return true

        if (args.isEmpty()) {
            sender.sendMessagePrefixed(sender.translate("commands.skull.usage")?.message ?: "Usage: /skull <name>")
            return true
        }

        val skullName = args[0]

        if (skullName.length > 24) {
            val drops = sender.inventory.addItem(
                Material.PLAYER_HEAD.toItemBuilder {
                    display("<gray>MiniBlock")
                    texture(skullName)
                }.build()
            )
            drops.forEach { (_, itemStack) ->
                sender.world.dropItem(sender.location, itemStack)
            }
        } else {
            val drops = sender.inventory.addItem(
                Material.PLAYER_HEAD.toItemBuilder {
                    display(sender.translate("commands.skull.name", mapOf("name" to skullName))?.message ?: "<gray>Head of $skullName")
                    owner(skullName)
                }.build()
            )
            drops.forEach { (_, itemStack) ->
                sender.world.dropItem(sender.location, itemStack)
            }
        }
        sender.sendMessagePrefixed(sender.translate("commands.skull.given", mapOf("name" to skullName))?.message ?: "You have been given a skull of $skullName.")
        return true
    }
}