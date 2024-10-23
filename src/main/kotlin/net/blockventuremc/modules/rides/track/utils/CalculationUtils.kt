package net.blockventuremc.modules.rides.track.utils

import net.blockventuremc.modules.rides.track.TrackNode
import kotlin.math.sqrt

fun calculateAdjustedSpeed(
    node: TrackNode,
    previousNode: TrackNode,
    trainWeight: Double,
    currentSpeed: Double,
    weatherFactor: Double,
    maintenanceFactor: Double
): Double {
    val heightDifference = node.posY - previousNode.posY

    val gravitationalAcceleration = 9.81 // m/s^2
    val potentialEnergyChange = trainWeight * gravitationalAcceleration * heightDifference

    val currentKineticEnergy = 0.5 * trainWeight * currentSpeed * currentSpeed
    val newKineticEnergy = currentKineticEnergy + potentialEnergyChange

    val newSpeed = sqrt(2 * newKineticEnergy / trainWeight)

    val dragFactor = weatherFactor * maintenanceFactor
    val adjustedSpeed = newSpeed * dragFactor
    return adjustedSpeed
}