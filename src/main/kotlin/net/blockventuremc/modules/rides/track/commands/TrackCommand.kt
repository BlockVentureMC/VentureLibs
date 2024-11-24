package net.blockventuremc.modules.rides.track.commands

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.blockventuremc.VentureLibs
import net.blockventuremc.annotations.VentureCommand
import net.blockventuremc.extensions.teleportAsyncWithPassengers
import net.blockventuremc.modules.general.events.custom.toVentureLocation
import net.blockventuremc.modules.rides.track.Nl2Importer
import net.blockventuremc.modules.rides.track.TrackManager
import net.blockventuremc.modules.rides.track.segments.AccelerationSegment
import net.blockventuremc.modules.rides.track.segments.BreakSegment
import net.blockventuremc.modules.rides.track.segments.BreakSegment.BreakType
import net.blockventuremc.modules.rides.track.segments.LaunchSegment
import net.blockventuremc.modules.rides.track.segments.LiftSegment
import net.blockventuremc.modules.rides.track.segments.SegmentTypes
import net.blockventuremc.modules.rides.track.segments.StationSegment
import net.blockventuremc.modules.rides.track.segments.TrackSegment
import net.blockventuremc.modules.structures.Animation
import net.blockventuremc.modules.structures.Attachment
import net.blockventuremc.modules.structures.ItemAttachment
import net.blockventuremc.modules.structures.StructureManager
import net.blockventuremc.modules.structures.TrainRegistry
import net.blockventuremc.modules.structures.impl.Cart
import net.blockventuremc.modules.structures.impl.Seat
import net.blockventuremc.modules.structures.impl.Train
import net.blockventuremc.utils.itembuilder.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import org.bukkit.util.BlockVector
import org.bukkit.util.Vector
import sun.net.www.content.text.plain
import java.io.File

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

            "placeblocks" -> {
                if (args.size != 2) {
                    sender.sendMessage("Usage: /track placeblocks <trackId>")
                    return true
                }
                val trackId = args[1].toIntOrNull() ?: run {
                    sender.sendMessage("Invalid track ID.")
                    return true
                }
                performPlaceTrackBlocks(sender, trackId)
            }

            "debug" -> {
                if (args.size != 2) {
                    sender.sendMessage("Usage: /track debug <trackId>")
                    return true
                }
                val trackId = args[1].toIntOrNull() ?: run {
                    sender.sendMessage("Invalid track ID.")
                    return true
                }
                performDebugDistance(sender, trackId)
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
            "tp" -> {
                if (args.size != 2) {
                    sender.sendMessage("Usage: /track tp <trackId>")
                    return true
                }
                val trackId = args[1].toIntOrNull() ?: run {
                    sender.sendMessage("Invalid track ID.")
                    return true
                }
                performTeleportTrack(sender, trackId)
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

            "speed" -> {
                if (args.size != 2) {
                    sender.sendMessage("Usage: /track speed <velocity>")
                    return true
                }
                performDebugSpeed(sender, args[1].toFloat().coerceIn(-200.0f, 200.0f))
            }

            "spawn" -> {
                if (args.size != 3) {
                    sender.sendMessage("Usage: /track spawn <name> <trackId>")
                    return true
                }
                val trackId = args[2].toIntOrNull() ?: run {
                    sender.sendMessage("Invalid track ID.")
                    return true
                }
                performSpawnTrainOnTrack(sender, trackId, args[1])
            }

            "despawn" -> {
                if (args.size != 2) {
                    sender.sendMessage("Usage: /track despawn <trackId>")
                    return true
                }
                val trackId = args[1].toIntOrNull() ?: run {
                    sender.sendMessage("Invalid track ID.")
                    return true
                }

                performDespawnTrainOnTrack(sender, trackId)
            }

            else -> {
                sender.sendMessage("Usage: /track <subcommand>")
            }
        }

        return true
    }

    private fun setSegmentType(args: Array<out String>, sender: CommandSender) {
        if (args.size < 5) {
            sender.sendMessage("Usage: /track segment <trackId> <nodeIdStart> <nodeIdEnd> <segmentType> <values..>")
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

        val segmentType = SegmentTypes.entries.find { it.name.equals(args[4], true) } ?: run {
            sender.sendMessage("Invalid segment type.")
            return
        }

        var trackSegment: TrackSegment? = null

        when (segmentType) {
            SegmentTypes.LIFT -> {
                val value = args[5].toFloatOrNull() ?: run {
                    sender.sendMessage("Invalid value")
                    return
                }
                trackSegment = LiftSegment(nodeIdStart, nodeIdEnd, value)
                sender.sendMessage("new Lift Segment")

            }

            SegmentTypes.NORMAL -> {
                trackSegment = TrackSegment(nodeIdStart, nodeIdEnd)
                sender.sendMessage("set Normal Segment")
            }

            SegmentTypes.LAUNCH -> {
                val value = args[5].toFloatOrNull() ?: run {
                    sender.sendMessage("Invalid acceleration (m/s²) value")
                    return
                }
                trackSegment = LaunchSegment(nodeIdStart, nodeIdEnd, value)
                sender.sendMessage("new Launch Segment")
            }

            SegmentTypes.BRAKE -> {
                val breakType = BreakType.entries.find { it.name.equals(args[5], true) } ?: run {
                    sender.sendMessage("Invalid break type (blockbreak, trimbreak)")
                    return
                }
                val minspeed = args[6].toFloatOrNull() ?: run {
                    sender.sendMessage("Invalid minspeed value")
                    return
                }

                trackSegment = BreakSegment(nodeIdStart, nodeIdEnd, breakType, minspeed)
                sender.sendMessage("new Break Segment")
            }

            SegmentTypes.STATION -> {
                val stationSpeed = args[5].toFloatOrNull() ?: run {
                    sender.sendMessage("Invalid station speed value")
                    return
                }
                trackSegment = StationSegment(nodeIdStart, nodeIdEnd, stationSpeed)
                sender.sendMessage("new Station Segment")
            }

            SegmentTypes.ACCELERATION -> {
                val value = args[5].toFloatOrNull() ?: run {
                    sender.sendMessage("Invalid fixedSpeed (m/s) value")
                    return
                }
                trackSegment = AccelerationSegment(nodeIdStart, nodeIdEnd, value)
                sender.sendMessage("new Acceleration Segment")
            }

            SegmentTypes.HIGHLIGHTED -> {
                // No action required
            }
        }
        if (trackSegment == null) {
            sender.sendMessage("Logic fehlt hier")
            return
        }

        TrackManager.saveTrack(track)
        track.setSegmentType(nodeIdStart, nodeIdEnd, trackSegment)
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

        if (TrackManager.tracks.containsKey(trackId)) {
            sender.sendMessage("Track $trackId already exists.")
            return
        }

        val trackRide = Nl2Importer(file, trackId, sender.location).import()
        TrackManager.tracks[trackId] = trackRide
        TrackManager.saveTrack(trackRide)
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

    private fun performTeleportTrack(sender: Player, trackId: Int) {
        val track = TrackManager.tracks[trackId] ?: run {
            sender.sendMessage("Track $trackId does not exist.")
            return
        }
        sender.teleportAsyncWithPassengers(track.origin)
        sender.sendMessage("teleportet to $trackId.")
    }

    private fun performHideTrack(sender: Player, trackId: Int) {
        val track = TrackManager.tracks[trackId] ?: run {
            sender.sendMessage("Track $trackId does not exist.")
            return
        }
        track.hideTrack()
        sender.sendMessage("Track $trackId hidden.")
    }

    private fun performPlaceTrackBlocks(sender: Player, trackId: Int) {

        val track = TrackManager.tracks[trackId] ?: run {
            sender.sendMessage("Track $trackId does not exist.")
            return
        }
        val item = sender.inventory.itemInMainHand
        sender.sendMessage("Plaziere Blöcke...")

        CoroutineScope(Dispatchers.Default).launch {
            track.nodes.forEach { node ->
                Bukkit.getScheduler().runTask(VentureLibs.instance, Runnable {
                    val offset = node.upVector.normalize().multiply(-1.0)
                    val position = track.origin.toVector().add(node.position).add(offset)
                    sender.world.getBlockAt(Location(sender.world, position.x, position.y, position.z)).type = item.type
                })
            }
        }
        sender.sendMessage("Es wurden Blöcke platziert.")
    }

    private fun performDebugSpeed(sender: Player, velocity: Float) {
        val first = StructureManager.trains.values.last()
        first.velocity = velocity
        sender.sendMessage("Velocity $velocity")
    }

    private fun performDebugDistance(sender: Player, trackId: Int) {
        val track = TrackManager.tracks[trackId] ?: run {
            sender.sendMessage("Track $trackId does not exist.")
            return
        }
        sender.sendMessage("Distance between nodes 0 and 1 is ${track.nodeDistance} meters. Total Length of ${track.totalLength}")
    }

    private fun performSpawnTrainOnTrack(sender: Player, trackId: Int, name: String) {
        val track = TrackManager.tracks[trackId] ?: run {
            sender.sendMessage("Track $trackId does not exist.")
            return
        }

        val trainAbstract = TrainRegistry.trains[name] ?: run {
            sender.sendMessage("Train $name does not exist.")
            return
        }
        val train = trainAbstract.train(track, 0.1)
        train.initialize()
        StructureManager.trains[train.uuid] = train

        sender.sendMessage("Train $name spawned on Track $trackId.")

    }

    private fun performDespawnTrainOnTrack(sender: Player, trackId: Int) {
        TrackManager.tracks[trackId] ?: run {
            sender.sendMessage("Track $trackId does not exist.")
            return
        }
        val trains = StructureManager.trains.map { it.value }
            .filter { it.trackRide.id == trackId }
        trains.forEach { train ->
            train.remove()
            StructureManager.trains.remove(train.uuid)
        }
        sender.sendMessage("Trains despawned on Track $trackId.")
    }


    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf(
                "import",
                "tp",
                "show",
                "hide",
                "list",
                "select",
                "segment",
                "spawn",
                "despawn",
                "placeblocks",
                "speed"
            ).filter { it.startsWith(args[0]) }

            2 -> when(args[0]) {
                "show", "hide", "select", "segment", "despawn", "tp", "placeblocks" ->
                    TrackManager.tracks.keys.map { it.toString() }.filter { it.startsWith(args[1]) }
                "spawn" -> TrainRegistry.trains.keys.map { it.toString() }.filter { it.startsWith(args[1]) }

                else -> emptyList()
            }
            3 -> when (args[0]) {
                "spawn" ->  TrackManager.tracks.keys.map { it.toString() }.filter { it.startsWith(args[1]) }
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