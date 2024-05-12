package net.blockventuremc.cache

open class LRUCacheList<T>(private var maxElements: Int = 0) : LinkedHashSet<T>(maxElements) {
    override fun add(element: T): Boolean {
        if (this.size >= this.maxElements) {
            this.remove(this.first())
        }
        return super.add(element)
    }
}