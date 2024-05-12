package ch.hippmann.godot.utilities.coroutines.scope

import ch.hippmann.godot.utilities.coroutines.defaultDispatcher
import ch.hippmann.godot.utilities.coroutines.mainDispatcher
import godot.Control
import godot.Object
import godot.annotation.RegisterFunction
import godot.core.Callable
import godot.core.asStringName
import godot.signals.Signal
import godot.signals.Signal0
import godot.signals.Signal1
import godot.signals.Signal2
import godot.signals.Signal3
import godot.signals.Signal4
import godot.signals.Signal5
import godot.signals.Signal6
import godot.signals.Signal7
import godot.signals.Signal8
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

interface GodotCoroutineScope : CoroutineScope {
    fun Control.resumeGodotContinuations()
    suspend fun Control.withGodotContext(block: () -> Unit)

    suspend fun Signal.await(): Array<Any?>

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback0Args(callableId: String)

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback1Args(arg0: Any?, callableId: String)

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback2Args(arg0: Any?, arg1: Any?, callableId: String)

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback3Args(arg0: Any?, arg1: Any?, arg2: Any?, callableId: String)

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback4Args(arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, callableId: String)

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback5Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        callableId: String
    )

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback6Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        callableId: String
    )

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback7Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        arg6: Any?,
        callableId: String
    )

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback8Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        arg6: Any?,
        arg7: Any?,
        callableId: String
    )
}

class DefaultGodotCoroutineScope(private var owner: Object) : GodotCoroutineScope {
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

    override fun Control.resumeGodotContinuations() {
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

    override suspend fun Control.withGodotContext(block: () -> Unit) {
        suspendCoroutine { continuation ->
            runBlocking {
                addUiContinuation(continuation, block)
            }
        }
    }


    private val continuationMapLock = Mutex()
    private val continuationMap: MutableMap<String, Continuation<Array<Any?>>> = mutableMapOf()

    override suspend fun Signal.await(): Array<Any?> {
        val callableId = UUID.randomUUID().toString()
        val callable = provideCallback(callableId)
        return continuationMapLock.withLock {
            suspendCoroutine { continuation ->
                continuationMap[callableId] = continuation
                this.connect(callable, Object.ConnectFlags.CONNECT_ONE_SHOT.id.toInt())
            }
        }
    }

    private fun Signal.provideCallback(callableId: String): Callable {
        return Callable(
            target = owner,
            methodName = when (this) {
                is Signal0 -> ::godotCoroutineScopeSignalCallback0Args
                is Signal1<*> -> ::godotCoroutineScopeSignalCallback1Args
                is Signal2<*, *> -> ::godotCoroutineScopeSignalCallback2Args
                is Signal3<*, *, *> -> ::godotCoroutineScopeSignalCallback3Args
                is Signal4<*, *, *, *> -> ::godotCoroutineScopeSignalCallback4Args
                is Signal5<*, *, *, *, *> -> ::godotCoroutineScopeSignalCallback5Args
                is Signal6<*, *, *, *, *, *> -> ::godotCoroutineScopeSignalCallback6Args
                is Signal7<*, *, *, *, *, *, *> -> ::godotCoroutineScopeSignalCallback7Args
                is Signal8<*, *, *, *, *, *, *, *> -> ::godotCoroutineScopeSignalCallback8Args
                else -> throw IllegalArgumentException("await can only handle signals with at most 8 arguments!")
            }.name.asStringName()
        ).bind(callableId)
    }

    override fun godotCoroutineScopeSignalCallback0Args(callableId: String) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(),
            callableId = callableId
        )
    }

    override fun godotCoroutineScopeSignalCallback1Args(arg0: Any?, callableId: String) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
            ),
            callableId = callableId
        )
    }

    override fun godotCoroutineScopeSignalCallback2Args(arg0: Any?, arg1: Any?, callableId: String) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
                arg1,
            ),
            callableId = callableId
        )
    }

    override fun godotCoroutineScopeSignalCallback3Args(arg0: Any?, arg1: Any?, arg2: Any?, callableId: String) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
                arg1,
                arg2,
            ),
            callableId = callableId
        )
    }

    override fun godotCoroutineScopeSignalCallback4Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        callableId: String
    ) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
                arg1,
                arg2,
                arg3,
            ),
            callableId = callableId
        )
    }

    override fun godotCoroutineScopeSignalCallback5Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        callableId: String
    ) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
                arg1,
                arg2,
                arg3,
                arg4,
            ),
            callableId = callableId
        )
    }

    override fun godotCoroutineScopeSignalCallback6Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        callableId: String
    ) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
                arg1,
                arg2,
                arg3,
                arg4,
                arg5,
            ),
            callableId = callableId
        )
    }

    override fun godotCoroutineScopeSignalCallback7Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        arg6: Any?,
        callableId: String
    ) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
                arg1,
                arg2,
                arg3,
                arg4,
                arg5,
                arg6,
            ),
            callableId = callableId
        )
    }

    override fun godotCoroutineScopeSignalCallback8Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        arg6: Any?,
        arg7: Any?,
        callableId: String
    ) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
                arg1,
                arg2,
                arg3,
                arg4,
                arg5,
                arg6,
                arg7,
            ),
            callableId = callableId
        )
    }

    private fun godotCoroutineScopeSignalCallback(args: Array<Any?>, callableId: String) {
        runBlocking {
            continuationMapLock.withLock {
                continuationMap.remove(callableId)
            }?.resume(args)
        }
    }
}

private typealias GodotContinuation = Continuation<Unit>
private typealias GodotContinuationWithBlock = Pair<Continuation<Unit>, () -> Unit>

private fun GodotContinuation.resume() = resume(Unit)