package ch.hippmann.godot.utilities.coroutines.scope

import ch.hippmann.godot.utilities.coroutines.defaultDispatcher
import ch.hippmann.godot.utilities.coroutines.mainDispatcher
import godot.Node
import godot.Object
import godot.annotation.RegisterFunction
import godot.core.Callable
import godot.core.StringName
import godot.core.asStringName
import godot.global.GD
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
import godot.util.camelToSnakeCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface GodotCoroutineScope : CoroutineScope {
    fun initSignalAwait(owner: Node)
    fun Node.resumeGodotContinuations()
    suspend fun Node.withGodotContext(block: () -> Unit)

    suspend fun Signal.await(): Array<Any?>

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback0Args(signalName: String)

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback1Args(arg0: Any?, signalName: String)

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback2Args(arg0: Any?, arg1: Any?, signalName: String)

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback3Args(arg0: Any?, arg1: Any?, arg2: Any?, signalName: String)

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback4Args(arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, signalName: String)

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback5Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        signalName: String,
    )

    @RegisterFunction
    fun godotCoroutineScopeSignalCallback6Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        signalName: String,
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
        signalName: String,
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
        signalName: String,
    )

    @RegisterFunction
    fun godotCoroutineScopeTriggerContinuations()
}

@Suppress("unused")
class DefaultGodotCoroutineScope : GodotCoroutineScope {
    private var owner: WeakReference<Node>? = null
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

    override fun initSignalAwait(owner: Node) {
        this.owner = WeakReference(owner)
        val callable = Callable(
            target = owner,
            methodName = ::godotCoroutineScopeTriggerContinuations.name.camelToSnakeCase().asStringName(),
        )
        owner.treeEntered.connect(callable)
        owner.ready.connect(callable)
        owner.treeExiting.connect(callable)
        owner.treeExited.connect(callable)
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


    private data class AwaitDataContainer(
        val continuation: Continuation<Array<Any?>>,
        val callable: Callable,
        val signalName: StringName,
    )

    private val continuationMapLock = Mutex()

    // callable as well only to keep a strong ref to it. Get's cleared later
    private val continuationMap: MutableMap<String, List<AwaitDataContainer>> = mutableMapOf()

    override suspend fun Signal.await(): Array<Any?> {
        val owner = this@DefaultGodotCoroutineScope.owner?.get()
            ?: throw IllegalStateException("There is not owner set! Did you forget to call initSignalAwait in the constructor?")
        val signalName = this.name.toString()
        val callable = provideCallable(owner, signalName)

        return suspendCoroutine { continuation ->
            runBlocking {
                continuationMapLock.withLock {
                    val key = "${owner.id}_${signalName}_${callable.getMethod()}"
                    val dataList = continuationMap[key]?.toMutableList() ?: mutableListOf()
                    continuationMap[key] = dataList.apply {
                        add(
                            AwaitDataContainer(
                                continuation = continuation,
                                callable = callable,
                                signalName = this@await.name,
                            )
                        )
                    }
                }

                withContext(mainDispatcher()) {
                    owner.withGodotContext {
                        owner.connect(
                            this@await.name,
                            callable,
                            Object.ConnectFlags.CONNECT_REFERENCE_COUNTED.id,
                        )
                    }
                }

//                owner.callDeferred(
//                    "connect".asStringName(),
//                    this@await.name,
//                    callable,
//                    Object.ConnectFlags.CONNECT_REFERENCE_COUNTED.id.toInt()
//                )
                GD.print("Did connect")
            }
        }
    }

    private fun Signal.provideCallable(owner: Node, signalName: String): Callable {
        val function = when (this) {
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
        }
        return Callable(
            target = owner,
            methodName = function.name.camelToSnakeCase().asStringName()
        ).bind(signalName)
    }

    override fun godotCoroutineScopeSignalCallback0Args(signalName: String) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(),
            signalName = signalName,
            callableName = ::godotCoroutineScopeSignalCallback0Args.name.camelToSnakeCase(),
        )
    }

    override fun godotCoroutineScopeSignalCallback1Args(arg0: Any?, signalName: String) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
            ),
            signalName = signalName,
            callableName = ::godotCoroutineScopeSignalCallback1Args.name.camelToSnakeCase(),
        )
    }

    override fun godotCoroutineScopeSignalCallback2Args(arg0: Any?, arg1: Any?, signalName: String) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
                arg1,
            ),
            signalName = signalName,
            callableName = ::godotCoroutineScopeSignalCallback2Args.name.camelToSnakeCase(),
        )
    }

    override fun godotCoroutineScopeSignalCallback3Args(arg0: Any?, arg1: Any?, arg2: Any?, signalName: String) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
                arg1,
                arg2,
            ),
            signalName = signalName,
            callableName = ::godotCoroutineScopeSignalCallback3Args.name.camelToSnakeCase(),
        )
    }

    override fun godotCoroutineScopeSignalCallback4Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        signalName: String,
    ) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
                arg1,
                arg2,
                arg3,
            ),
            signalName = signalName,
            callableName = ::godotCoroutineScopeSignalCallback4Args.name.camelToSnakeCase(),
        )
    }

    override fun godotCoroutineScopeSignalCallback5Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        signalName: String,
    ) {
        godotCoroutineScopeSignalCallback(
            args = arrayOf(
                arg0,
                arg1,
                arg2,
                arg3,
                arg4,
            ),
            signalName = signalName,
            callableName = ::godotCoroutineScopeSignalCallback5Args.name.camelToSnakeCase(),
        )
    }

    override fun godotCoroutineScopeSignalCallback6Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        signalName: String,
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
            signalName = signalName,
            callableName = ::godotCoroutineScopeSignalCallback6Args.name.camelToSnakeCase(),
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
        signalName: String,
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
            signalName = signalName,
            callableName = ::godotCoroutineScopeSignalCallback7Args.name.camelToSnakeCase(),
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
        signalName: String,
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
            signalName = signalName,
            callableName = ::godotCoroutineScopeSignalCallback8Args.name.camelToSnakeCase(),
        )
    }

    override fun godotCoroutineScopeTriggerContinuations() {
        owner?.get()?.resumeGodotContinuations()
    }

    private fun godotCoroutineScopeSignalCallback(args: Array<Any?>, signalName: String, callableName: String) {
        GD.print("Received signal emition")
        runBlocking {
            val owner = owner?.get() ?: return@runBlocking
            val key = "${owner.id}_${signalName}_${callableName}"
            val awaitDataContainers = continuationMapLock.withLock {
                continuationMap.remove(key)
            } ?: return@runBlocking

            awaitDataContainers.forEach { awaitDataContainer ->
                awaitDataContainer.continuation.resume(args)

                if (owner.isConnected(awaitDataContainer.signalName, awaitDataContainer.callable)) {
                    owner.disconnect(awaitDataContainer.signalName, awaitDataContainer.callable)
                }
            }
        }
    }
}

private typealias GodotContinuation = Continuation<Unit>
private typealias GodotContinuationWithBlock = Pair<Continuation<Unit>, () -> Unit>

private fun GodotContinuation.resume() = resume(Unit)