package ch.hippmann.godot.utilities.coroutines.scope

import ch.hippmann.godot.utilities.coroutines.defaultDispatcher
import godot.Node
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

@Suppress("unused")
class DefaultGodotCoroutineScope : GodotCoroutineScope {
    private val godotContinuationsMutex = Mutex()
    private val godotContinuations: Queue<GodotContinuationWithBlock> = LinkedList()

    override val coroutineContext: CoroutineContext =
        defaultDispatcher() + SupervisorJob() + object : CoroutineExceptionHandler {
            override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler
            override fun handleException(context: CoroutineContext, exception: Throwable) {
                throw exception
            }
        }

    private suspend fun addGodotContinuation(continuation: GodotContinuation, block: () -> Unit) {
        godotContinuationsMutex.withLock {
            godotContinuations.add(continuation to block)
        }
    }

    override fun Node.resumeGodotContinuations() {
        runBlocking {
            godotContinuationsMutex.withLock {
                while (godotContinuations.isNotEmpty()) {
                    val (continuation, block) = godotContinuations.remove()
                    block()
                    continuation.resume()
                }
            }
        }
    }

    override suspend fun Node.withGodotContext(block: () -> Unit) {
        suspendCoroutine { continuation ->
            runBlocking {
                addGodotContinuation(continuation, block)
            }
        }
    }
}