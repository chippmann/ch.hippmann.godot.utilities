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
    private val uiContinuationsMutex = Mutex()
    private val uiContinuations: Queue<GodotContinuationWithBlock> = LinkedList()

    override val coroutineContext: CoroutineContext =
        defaultDispatcher() + SupervisorJob() + object : CoroutineExceptionHandler {
            override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler
            override fun handleException(context: CoroutineContext, exception: Throwable) {
                throw exception
            }
        }

    private suspend fun addUiContinuation(continuation: GodotContinuation, block: () -> Unit) {
        uiContinuationsMutex.withLock {
            uiContinuations.add(continuation to block)
        }
    }

    override fun Node.resumeGodotContinuations() {
        runBlocking {
            uiContinuationsMutex.withLock {
                while (uiContinuations.isNotEmpty()) {
                    val (continuation, block) = uiContinuations.remove()
                    block()
                    continuation.resume()
                }
            }
        }
    }

    override suspend fun Node.withGodotContext(block: () -> Unit) {
        suspendCoroutine { continuation ->
            runBlocking {
                addUiContinuation(continuation, block)
            }
        }
    }
}