package net.blockventuremc.modules.warps

import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.*
import net.blockventuremc.modules.general.model.Ranks
import net.blockventuremc.modules.warps.gui.WarpGui
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault

@VentureCommand(
    name = "warp",
    permission = "blockventure.crew.warp",
    description = "Teleport to a warp",
    usage = "/warp <warp>",
    permissionDefault = PermissionDefault.TRUE,
    aliases = ["warps"]
)
class WarpCommand : CommandExecutor, TabExecutor {

    private val subcommandRanks = mapOf(
        "list" to Ranks.GUEST,
        "delete" to Ranks.DEVELOPER,
        "create" to Ranks.TEAM,
        "reload" to Ranks.DEVELOPER
    )

    /**
     * Executes a command sent by a command sender.
     *
     * @param sender The command sender.
     * @param command The executed command.
     * @param label The command label.
     * @param args The command arguments.
     * @return true if the command was executed successfully, false otherwise.
     */
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return true

        if (label.equals("warps", ignoreCase = true)) {
            WarpGui.openMenu(sender)
            return true
        }

        when (args.size) {
            1 -> {
                // Teleport to warp, list performs subcommand
                val arg = args[0]

                if (testIfSubcommand(sender, arg)) return true

                when (arg) {
                    "create" -> {
                        // Create warp
                        sender.sendError(
                            sender.translate("commands.warp.create.usage")?.message ?: "Usage: /warp create <warp>"
                        )
                        sender.sendDeniedSound()
                    }

                    "delete" -> {
                        // Delete warp
                        sender.sendError(
                            sender.translate("commands.warp.delete.usage")?.message ?: "Usage: /warp delete <warp>"
                        )
                        sender.sendDeniedSound()
                    }

                    "list" -> listWarps(sender)
                    "reload" -> reloadWarps(sender)
                    else -> teleportToWarp(sender, arg)
                }

            }

            2 -> {
                val subcommand = args[0]
                val arg = args[1]

                if (testIfSubcommand(sender, subcommand)) return true

                when (subcommand) {
                    "create" -> createWarp(sender, arg)
                    "delete" -> deleteWarp(sender, arg)
                    else -> {
                        showUsage(sender)
                    }
                }
            }

            3 -> {
                handleCreateWarpWithRank(sender, args[0], args[1], args[2])
            }

            4 -> {
                handleCreateWarpWithRank(sender, args[0], args[1], args[2], args[3])
            }

            else -> {
                showUsage(sender)
            }
        }

