package net.blockventuremc.modules.customblockdata

import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.util.BlockVector
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Represents a PersistentDataContainer for a specific Block. Also includes some general utility methods
 * that can be applied to every PersistentDataContainer.
 */
class CustomBlockData(
    private val block: Block,
    private val plugin: Plugin
) : PersistentDataContainer {

    companion object {
        private val DIRTY_BLOCKS: MutableSet<Map.Entry<UUID, BlockVector>> = HashSet()
        private val PRIMITIVE_DATA_TYPES: Array<PersistentDataType<*, *>> = arrayOf(
            PersistentDataType.BYTE,
            PersistentDataType.SHORT,
            PersistentDataType.INTEGER,
            PersistentDataType.LONG,
            PersistentDataType.FLOAT,
            PersistentDataType.DOUBLE,
            PersistentDataType.STRING,
            PersistentDataType.BYTE_ARRAY,
            PersistentDataType.INTEGER_ARRAY,
            PersistentDataType.LONG_ARRAY,
            PersistentDataType.TAG_CONTAINER_ARRAY,
            PersistentDataType.TAG_CONTAINER
        )
        val PERSISTENCE_KEY = Objects.requireNonNull(
            NamespacedKey.fromString("customblockdata:protected"),
            "Could not create persistence NamespacedKey"
        )
        private val KEY_REGEX: Pattern = Pattern.compile("^x(\\d+)y(-?\\d+)z(\\d+)$")
        const val CHUNK_MIN_XZ = 0
        const val CHUNK_MAX_XZ = (2 shl 3) - 1

        private fun getBlockEntry(block: Block): Map.Entry<UUID, BlockVector> {
            val uuid = block.world.uid
            val blockVector = BlockVector(block.x.toDouble(), block.y.toDouble(), block.z.toDouble())
            return AbstractMap.SimpleEntry(uuid, blockVector)
        }

        fun isDirty(block: Block): Boolean {
            return DIRTY_BLOCKS.contains(getBlockEntry(block))
        }

        fun setDirty(plugin: Plugin, blockEntry: Map.Entry<UUID, BlockVector>) {
            if (!plugin.isEnabled) { // checks if the plugin is disabled to prevent the IllegalPluginAccessException
                return
            }
            DIRTY_BLOCKS.add(blockEntry)
            Bukkit.getScheduler().runTask(plugin, Runnable { DIRTY_BLOCKS.remove(blockEntry) })
        }

        private fun getKey(plugin: Plugin, block: Block): NamespacedKey {
            return NamespacedKey(plugin, getKey(block))
        }

        fun getKey(block: Block): String {
            val x = block.x and 0x000F
            val y = block.y
            val z = block.z and 0x000F
            return "x$x" + "y$y" + "z$z"
        }

        private fun getBlockFromKey(key: NamespacedKey, chunk: Chunk): Block? {
            val matcher: Matcher = KEY_REGEX.matcher(key.key)
            if (!matcher.matches()) return null
            val x = matcher.group(1).toInt()
            val y = matcher.group(2).toInt()
            val z = matcher.group(3).toInt()
            return if (x < CHUNK_MIN_XZ || x > CHUNK_MAX_XZ || z < CHUNK_MIN_XZ || z > CHUNK_MAX_XZ) null
            else chunk.getBlock(x, y, z)
        }

        fun hasCustomBlockData(block: Block, plugin: Plugin): Boolean {
            return block.chunk.persistentDataContainer.has(getKey(plugin, block), PersistentDataType.TAG_CONTAINER)
        }

        fun getBlocksWithCustomData(plugin: Plugin, chunk: Chunk): Collection<Block> {
            val dummy = NamespacedKey(plugin, "dummy")
            return getBlocksWithCustomData(chunk, dummy)
        }

        private fun getBlocksWithCustomData(chunk: Chunk, namespace: NamespacedKey): List<Block> {
            val chunkPDC: PersistentDataContainer = chunk.persistentDataContainer
            return chunkPDC.keys.filter { key: NamespacedKey ->
                key.namespace == namespace.namespace
            }.map { key: NamespacedKey ->
                getBlockFromKey(key, chunk)
            }.filterNotNull().toList()
        }

        fun getBlocksWithCustomData(namespace: String, chunk: Chunk): List<Block> {
            val dummy = NamespacedKey(namespace, "dummy")
            return getBlocksWithCustomData(chunk, dummy)
        }

        fun getDataType(pdc: PersistentDataContainer, key: NamespacedKey): PersistentDataType<*, *>? {
            for (dataType in PRIMITIVE_DATA_TYPES) {
                if (pdc.has(key, dataType)) return dataType
            }
            return null
        }
    }

    private val chunk: Chunk = block.chunk

    private val pdc: PersistentDataContainer

    private val blockEntry: Map.Entry<UUID, BlockVector> = getBlockEntry(block)

    init {
        pdc = getPersistentDataContainer()
    }

    fun getBlock(): Block? {
        val world = Bukkit.getWorld(blockEntry.key)
        val vector: BlockVector = blockEntry.value
        return world?.getBlockAt(vector.blockX.toInt(), vector.blockY.toInt(), vector.blockZ.toInt())
    }

    private fun getPersistentDataContainer(): PersistentDataContainer {
        val chunkPDC: PersistentDataContainer = chunk.persistentDataContainer
        val blockPDC: PersistentDataContainer? = chunkPDC.get(getKey(plugin, block), PersistentDataType.TAG_CONTAINER)
        return blockPDC ?: chunkPDC.adapterContext.newPersistentDataContainer()
    }

    fun clear() {
        pdc.keys.forEach(pdc::remove)
        save()
    }

    private fun save() {
        setDirty(plugin, blockEntry)
        if (pdc.isEmpty) {
            chunk.persistentDataContainer.remove(getKey(plugin, block))
        } else {
            chunk.persistentDataContainer.set(getKey(plugin, block), PersistentDataType.TAG_CONTAINER, pdc)
        }
    }

    @Suppress("UNCHECKED_CAST", "ConstantConditions")
    fun copyTo(block: Block, plugin: Plugin) {
        val newCbd = CustomBlockData(block, plugin)
        keys.forEach { key ->
            if (key == null) return@forEach
            val dataType: PersistentDataType<*, *>? = getDataType(this, key)
            if (dataType != null) {
                newCbd.set(
                    key,
                    dataType as PersistentDataType<Any, Any>,
                    this[key, dataType] as Any
                )
            }
        }
    }

    override fun <T, Z> set(key: NamespacedKey, type: PersistentDataType<T, Z>, value: Z & Any) {
        pdc.set(key, type, value)
        save()
    }

    override fun <T, Z> has(key: NamespacedKey, type: PersistentDataType<T, Z>): Boolean {
        return pdc.has(key, type)
    }

    override fun has(key: NamespacedKey): Boolean {
        for (type in PRIMITIVE_DATA_TYPES) {
            if (pdc.has(key, type)) return true
        }
        return false
    }

    override fun <T, Z> get(key: NamespacedKey, type: PersistentDataType<T, Z>): Z? {
        return pdc[key, type]
    }

    override fun <T, C> getOrDefault(
        key: NamespacedKey,
        type: PersistentDataType<T & Any, C & Any>,
        defaultValue: C & Any
    ): C & Any {
        return pdc.getOrDefault(key, type, defaultValue)
    }

    override fun getKeys(): Set<NamespacedKey?> {
        return pdc.keys
    }

    override fun remove(key: NamespacedKey) {
        pdc.remove(key)
        save()
    }

    override fun isEmpty(): Boolean {
        return pdc.isEmpty
    }

    override fun copyTo(pdc: PersistentDataContainer, replace: Boolean) {
        this.pdc.copyTo(pdc, replace)
    }

    override fun getAdapterContext(): PersistentDataAdapterContext {
        return pdc.adapterContext
    }

    override fun serializeToBytes(): ByteArray {
        return pdc.serializeToBytes()
    }

    override fun readFromBytes(bytes: ByteArray, clear: Boolean) {
        pdc.readFromBytes(bytes, clear)
    }

    fun getDataType(key: NamespacedKey): PersistentDataType<*, *>? {
        return getDataType(pdc, key)
    }
}