package net.blockventuremc.modules.rides.track

import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.modules.rides.track.segments.NormalSegment
import net.blockventuremc.modules.rides.track.segments.SegmentTypes
import net.blockventuremc.modules.rides.track.segments.TrackSegment
import org.bukkit.Location
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.util.*
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

class TrackRide(private val id: Int, private val origin: Location) {

    val nodes = mutableListOf<TrackNode>()
    private val itemDisplays = mutableMapOf<Int, UUID>()
    private var trackSegments = listOf<TrackSegment>()
    private var highlightedNode = -1

    init {
        loadNodeEntitiesFromFile()
    }

    /**
     * Adds the given list of `TrackNode` objects to the `nodes` list of the `TrackRide` class.
     *
     * @param nodes The list of `TrackNode` objects to be added.
     */
    fun addNodes(nodes: List<TrackNode>) {
        this.nodes.addAll(nodes)

        // Recalculate the segments
        recalculateSegments()
    }

    /**
     * Adds a TrackNode to the nodes list.
     *
     * @param node The TrackNode to be added.
     */
    fun addNode(node: TrackNode) {
        nodes.add(node)

        // Recalculate the segments
        recalculateSegments()
    }

    /**
     * Highlights a node by changing its display entity to the highlighted material
     * and updating the highlightedNode field.
     *
     * @param nodeId The ID of the node to highlight.
     */
    fun highlightNode(nodeId: Int) {
        if (highlightedNode != -1) {
            val oldEntityUUID = itemDisplays[highlightedNode]
            if (oldEntityUUID != null) {
                val entity = origin.world.getEntity(oldEntityUUID) as ItemDisplay
                val oldSegment = trackSegments.find { trackSegment -> trackSegment.nodes.any { it.id == highlightedNode } }
                if (oldSegment != null) {
                    entity.setItemStack( ItemStack(oldSegment.function.trackDisplay.material))
                }
            }
        }

        val newEntityUUID = itemDisplays[nodeId]
        if (newEntityUUID != null) {
            val entity = origin.world.getEntity(newEntityUUID) as ItemDisplay
            entity.setItemStack( ItemStack(SegmentTypes.HIGHLIGHTED.material))
            highlightedNode = nodeId
        }
    }

    fun setSegmentType(nodeIdStart: Int, nodeIdEnd: Int, segmentType: SegmentTypes) {
        val startNode = nodes.find { it.id == nodeIdStart } ?: run {
            getLogger().warn("Node $nodeIdStart not found.")
            return
        }
        val endNode = nodes.find { it.id == nodeIdEnd } ?: run {
            getLogger().warn("Node $nodeIdEnd not found.")
            return
        }

       val nodes = nodes.subList(nodes.indexOf(startNode), nodes.indexOf(endNode) + 1)
        val segment = TrackSegment(trackSegments.size + 1, nodes, segmentType.segmentType.createInstance())
        trackSegments += segment



        recalculateSegments()
    }

    /**
     * Recalculates the segments of a track ride.
     *
     * This method filters out all NormalSegments from the trackSegments list, collects the ranges of the remaining
     * non-normal segments, and creates NormalSegments for any gaps between those ranges. It also handles the wrap-around
     * segment if necessary. Finally, it adds the non-normal segments back to the newSegments list and sorts them by node index.
     * The trackSegments list is then updated with the new segments.
     */
    private fun recalculateSegments() {
        // Filter out all NormalSegments
        val nonNormalSegments = trackSegments.filter { it.function !is NormalSegment }

        // Collect the ranges of non-normal segments
        val ranges = nonNormalSegments.flatMap { segment ->
            val startIndex = nodes.indexOf(segment.nodes.first())
            val endIndex = nodes.indexOf(segment.nodes.last())
            listOf(startIndex to endIndex)
        }.sortedBy { it.first }

        val newSegments = mutableListOf<TrackSegment>()
        var lastEndIndex = 0

        for (range in ranges) {
            val (startIndex, endIndex) = range
            if (lastEndIndex < startIndex - 1) {
                // Create a NormalSegment for the gap
                val normalSegmentNodes = nodes.subList(lastEndIndex, startIndex)
                newSegments.add(TrackSegment(newSegments.size + 1, normalSegmentNodes, NormalSegment()))
            }
            lastEndIndex = endIndex + 1
        }

        // Handle the segment from the last end index to the end of the nodes list
        if (lastEndIndex < nodes.size) {
            val normalSegmentNodes = nodes.subList(lastEndIndex, nodes.size)
            newSegments.add(TrackSegment(newSegments.size + 1, normalSegmentNodes, NormalSegment()))
        }

        // Handle wrap-around segment
        if (ranges.isNotEmpty() && ranges.first().first > 0 && ranges.last().second < nodes.size - 1) {
            val firstStartIndex = ranges.first().first
            val lastSegmentEndIndex = ranges.last().second
            if (lastSegmentEndIndex < nodes.size - 1 || firstStartIndex > 0) {
                val normalSegmentNodes = nodes.subList(lastSegmentEndIndex, nodes.size) + nodes.subList(0, firstStartIndex)
                newSegments.add(TrackSegment(newSegments.size + 1, normalSegmentNodes, NormalSegment()))
            }
        }

        // Add the non-normal segments back
        newSegments.addAll(nonNormalSegments)

        // Sort the segments by their node index
        trackSegments = newSegments.sortedBy { nodes.indexOf(it.nodes.first()) }

        getLogger().info("Recalculated segments for track $id.")
        for (segment in trackSegments) {
            segment.calculateSpeed(0.0, 1.0, 1.0, 1.0)
            getLogger().info("Segment ${segment.id} (${segment.function::class.simpleName}): ${segment.nodes.first().id} - ${segment.nodes.last().id} with a length of ${segment.length} meters. Average speed: ${segment.averageSpeed} m/s.")
        }

        repaintSegments()
    }

