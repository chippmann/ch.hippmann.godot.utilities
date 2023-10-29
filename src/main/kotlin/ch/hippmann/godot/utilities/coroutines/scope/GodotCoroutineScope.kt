package ch.hippmann.godot.utilities.coroutines.scope

import ch.hippmann.godot.utilities.coroutines.defaultDispatcher
import ch.hippmann.godot.utilities.coroutines.mainDispatcher
import godot.Control
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface GodotCoroutineScope: CoroutineScope {
    fun Control.resumeUiContinuations()
    suspend fun Control.withUiContext(block: () -> Unit)
}

class DefaultGodotCoroutineScope: GodotCoroutineScope {
    private val uiContinuationsMutex = Mutex()
    private val uiContinuations: Queue<UiContinuationWithBlock> = LinkedList()

    override val coroutineContext: CoroutineContext = defaultDispatcher() + SupervisorJob() + object : CoroutineExceptionHandler {
        override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler
        override fun handleException(context: CoroutineContext, exception: Throwable) {
            throw exception
        }
    }

    private suspend fun addUiContinuation(continuation: UiContinuation, block: () -> Unit) {
        uiContinuationsMutex.withLock {
            uiContinuations.add(continuation to block)
        }
    }

    override fun Control.resumeUiContinuations() {
        runBlocking {
            uiContinuationsMutex.withLock {
                while (uiContinuations.isNotEmpty()) {
                    val (continuation, block) = uiContinuations.remove()
                    withContext(mainDispatcher()) {
                        block()
                    }
                    continuation.resume()
                }
            }
        }
    }

    override suspend fun Control.withUiContext(block: () -> Unit) {
        suspendCoroutine { continuation ->
            runBlocking {
                addUiContinuation(continuation, block)
            }
        }
    }
}

private typealias UiContinuation = Continuation<Unit>
private typealias UiContinuationWithBlock = Pair<Continuation<Unit>, () -> Unit>
private fun UiContinuation.resume() = resume(Unit)