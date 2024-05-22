package net.blockventuremc.modules.rides.track

import net.blockventuremc.VentureLibs
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.modules.rides.track.segments.SegmentTypes
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import java.io.File
import java.util.*

@VentureCommand(
    name = "track",
    description = "The main command for the track module.",
    usage = "/track <subcommand>",
    permission = "rides.track",
    permissionDefault = PermissionDefault.OP
)
class TrackCommand : CommandExecutor, TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("Usage: /track <subcommand>")
            return true
        }

        when (args[0]) {
            "import" -> {
                if (args.size != 2) {
                    sender.sendMessage("Usage: /track import <trackId>")
                    return true
                }
                val trackId = args[1].toIntOrNull() ?: run {
                    sender.sendMessage("Invalid track ID.")
                    return true
                }
                performImportTrack(sender, trackId)
            }
            "list" -> {
                performListTracks(sender)
            }
            "show" -> {
                if (args.size != 2) {
                    sender.sendMessage("Usage: /track show <trackId>")
                    return true
                }
                val trackId = args[1].toIntOrNull() ?: run {
                    sender.sendMessage("Invalid track ID.")
                    return true
                }
                performShowTrack(sender, trackId)
            }
            "hide" -> {
                if (args.size != 2) {
                    sender.sendMessage("Usage: /track hide <trackId>")
                    return true
                }
                val trackId = args[1].toIntOrNull() ?: run {
                    sender.sendMessage("Invalid track ID.")
                    return true
                }
                performHideTrack(sender, trackId)
            }
            "select" -> {
                selectTrackNode(args, sender)
            }
            "segment" -> {
                setSegmentType(args, sender)
            }
            else -> {
                sender.sendMessage("Usage: /track <subcommand>")
            }
        }

        return true
    }

    private fun setSegmentType(args: Array<out String>, sender: CommandSender) {
        if (args.size < 5) {
            sender.sendMessage("Usage: /track segment <trackId> <nodeIdStart> <nodeIdEnd> <segmentType>")
            return
        }

        val trackId = args[1].toIntOrNull() ?: run {
            sender.sendMessage("Invalid track ID.")
            return
        }

        val track = TrackManager.tracks[trackId] ?: run {
            sender.sendMessage("Track $trackId does not exist.")
            return
        }

        val nodeIdStart = args[2].toIntOrNull() ?: run {
            sender.sendMessage("Invalid start node ID.")
            return
        }

        val nodeIdEnd = args[3].toIntOrNull() ?: run {
            sender.sendMessage("Invalid end node ID.")
            return
        }

        val segmentType = SegmentTypes.valueOf(args[4].uppercase(Locale.getDefault()))

        track.setSegmentType(nodeIdStart, nodeIdEnd, segmentType)
    }

    private fun selectTrackNode(args: Array<out String>, sender: CommandSender) {
        if (args.size < 2) {
            sender.sendMessage("Usage: /track select <trackId> [nodeId]")
            return
        }
        val trackId = args[1].toIntOrNull() ?: run {
            sender.sendMessage("Invalid track ID.")
            return
        }

        val track = TrackManager.tracks[trackId] ?: run {
            sender.sendMessage("Track $trackId does not exist.")
            return
        }

        if (args.size == 2) {
            track.highlightNode(track.nodes.first().id)
            return
        }

        val nodeId = args[2].toIntOrNull() ?: run {
            sender.sendMessage("Invalid node ID.")
            return
        }

        if (!track.nodes.any { it.id == nodeId }) {
            sender.sendMessage("Node $nodeId does not exist.")
            return
        }
        track.highlightNode(nodeId)
    }

    private fun performImportTrack(sender: Player, trackId: Int) {
        val file = File(VentureLibs.instance.dataFolder, "rides/track/$trackId.nl2").also { it.parentFile.mkdirs() }
        if (!file.exists()) {
            sender.sendMessage("Track file does not exist. Please upload the track file to ${file.path}.")
            return
        }
        TrackManager.tracks[trackId] = Nl2Importer(file, trackId, sender.location).import()
        sender.sendMessage("Track $trackId imported.")
        performShowTrack(sender, trackId)
    }

    private fun performListTracks(sender: Player) {
        sender.sendMessage("List of tracks:")
        TrackManager.tracks.forEach { (id, _) ->
            sender.sendMessage("Track $id")
        }
    }

    private fun performShowTrack(sender: Player, trackId: Int) {
        val track = TrackManager.tracks[trackId] ?: run {
            sender.sendMessage("Track $trackId does not exist.")
            return
        }
        track.displayTrack()
        sender.sendMessage("Track $trackId displayed.")
    }

    private fun performHideTrack(sender: Player, trackId: Int) {
        val track = TrackManager.tracks[trackId] ?: run {
            sender.sendMessage("Track $trackId does not exist.")
            return
        }
        track.hideTrack()
        sender.sendMessage("Track $trackId hidden.")
    }


    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> listOf("import", "show", "hide", "list", "select", "segment").filter { it.startsWith(args[0]) }
            2 -> when (args[0]) {
                "show", "hide", "select", "segment" -> TrackManager.tracks.keys.map { it.toString() }.filter { it.startsWith(args[1]) }
                else -> emptyList()
            }
            5 -> when (args[0]) {
                "segment" -> SegmentTypes.entries.map { it.name }.filter { it.startsWith(args[3]) }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}