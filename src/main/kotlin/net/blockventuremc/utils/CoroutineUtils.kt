package net.blockventuremc.utils

import kotlinx.coroutines.*
import net.blockventuremc.BlockVenture
import org.bukkit.Bukkit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * mcroutine guarantees execution on the server thread.
 * True blocking is illegal, see [mcasync] for options.
 */
fun <T> mcroutine(coroutine: suspend () -> T) {
    CoroutineScope(Dispatchers.mc).launch {
        coroutine()
    }
}

/**
 * mcasync guarantees execution away from the server thread.
 * True blocking is OK, mcasync uses an Unbound thread-pool, so it's
 * not an option for CPU bound tasks.
 */
suspend fun <T> mcasync(coroutine: suspend () -> T): T {
    return withContext(Dispatchers.async) {
        try {
            coroutine()
        } catch (e: Exception) {
            throw e
        }
    }
}


val globalPool: ExecutorService = Executors.newCachedThreadPool()

object MinecraftCoroutineDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getServer().scheduler.runTask(BlockVenture.instance, block)
            return
        }
        block.run()
    }
}

object AsyncCoroutineDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            globalPool.execute(block)
            return
        }
        block.run()
    }
}

val Dispatchers.async: CoroutineContext
    get() = AsyncCoroutineDispatcher

val Dispatchers.mc: CoroutineContext
    get() = MinecraftCoroutineDispatcher