package net.blockventuremc.cache

import net.blockventuremc.VentureLibs
import net.blockventuremc.extensions.getLogger
import net.blockventuremc.modules.general.events.custom.Area
import net.blockventuremc.modules.general.events.custom.toVentureLocation
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object AreaCache {

    private val areas = mutableMapOf<String, Area>()
    private val cacheLock: ReadWriteLock = ReentrantReadWriteLock()

    init {
        reloadAreas()
    }

    fun reloadAreas() {
        cacheLock.writeLock().lock()
        areas.clear()

        val areasConfig = VentureLibs.instance.config
        val areaNames = areasConfig.getConfigurationSection("areas")?.getKeys(false) ?: return
        for (area in areaNames) {
            getLogger().info("Loading area $area")
            val name = areasConfig.getString("areas.$area.name") ?: continue
            val point1 = areasConfig.getString("areas.$area.p1") ?: continue
            val point2 = areasConfig.getString("areas.$area.p2") ?: continue
            val chatRoomType = areasConfig.getString("areas.$area.chatRoomType") ?: "<color:#7593ff>ChatRoom"
            areas[area] = Area(name, point1.toVentureLocation(), point2.toVentureLocation(), chatRoomType)
            getLogger().info("Loaded area $area")
        }
        getLogger().info("Loaded ${areas.size} areas.")
        cacheLock.writeLock().unlock()
    }

    fun addArea(area: Area) {
        cacheLock.writeLock().lock()
        areas[area.name] = area
        cacheLock.writeLock().unlock()
    }

    fun removeArea(area: Area) {
        cacheLock.writeLock().lock()
        areas.remove(area.name)
        cacheLock.writeLock().unlock()
    }

    fun getArea(name: String): Area? {
        cacheLock.readLock().lock()
        try {
            return areas[name]
        } finally {
            cacheLock.readLock().unlock()
        }
    }

    fun getAreas(): List<Area> {
        cacheLock.readLock().lock()
        try {
            return areas.values.toList()
        } finally {
            cacheLock.readLock().unlock()
        }
    }

}