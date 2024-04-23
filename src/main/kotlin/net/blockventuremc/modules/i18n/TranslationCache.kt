package net.blockventuremc.modules.i18n

import com.google.gson.Gson
import dev.fruxz.ascend.extension.getResourceOrNull
import dev.fruxz.ascend.extension.logging.getItsLogger
import net.blockventuremc.modules.i18n.model.Translation
import java.io.File
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.time.measureTime

/**
 * The TranslationCache class is responsible for caching translations and providing access to
 * cached translations in a thread-safe manner.
 */
object TranslationCache {

    /**
     * The Gson instance used for JSON serialization and deserialization.
     */
    private val gson = Gson()

    /**
     * The fallback language to be used when a translation is not found for a given language code.
     * The fallback language is represented by a dash-combined ISO-639 (language) and ISO-3166 (country) code.
     * If no translation is found for the specified language code, the system will attempt to find the translation in the fallback language.
     */
    private const val FALLBACK_LANGUAGE = "en-US"

    /**
     * This variable represents a ReadWriteLock object named 'lock'. It is used for managing concurrent access to shared resources.
     * The ReadWriteLock allows multiple threads to read the resource concurrently, but only one thread can write to the resource at a time.
     */
    private val lock: ReadWriteLock = ReentrantReadWriteLock()

    /**
     * Represents a cache for storing translations.
     *
     * The cache is a map where the language code is the key and a list of translations is the value.
     *
     * @property cache The map representing the cache. It is initially empty.
     */
    private var cache: Map<String, List<Translation>> = emptyMap()


    private val translationFolder = this::class.java.getResource("/translations")?.path?.let { Path(it) }


    /**
     * Loads all translations from the database and updates the cache.
     * This method acquires a write lock to ensure thread safety when updating the cache.
     * After the cache is updated, it logs the number of translations loaded and the time taken to load them.
     */
    fun loadAll() {
        val loadTimer = measureTime {
            try {
                lock.writeLock().lock()
                cache = loadAllTranslations()
            } finally {
                lock.writeLock().unlock()
            }
        }
        getItsLogger().info("Loaded ${cache.size} languages with a total of ${getTotalTranslations()} translations in $loadTimer.")
    }

    private fun loadAllTranslations(): Map<String, List<Translation>> {
        if (translationFolder == null || !translationFolder.isDirectory()) {
            println("translations folder not found!")
            return emptyMap()
        }

        if (translationFolder.listDirectoryEntries().isEmpty()) {
            println("translations folder is empty!")
            return emptyMap()
        }

        val translations = mutableMapOf<String, List<Translation>>()
        val translationFiles = translationFolder.listDirectoryEntries().filter { it.fileName.toString().endsWith(".json") }
        var totalTranslations = 0
        val allTranslationTime = measureTime {
            for (translationFile in translationFiles) {
                val langTranslations = mutableListOf<Translation>()
                val languageCode = translationFile.fileName.toString().removeSuffix(".json")
                val englishTranslations = gson.fromJson(translationFile.readText(), Map::class.java)
                getItsLogger().info("Migrating ${englishTranslations.size} $languageCode translations...")
                val translationTime = measureTime {
                    englishTranslations.forEach { (key, value) ->
                        langTranslations.add(Translation(messageKey = key.toString(), message = value.toString()))
                        totalTranslations++
                    }
                }
                translations[languageCode] = langTranslations
                getItsLogger().info("Loaded ${englishTranslations.size} $languageCode translations in ${translationTime}.")
            }
        }
        getItsLogger().info("Loaded ${translations.size} languages with a total of $totalTranslations translations in $allTranslationTime.")
        return translations
    }

    /**
     * Returns the list of available languages.
     *
     * @return The list of languages.
     */
    val languages: List<String>
        get() = cache.keys.toList()

    /**
     * Returns the total number of translations in the cache.
     *
     * This method acquires a read lock on the 'lock' object, allowing multiple threads
     * to read the cache concurrently.
     *
     * @return The total number of translations in the cache.
     */
    private fun getTotalTranslations(): Long {
        try {
            lock.readLock().lock()
            return cache.values.sumOf { it.size }.toLong()
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Retrieves all translations from the cache.
     *
     * @return A map containing language code as the key and a list of translations as the value.
     */
    fun getAll(): Map<String, List<Translation>> {
        try {
            lock.readLock().lock()
            return cache
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Retrieves the list of translations for the given language code.
     *
     * @param languageCode The language code in dash-combined ISO-639 (language) and ISO-3166 (country) format.
     * @return The list of translations for the given language code, or null if no translations are available.
     */
    private fun get(languageCode: String): List<Translation>? {
        try {
            lock.readLock().lock()
            return cache[languageCode]
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Retrieves translations for a given message key.
     *
     * @param messageKey The key identifying the message for which translations are requested.
     * @return A map containing translations for each language code.
     */
    fun getTranslationsFor(messageKey: String): Map<String, Translation?> {
        val translations = mutableMapOf<String, Translation?>()
        for (languageCode in languages) {
            translations[languageCode] = get(languageCode, messageKey)
        }
        return translations
    }

    /**
     * Retrieves the translation for the given language code and message key.
     *
     * @param languageCode The language code of the translation as dash-combined ISO-639 (language) and ISO-3166 (country).
     * @param messageKey The key identifying the message.
     * @param placeholders The placeholders to be replaced in the message.
     * @return The translation matching the language code and message key, or null if not found.
     */
    fun get(languageCode: String, messageKey: String, placeholders: Map<String, Any?> = emptyMap()): Translation? {
        try {
            lock.readLock().lock()
            var message = cache[languageCode]?.find { it.messageKey == messageKey } ?: cache[FALLBACK_LANGUAGE]?.find { it.messageKey == messageKey }

            if (message == null) {
                getItsLogger().info("No translation found for $languageCode:$messageKey")
                return null
            }

            for ((key, value) in placeholders) {
                message = message?.copy(message = message.message.replace("%$key%", value.toString()))
            }

            return message
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Adds a translation to the cache for a given name.
     *
     * @param name The name of the translation.
     * @param translation The list of Translation objects representing the translations of a message in different languages.
     */
    fun put(name: String, translation: List<Translation>) {
        try {
            lock.writeLock().lock()
            cache = cache + (name to translation)
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Removes the specified name from the cache.
     *
     * This method acquires a write lock on the cache and removes the specified name from it. If the name is not present in the cache, no action is taken.
     *
     * @param name the name to be removed from the cache
     */
    fun remove(name: String) {
        try {
            lock.writeLock().lock()
            cache = cache - name
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Clears the cache.
     *
     * This method acquires a write lock on the lock associated with this cache,
     * then sets the cache to an empty map, effectively clearing it.
     * Finally, it releases the write lock.
     *
     * @throws Exception if acquiring or releasing the write lock fails
     */
    fun clear() {
        try {
            lock.writeLock().lock()
            cache = emptyMap()
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Checks if the given name exists in the cache.
     *
     * @param name the name to check for existence in the cache
     * @return true if the name exists in the cache, false otherwise
     */
    fun contains(name: String): Boolean {
        try {
            lock.readLock().lock()
            return cache.containsKey(name)
        } finally {
            lock.readLock().unlock()
        }
    }
}