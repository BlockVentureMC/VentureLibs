package net.blockventuremc.modules.rides.track

import net.blockventuremc.VentureLibs
import org.bukkit.Location
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import java.io.File
import java.util.*

class TrackRide(private val id: Int, private val origin: Location) {

    private val nodes = mutableListOf<TrackNode>()
    private val itemDisplays = mutableListOf<UUID>()

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
    }

    /**
     * Adds a TrackNode to the nodes list.
     *
     * @param node The TrackNode to be added.
     */
    fun addNode(node: TrackNode) {
        nodes.add(node)
    }

    /**
     * Displays each node in the track as itemDisplayEntities in the given world.
     *
     */
    fun displayTrack() {
        for (node in nodes) {
            // Display the track
            val display = node.displayInWord(origin)
            itemDisplays.add(display)
        }
        saveNodeEntitiesToFile()
    }

    /**
     * Hides the track from the world.
     */
    fun hideTrack() {
        for (itemDisplay in itemDisplays) {
            val entity = origin.world.getEntity(itemDisplay)
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
        itemDisplays.forEach { jsonArray.add(it.toString()) }
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
        jsonArray.forEach { itemDisplays.add(UUID.fromString(it.toString())) }
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