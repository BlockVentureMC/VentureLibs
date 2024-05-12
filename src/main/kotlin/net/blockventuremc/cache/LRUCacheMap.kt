package net.blockventuremc.cache

open class LRUCacheMap<T, M>(private var maxElements: Int = 0) : LinkedHashMap<T, M>(maxElements) {
    override fun removeEldestEntry(eldest: Map.Entry<T, M>) = (this.size > this.maxElements)
}