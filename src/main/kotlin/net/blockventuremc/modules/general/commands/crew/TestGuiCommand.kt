package net.blockventuremc.modules.general.commands.crew

import dev.fruxz.stacked.text
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.sendMessageBlock
import net.blockventuremc.utils.CharRepo
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@VentureCommand(
    name = "testgui",
    permission = "venturechat.testgui",
    description = "Test the GUI",
    usage = "/testgui",
    permissionDefault = PermissionDefault.OP
)
class TestGuiCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        val player = sender

        val title = CharRepo.getNeg(8) + "<white>" + CharRepo.MENU_CONTAINER_27 + CharRepo.getNeg(168) + CharRepo.MENU_BUTTON

        val inventory = Bukkit.createInventory(null, 27, text(title))

        player.openInventory(inventory)

        return true
    }



}