    private fun repaintSegments() {
        for (segment in trackSegments) {
            for (node in segment.nodes) {
                val entityUUID = itemDisplays[node.id]
                if (entityUUID != null) {
                    val entity = origin.world.getEntity(entityUUID) as ItemDisplay
                    entity.setItemStack( ItemStack(segment.function.trackDisplay.material))
                }
            }
        }
        highlightedNode = -1
    }


    /**
     * Displays each node in the track as itemDisplayEntities in the given world.
     *
     */
    fun displayTrack() {
        for (node in nodes) {

            if (itemDisplays.containsKey(node.id)) {
                // The node is already displayed
                continue
            }

            // Display the track
            val display = node.displayInWord(origin)
            itemDisplays[node.id] = display
        }
        saveNodeEntitiesToFile()
    }

    /**
     * Hides the track from the world.
     */
    fun hideTrack() {
        for (itemDisplay in itemDisplays) {
            val entity = origin.world.getEntity(itemDisplay.value)
            if (entity?.chunk?.isForceLoaded == true) {
                entity.chunk.isForceLoaded = false
            }
            entity?.remove()
        }
        removeNodeEntitiesFromFile()
    }

    /**
     * Saves the node entities to a file.
     * The nodes are saved as a JSON array in a file located at the "rides*/
    private fun saveNodeEntitiesToFile() {
        // Save the nodes to a file
        val file = File(VentureLibs.instance.dataFolder, "rides/track/$id.json").also { it.parentFile.mkdirs(); it.createNewFile() }
        val jsonArray = JSONArray()
        itemDisplays.forEach {
            val jsonObj = JSONObject()
            jsonObj["id"] = it.key
            jsonObj["uuid"] = it.value.toString()
            jsonArray.add(jsonObj)
        }
        file.writeText(jsonArray.toJSONString())
    }

    /**
     * Loads the node entities from a file.
     * The nodes are loaded from a JSON array in a file located at the "rides" directory.
     */
    private fun loadNodeEntitiesFromFile() {
        // Load the nodes from a file
        val file = File(VentureLibs.instance.dataFolder, "rides/track/$id.json")
        if (!file.exists()) return
        val jsonArrayText = file.readText()
        val jsonArray = JSONParser().parse(jsonArrayText) as JSONArray
        jsonArray.forEach {
            val jsonObj = it as JSONObject
            val id = jsonObj["id"]
            val uuid = jsonObj["uuid"]
            itemDisplays[id as Int] = UUID.fromString(uuid as String)
        }
    }

    /**
     * Removes the node entities from a file.
     *
     * This method is responsible for deleting the file that contains the node entities for the track ride.
     * It checks if the file exists and then deletes it.
     * If the file does not exist, it does nothing.
     * This method is used internally and should not be called directly.
     */
    private fun removeNodeEntitiesFromFile(): Unit {
        // Remove the nodes from a file
        val file = File(VentureLibs.instance.dataFolder, "rides/track/$id.json")
        if (!file.exists()) return
        file.delete()
    }
}