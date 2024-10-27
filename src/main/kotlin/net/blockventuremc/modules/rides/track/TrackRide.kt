package net.blockventuremc.modules.rides.track

import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.getLogger
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

class TrackRide(val id: Int, val origin: Location) {

    val nodes = mutableListOf<TrackNode>()
    private val itemDisplays = mutableMapOf<Int, UUID>()

    private val trackSegments = mutableMapOf<Pair<Int, Int>, TrackSegment>()

    private var highlightedNode = -1

    var nodeDistance = 0.0
    var totalLength = 0.0

    init {
        displayTrack()
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
     * Highlights a node by changing its display entity to the highlighted material
     * and updating the highlightedNode field.
     *
     * @param nodeId The ID of the node to highlight.
     */
    fun highlightNode(nodeId: Int) {

        val newEntityUUID = itemDisplays[nodeId]
        if (newEntityUUID != null) {
            val entity = origin.world.getEntity(newEntityUUID) as ItemDisplay
            entity.setItemStack(ItemStack(SegmentTypes.HIGHLIGHTED.material))
            highlightedNode = nodeId
        }
    }

    fun setSegmentType(nodeIdStart: Int, nodeIdEnd: Int, segment: TrackSegment) {
        val startNode = nodes.find { it.id == nodeIdStart } ?: run {
            getLogger().warn("Node $nodeIdStart not found.")
            return
        }
        val endNode = nodes.find { it.id == nodeIdEnd } ?: run {
            getLogger().warn("Node $nodeIdEnd not found.")
            return
        }
        if (segment.type == SegmentTypes.NORMAL) {
            trackSegments[Pair(nodeIdStart, nodeIdEnd)]?.let { segment ->
                trackSegments.remove(Pair(nodeIdStart, nodeIdEnd))
            }
        } else {
            trackSegments[Pair(nodeIdStart, nodeIdEnd)] = segment
        }

        recalculateSegments()
    }

    private fun recalculateSegments() {

        // Calculate the total length of the track
        nodeDistance = nodes[0].position.distance(nodes[1].position)
        totalLength = nodeDistance * nodes.size.toDouble()

        repaintSegments()
    }

    private fun repaintSegments() {
        for (itemDisplay in itemDisplays) {
            val entity = origin.world.getEntity(itemDisplay.value) as? ItemDisplay
            entity?.isCustomNameVisible = false
            entity?.setItemStack(ItemStack(SegmentTypes.NORMAL.material))
        }

        trackSegments.forEach { (pair, segment) ->

            val startNode = pair.first
            val endNode = pair.second

            for (id in startNode..endNode) {
                val entityUUID = itemDisplays[id]
                if (entityUUID != null) {
                    val entity = origin.world.getEntity(entityUUID) as ItemDisplay
                    entity.setItemStack(ItemStack(segment.type.material))
                    if (id == startNode || id == endNode) {
                        entity.isCustomNameVisible = true
                        entity.customName = "${segment.type.name} id=$id"
                    }
                }
            }
        }
        highlightedNode = -1
    }

    fun displayTrack() {
        if (!itemDisplays.isEmpty()) return
        for (node in nodes) {
            val display = node.displayInWord(origin)
            itemDisplays[node.id] = display
        }
        repaintSegments()
    }

    fun hideTrack() {
        for (itemDisplay in itemDisplays) {
            val entity = origin.world.getEntity(itemDisplay.value)
            if (entity?.chunk?.isForceLoaded == true) {
                entity.chunk.isForceLoaded = false
            }
            entity?.remove()
        }
        itemDisplays.clear()
    }

    fun findSegment(nodeId: Int): TrackSegment? {
        trackSegments.forEach { (pair, segment) ->
            val (min, max) = pair
            if (nodeId in min..max) {
                return segment
            }
        }
        return null
    }

    private fun saveSegmentsToFile() {
        // Save the nodes to a file
        val file = File(
            VentureLibs.instance.dataFolder,
            "rides/track/$id.json"
        ).also { it.parentFile.mkdirs(); it.createNewFile() }
        val jsonArray = JSONArray()
        itemDisplays.forEach {
            val jsonObj = JSONObject()
            jsonObj["id"] = it.key
            jsonObj["uuid"] = it.value.toString()
            jsonArray.add(jsonObj)
        }
        file.writeText(jsonArray.toJSONString())
    }

    private fun loadSegmentsFromFile() {
        // Load the nodes from a file
        val file = File(VentureLibs.instance.dataFolder, "rides/track/$id.json")
        if (!file.exists()) return
        val jsonArrayText = file.readText()
        val jsonArray = JSONParser().parse(jsonArrayText) as JSONArray
        var element = 0
        getLogger().info("Loaded $element nodes in ")
    }

    private fun deleteSeqmentsFile(): Unit {
        // Remove the nodes from a file
        val file = File(VentureLibs.instance.dataFolder, "rides/track/$id.json")
        if (!file.exists()) return
        file.delete()
    }
}