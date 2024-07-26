package ch.hippmann.godot.utilities.coroutines.await

import ch.hippmann.godot.utilities.coroutines.mainDispatcher
import ch.hippmann.godot.utilities.coroutines.scope.DefaultGodotCoroutineScope
import ch.hippmann.godot.utilities.coroutines.scope.GodotCoroutineScope
import ch.hippmann.godot.utilities.logging.Log
import godot.Node
import godot.OS
import godot.Object
import godot.core.Callable
import godot.core.StringName
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
import godot.util.camelToSnakeCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private data class AwaitDataContainer(
    val continuation: Continuation<Array<Any?>>,
    val callable: Callable,
    val signalName: StringName,
) {
    fun assembleKey(owner: Node): String = "${owner.id}_${signalName}_${callable.getMethod()}"
}

@Suppress("unused")
class SignalAwaiter(
    private val printDebug: Boolean = OS.isDebugBuild(),
) : SignalAwaitable, GodotCoroutineScope by DefaultGodotCoroutineScope() {
    private var owner: WeakReference<Node>? = null

    override fun <T : Node> T.initSignalAwait() {
        this@SignalAwaiter.owner = WeakReference(this)
        val callable = Callable(
            target = this,
            methodName = ::signalAwaitableTriggerContinuations.name.camelToSnakeCase().asStringName(),
        )
        this.treeEntered.connect(callable)
        this.ready.connect(callable)
        this.treeExiting.connect(callable)
        this.treeExited.connect(callable)
    }

    private val continuationMapLock = Mutex()
    private val continuationMap: MutableMap<String, List<AwaitDataContainer>> = mutableMapOf()

    override suspend fun Signal.await(): Array<Any?> {
        val owner = this@SignalAwaiter.owner?.get() ?: run {
            if (OS.isDebugBuild()) {
                throw IllegalStateException("There is not owner set! Did you forget to call initSignalAwait in the constructor?")
            } else {
                Log.err { "There is not owner set! Did you forget to call initSignalAwait in the constructor? Returning empty array immediately and will not await anything" }
            }
            return arrayOf()
        }

        // directly emit
        when {
            this.name == owner.treeEntered.name && owner.isInsideTree() -> return arrayOf<Any?>().also {
                debugLog { "Wanted to wait for signal ${owner::treeEntered.name} but node is already in tree. Will continue straight away" }
            }

            this.name == owner.ready.name && owner.isNodeReady() -> return arrayOf<Any?>().also {
                debugLog { "Wanted to wait for signal ${owner::ready.name} but node is already in ready state. Will continue straight away" }
            }

            this.name == owner.treeExiting.name && !owner.isInsideTree() -> return arrayOf<Any?>().also {
                debugLog { "Wanted to wait for signal ${owner::treeExiting.name} but node is not in tree. Will continue straight away" }
            }

            this.name == owner.treeExited.name && !owner.isNodeReady() -> return arrayOf<Any?>().also {
                debugLog { "Wanted to wait for signal ${owner::treeExited.name} but node is not in tree. Will continue straight away" }
            }
        }

        val signalName = this.name.toString()
        val callable = provideCallable(owner, signalName)

        return suspendCoroutine { continuation ->
            runBlocking {
                continuationMapLock.withLock {
                    val key = assembleKey(owner, signalName, callable.getMethod().toString())
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
                        debugLog { "Connected signal ${this@await.name} to callable: ${callable.getObject()}::${callable.getMethod()}" }
                    }
                }
            }
        }
    }

    private fun Signal.provideCallable(owner: Node, signalName: String): Callable {
        val function = when (this) {
            is Signal0 -> ::signalAwaitableSignalCallback0Args
            is Signal1<*> -> ::signalAwaitableSignalCallback1Args
            is Signal2<*, *> -> ::signalAwaitableSignalCallback2Args
            is Signal3<*, *, *> -> ::signalAwaitableSignalCallback3Args
            is Signal4<*, *, *, *> -> ::signalAwaitableSignalCallback4Args
            is Signal5<*, *, *, *, *> -> ::signalAwaitableSignalCallback5Args
            is Signal6<*, *, *, *, *, *> -> ::signalAwaitableSignalCallback6Args
            is Signal7<*, *, *, *, *, *, *> -> ::signalAwaitableSignalCallback7Args
            is Signal8<*, *, *, *, *, *, *, *> -> ::signalAwaitableSignalCallback8Args
            else -> throw IllegalArgumentException("await can only handle signals with at most 8 arguments!")
        }
        return Callable(
            target = owner,
            methodName = function.name.camelToSnakeCase().asStringName()
        ).bind(signalName)
    }

    private fun signalAwaitableSignalCallback(args: Array<Any?>, signalName: String, callableName: String) {
        debugLog { "Received signal emition from signal: $signalName for callable: $callableName. Provided args: $args" }
        runBlocking {
            val owner = owner?.get() ?: run {
                Log.err { "There is not owner present anymore! No op" }
                return@runBlocking
            }
            val key = assembleKey(owner, signalName, callableName)
            val awaitDataContainers = continuationMapLock.withLock {
                continuationMap.remove(key)
            } ?: run {
                Log.err { "No await data found for key $key" }
                return@runBlocking
            }

            awaitDataContainers.forEach { awaitDataContainer ->
                debugLog { "Await data found: $awaitDataContainer. Resuming continuation" }
                awaitDataContainer.continuation.resume(args)

                if (owner.isConnected(awaitDataContainer.signalName, awaitDataContainer.callable)) {
                    debugLog { "Disconnecting signal for await data: $awaitDataContainer." }
                    owner.disconnect(awaitDataContainer.signalName, awaitDataContainer.callable)
                }
            }
        }
    }

    private inline fun debugLog(message: () -> String) {
        if (printDebug) {
            Log.debug(message = message)
        }
    }

    private fun assembleKey(owner: Node, signalName: String, callableName: String) =
        "${owner.id}_${signalName}_${callableName}"

    // START: await callback registered functions
    override fun signalAwaitableSignalCallback0Args(signalName: String) {
        signalAwaitableSignalCallback(
            args = arrayOf(),
            signalName = signalName,
            callableName = ::signalAwaitableSignalCallback0Args.name.camelToSnakeCase(),
        )
    }

    override fun signalAwaitableSignalCallback1Args(arg0: Any?, signalName: String) {
        signalAwaitableSignalCallback(
            args = arrayOf(arg0),
            signalName = signalName,
            callableName = ::signalAwaitableSignalCallback1Args.name.camelToSnakeCase(),
        )
    }

    override fun signalAwaitableSignalCallback2Args(arg0: Any?, arg1: Any?, signalName: String) {
        signalAwaitableSignalCallback(
            args = arrayOf(arg0, arg1),
            signalName = signalName,
            callableName = ::signalAwaitableSignalCallback2Args.name.camelToSnakeCase(),
        )
    }

    override fun signalAwaitableSignalCallback3Args(arg0: Any?, arg1: Any?, arg2: Any?, signalName: String) {
        signalAwaitableSignalCallback(
            args = arrayOf(arg0, arg1, arg2),
            signalName = signalName,
            callableName = ::signalAwaitableSignalCallback3Args.name.camelToSnakeCase(),
        )
    }

    override fun signalAwaitableSignalCallback4Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        signalName: String,
    ) {
        signalAwaitableSignalCallback(
            args = arrayOf(arg0, arg1, arg2, arg3),
            signalName = signalName,
            callableName = ::signalAwaitableSignalCallback4Args.name.camelToSnakeCase(),
        )
    }

    override fun signalAwaitableSignalCallback5Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        signalName: String,
    ) {
        signalAwaitableSignalCallback(
            args = arrayOf(arg0, arg1, arg2, arg3, arg4),
            signalName = signalName,
            callableName = ::signalAwaitableSignalCallback5Args.name.camelToSnakeCase(),
        )
    }

    override fun signalAwaitableSignalCallback6Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        signalName: String,
    ) {
        signalAwaitableSignalCallback(
            args = arrayOf(arg0, arg1, arg2, arg3, arg4, arg5),
            signalName = signalName,
            callableName = ::signalAwaitableSignalCallback6Args.name.camelToSnakeCase(),
        )
    }

    override fun signalAwaitableSignalCallback7Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        arg6: Any?,
        signalName: String,
    ) {
        signalAwaitableSignalCallback(
            args = arrayOf(arg0, arg1, arg2, arg3, arg4, arg5, arg6),
            signalName = signalName,
            callableName = ::signalAwaitableSignalCallback7Args.name.camelToSnakeCase(),
        )
    }

    override fun signalAwaitableSignalCallback8Args(
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
        signalAwaitableSignalCallback(
            args = arrayOf(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7),
            signalName = signalName,
            callableName = ::signalAwaitableSignalCallback8Args.name.camelToSnakeCase(),
        )
    }

    /**
     * Triggers continuations needed if the user did some await setup in godot lifecycle functions like [Node._ready].
     */
    override fun signalAwaitableTriggerContinuations() {
        owner?.get()?.resumeGodotContinuations()
    }
    // END: await callback registered functions
}