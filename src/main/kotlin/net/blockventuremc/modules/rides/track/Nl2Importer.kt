package net.blockventuremc.modules.rides.track

import org.bukkit.Location
import java.io.File

class Nl2Importer(private val file: File, private val trackId: Int, private val origin: Location) {

    /**
     * Imports the track from the file.
     *
     * @return The track ride.
     */
    fun import(): TrackRide {
        // Import the track from the file
        val trackRide = TrackRide(trackId, origin)

        // Load the track nodes from the file
        val trackNodes = loadTrackNodesFromFile()

        // Add the track nodes to the track ride
        trackRide.addNodes(trackNodes)

        return trackRide
    }

    private fun loadTrackNodesFromFile(): List<TrackNode> {
        // Load the track nodes from the file
        val trackNodes = mutableListOf<TrackNode>()

        file.forEachLine { line ->
            try {
                if (line.contains("No.")) return@forEachLine
                trackNodes.add(covertToTrackNode(line))
            } catch (e: IllegalArgumentException) {
                // Skip the line if it is invalid
                e.printStackTrace()
            }
        }
        return trackNodes
    }

    /**
     * Converts a line of data into a TrackNode object.
     *
     * @param line The line of data to be converted.
     *             The line should contain the following parts separated by either a space or a semicolon:
     *             - ID
     *             - X coordinate of the position
     *             - Y coordinate of the position
     *             - Z coordinate of the position
     *             - X coordinate of the front vector
     *             - Y coordinate of the front vector
     *             - Z coordinate of the front vector
     *             - X coordinate of the left vector
     *             - Y coordinate of the left vector
     *             - Z coordinate of the left vector
     *             - X coordinate of the up vector
     *             - Y coordinate of the up vector
     *             - Z coordinate of the up vector
     * @return The TrackNode object created from the line of data.
     * @throws IllegalArgumentException if the line does not contain the correct number of parts.
     */
    private fun covertToTrackNode(line: String): TrackNode {
        // Convert the data to a TrackNode object
        val splitterator = if (line.contains(";")) ";" else "\t"

        val parts = line.split(splitterator)
        require(parts.size == 13) { "Invalid number of parts in line: $line" }
        require(parts[0].toIntOrNull() != null) { "Invalid ID in line: $line" }

        return TrackNode(
            id = parts[0].toInt(),
            posX = parts[1].toDouble(),
            posY = parts[2].toDouble(),
            posZ = parts[3].toDouble(),
            frontX = parts[4].toFloat(),
            frontY = parts[5].toFloat(),
            frontZ = parts[6].toFloat(),
            leftX = parts[7].toFloat(),
            leftY = parts[8].toFloat(),
            leftZ = parts[9].toFloat(),
            upX = parts[10].toFloat(),
            upY = parts[11].toFloat(),
            upZ = parts[12].toFloat()
        )
    }
}