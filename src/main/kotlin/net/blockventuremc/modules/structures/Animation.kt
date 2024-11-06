package net.blockventuremc.modules.structures

abstract class Animation {

    val animationMap = mutableMapOf<String, Any>()

    abstract fun animate()
}