        return true
    }

    /**
     * Provides tab completion suggestions for the `onTabComplete` method.
     *
     * @param sender The command sender.
     * @param command The executed command.
     * @param label The command label.
     * @param args The command arguments.
     * @return A list of tab completion suggestions.
     */
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {

        val warps = WarpManager.getWarps().filter { sender.isRankOrHigher(it.rankNeeded) }.map { it.name }

        return when (args.size) {
            1 -> {
                (subcommandRanks.filter { sender.isRankOrHigher(it.value) }
                    .map { it.key } + warps).filter { it.startsWith(args[0]) }
            }

            2 -> {
                if (!sender.isRankOrHigher(Ranks.DEVELOPER)) return emptyList()
                when (args[0]) {
                    "delete" -> {
                        // Return all warps
                        warps.filter { it.startsWith(args[1]) }
                    }

                    else -> emptyList()
                }
            }

            3 -> {
                if (!sender.isRankOrHigher(Ranks.TEAM)) return emptyList()
                when (args[0]) {
                    "create" -> {
                        // Return all ranks
                        Ranks.entries.map { it.name }.filter { it.startsWith(args[2]) }
                    }

                    else -> emptyList()
                }
            }

            4 -> {
                if (!sender.isRankOrHigher(Ranks.TEAM)) return emptyList()
                when (args[0]) {
                    "create" -> {
                        // Return all types
                        WarpType.entries.map { it.name }.filter { it.startsWith(args[3]) }
                    }
                    else -> emptyList()
                }
            }

            else -> emptyList()
        }
    }

    /**
     * Tests if the command sender has the necessary rank to execute the given subcommand.
     *
     * @param sender The command sender
     * @param subcommand The subcommand to be tested
     * @return `true` if the command sender has the necessary rank, `false` otherwise
     */
    private fun testIfSubcommand(sender: CommandSender, subcommand: String): Boolean {
        val rankNeeded = subcommandRanks[subcommand] ?: return false
        if (!sender.isRankOrHigher(rankNeeded)) {
            sender.sendError(
                sender.translate("no_permission.rank", mapOf("rank" to rankNeeded.name))?.message
                    ?: "You need to be ${rankNeeded.name} to use this command."
            )
            return true
        }
        return false
    }

    /**
     * Lists all available warps to the command sender.
     *
     * @param sender The command sender.
     */
    private fun listWarps(sender: CommandSender) {
        val translatedClickToWarp = sender.translate("commands.warp.click_to_warp")?.message ?: "Click to warp."

        val warps = WarpManager.getWarps().filter { sender.isRankOrHigher(it.rankNeeded) }
            .map { "<click:run_command:'/warp ${it.name}'><hover:show_text:'<color:#2ecc71>$translatedClickToWarp (${it.type.name})</color>'><color:${it.rankNeeded.rank.color}>${it.name}</color></hover></click>" }
        sender.sendInfo("Warps: ${warps.joinToString(", ")}")
        sender.sendOpenSound()
    }

    /**
     * Teleports the sender to a specified warp.
     *
     * @param sender the command sender
     * @param arg the name of the warp to teleport to
     */
    private fun teleportToWarp(sender: CommandSender, arg: String) {
        val warp = WarpManager.getWarp(arg)

        if (warp == null || !sender.isRankOrHigher(warp.rankNeeded)) {
            sender.sendError(
                sender.translate("commands.warp.not_found", mapOf("warp" to arg))?.message ?: "Warp $arg not found."
            )
            return
        }

        if (sender !is Player) {
            sender.sendError("Only players can use this command.")
            return
        }

        sender.teleport(warp.location)
        sender.sendSuccessSound()
    }

    /**
     * Reloads the list of warps from the "warps.yml" configuration file.
     * Existing warps will be cleared before loading new ones.
     * Errors encountered during the loading process will be logged and captured using Sentry.
     *
     * @param sender The command sender who triggered the warp reload.
     */
    private fun reloadWarps(sender: CommandSender) {
        // Reload warps
        WarpManager.reloadWarps()
        sender.sendSuccess(sender.translate("commands.warp.reload.success")?.message ?: "Warps reloaded.")
        sender.sendSuccessSound()
    }

    /**
     * Creates a warp at the specified location with an optional rank requirement.
     *
     * @param sender the command sender creating the warp
     * @param arg the name of the warp
     * @param ranks the minimum rank required to access the warp, default is [Ranks.TEAM]
     * @param type the type of the warp, default is [WarpType.GENERIC]
     */
    private fun createWarp(sender: CommandSender, arg: String, ranks: Ranks = Ranks.TEAM, type: WarpType = WarpType.GENERIC) {
        // Create warp
        if (sender !is Player) {
            sender.sendError("Only players can use this command.")
            return
        }

        val warp = Warp(arg, sender.location, ranks, type)

        WarpManager.addWarp(warp)
        sender.sendSuccess(
            sender.translate(
                "commands.warp.create.success",
                mapOf("warp" to arg, "rank" to "<color:${ranks.rank.color}>${ranks.name}</color>")
            )?.message ?: "Warp $arg created."
        )
        sender.sendSuccessSound()
    }

    /**
     * Deletes the specified warp.
     *
     * @param sender the command sender deleting the warp
     * @param arg the name of the warp to delete
     */
    private fun deleteWarp(sender: CommandSender, arg: String) {
        // Delete warp
        val warp = WarpManager.getWarp(arg)

        if (warp == null) {
            sender.sendError(
                sender.translate("commands.warp.not_found", mapOf("warp" to arg))?.message ?: "Warp $arg not found."
            )
            return
        }

        WarpManager.removeWarp(warp.name)
        sender.sendSuccess(
            sender.translate("commands.warp.delete.success", mapOf("warp" to arg))?.message ?: "Warp $arg deleted."
        )
        sender.sendSuccessSound()
    }

    /**
     * Displays the usage message to the command sender.
     *
     * @param sender The command sender to display the usage message to.
     */
    private fun showUsage(sender: CommandSender) {
        // Show usage
        sender.sendError(sender.translate("commands.warp.invalid_usage")?.message ?: "Invalid usage.")
        sender.sendDeniedSound()
    }

    /**
     * Handles the creation of a warp with a specified rank requirement.
     *
     * @param sender The command sender.
     * @param subcommand The subcommand.
     * @param warp The name of the warp.
     * @param rankString The rank requirement for the warp.
     */
    private fun handleCreateWarpWithRank(sender: CommandSender, subcommand: String, warp: String, rankString: String, type: String = "GENERIC") {
        if (testIfSubcommand(sender, subcommand)) return

        if (subcommand != "create") {
            showUsage(sender)
            return
        }

        val rank = Ranks.entries.find { it.name.equals(rankString, ignoreCase = true) }

        if (rank == null) {
            sender.sendError(
                sender.translate(
                    "commands.warp.invalid_rank",
                    mapOf("rank" to rankString)
                )?.message ?: "Invalid rank."
            )
            sender.sendDeniedSound()
            return
        }

        val warpType = WarpType.entries.find { it.name.equals(type, true) } ?: run {
            sender.sendError(
                sender.translate(
                    "commands.warp.invalid_type",
                    mapOf("type" to type)
                )?.message ?: "Invalid type $type."
            )
            sender.sendDeniedSound()
            return
        }


        createWarp(sender, warp, rank, warpType)
    }
}