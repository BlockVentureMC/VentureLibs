package net.blockventuremc.modules.structures

import org.bukkit.util.Vector

class Rigidbody {

    var mass = 200.0//KG
    var useGravity = true

    //Properties
    var position: Vector = Vector()
    var rotation: Vector = Vector()
    var velocity: Vector = Vector()
    var angularVelocity: Vector = Vector()

    //Methods
    fun updateTransform() {

    }

    fun despawn() {

    }

}