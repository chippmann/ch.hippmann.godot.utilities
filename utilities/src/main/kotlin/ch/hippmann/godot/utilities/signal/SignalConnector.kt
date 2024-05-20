package ch.hippmann.godot.utilities.signal

import godot.Node
import godot.core.Callable
import godot.core.GodotError
import godot.core.VariantArray
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
import java.lang.ref.WeakReference
import java.util.*

@Suppress("unused")
class SignalConnector : SignalConnectable {
    private val callbackMapLock = Mutex()
    private val callbackMap = mutableMapOf<String, MutableMap<String, (List<Any?>) -> Unit>>()
    private var owner: WeakReference<Node>? = null

    override fun <T : Node> T.initSignalConnectable() {
        this@SignalConnector.owner = WeakReference(this)
    }

    private fun connectInternal(
        signal: Signal,
        methodName: String,
        binds: VariantArray<Any?>?,
        flags: Long,
        callback: (args: List<Any?>) -> Unit
    ): SignalConnectable.ConnectionResult {
        val owner = this.owner?.get() ?: throw IllegalStateException("Got no owner. Did you forget to call initSignalConnectable?")

        val signalName = signal.name.toString()

        val callable = Callable(
            target = owner,
            methodName = methodName.asStringName()
        ).let { callable ->
            if (binds == null) {
                callable.bind(signalName)
            } else {
                callable.bind(
                    *binds.toTypedArray(),
                    signalName
                )
            }
        }

        val connectionResult = signal.connect(
            callable = callable,
            flags = flags.toInt(),
        )

        if (connectionResult == GodotError.OK) {
            val callbackId = runBlocking {
                callbackMapLock.withLock {
                    val callbackData = callbackMap[signalName] ?: mutableMapOf()
                    val callbackId = UUID.randomUUID().toString()

                    callbackMap[signalName] = callbackData.apply {
                        put(callbackId) { args ->
                            callback(
                                args,
                            )

                            // can become disconnected through flags the user provides
                            if (!owner.isConnected(signal = signalName.asStringName(), callable = callable)) {
                                runBlocking {
                                    callbackMapLock.withLock {
                                        callbackMap.remove(signalName)
                                    }
                                }
                            }
                        }
                    }

                    callbackId
                }
            }
            return SignalConnectable.ConnectionResult.Success(callbackId)
        } else {
            return SignalConnectable.ConnectionResult.Failure(connectionResult)
        }
    }

    private fun triggerCallback(signalName: String, args: List<Any?>) {
        runBlocking {
            val owner = this@SignalConnector.owner?.get()
            if (owner == null) {
                callbackMapLock.withLock {
                    callbackMap.clear()
                }
                return@runBlocking
            }

            val callbacks = callbackMapLock.withLock {
                callbackMap[signalName]?.values
            }

            callbacks?.forEach { callback ->
                callback(args)
            }
        }
    }

    private fun Signal.provideCallable(): Callable {
        val owner = this@SignalConnector.owner?.get() ?: throw IllegalStateException("Got no owner. Did you forget to call initSignalConnectable?")

        val function = when (this) {
            is Signal0 -> ::signalConnectableSignalCallback0Args
            is Signal1<*> -> ::signalConnectableSignalCallback1Args
            is Signal2<*, *> -> ::signalConnectableSignalCallback2Args
            is Signal3<*, *, *> -> ::signalConnectableSignalCallback3Args
            is Signal4<*, *, *, *> -> ::signalConnectableSignalCallback4Args
            is Signal5<*, *, *, *, *> -> ::signalConnectableSignalCallback5Args
            is Signal6<*, *, *, *, *, *> -> ::signalConnectableSignalCallback6Args
            is Signal7<*, *, *, *, *, *, *> -> ::signalConnectableSignalCallback7Args
            is Signal8<*, *, *, *, *, *, *, *> -> ::signalConnectableSignalCallback8Args
            else -> throw IllegalArgumentException("await can only handle signals with at most 8 arguments!")
        }
        return Callable(
            target = owner,
            methodName = function.name.camelToSnakeCase().asStringName()
        )
    }

    fun <NODE : Node> Signal.disconnect(callbackHandle: String) {
        val signalName = this.name.toString()
        val isNoConnectionLeft = runBlocking {
            callbackMapLock.withLock {
                val callbackMap = callbackMap[signalName]
                callbackMap?.remove(callbackHandle)

                callbackMap?.isEmpty() == true
            }
        }

        if (isNoConnectionLeft) {
            disconnect(provideCallable())
        }
    }

    fun <NODE : Node> Signal.disconnectAll() {
        val signalName = this.name.toString()
        runBlocking {
            callbackMapLock.withLock {
                callbackMap.remove(signalName)
            }
        }
        disconnect(provideCallable())
    }

    override fun Signal0.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: () -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback0Args.name.camelToSnakeCase(),
            binds = binds,
            flags = flags,
        ) { _ ->
            callback()
        }
    }

    override fun <P0> Signal1<P0>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.camelToSnakeCase(),
            binds = binds,
            flags = flags,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            callback(args[0] as P0)
        }
    }

    override fun <P0, P1> Signal2<P0, P1>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.camelToSnakeCase(),
            binds = binds,
            flags = flags,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            callback(
                args[0] as P0,
                args[1] as P1,
            )
        }
    }

    override fun <P0, P1, P2> Signal3<P0, P1, P2>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.camelToSnakeCase(),
            binds = binds,
            flags = flags,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            callback(
                args[0] as P0,
                args[1] as P1,
                args[2] as P2,
            )
        }
    }

    override fun <P0, P1, P2, P3> Signal4<P0, P1, P2, P3>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2, P3) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.camelToSnakeCase(),
            binds = binds,
            flags = flags,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            callback(
                args[0] as P0,
                args[1] as P1,
                args[2] as P2,
                args[3] as P3,
            )
        }
    }

    override fun <P0, P1, P2, P3, P4> Signal5<P0, P1, P2, P3, P4>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2, P3, P4) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.camelToSnakeCase(),
            binds = binds,
            flags = flags,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            callback(
                args[0] as P0,
                args[1] as P1,
                args[2] as P2,
                args[3] as P3,
                args[4] as P4,
            )
        }
    }

    override fun <P0, P1, P2, P3, P4, P5> Signal6<P0, P1, P2, P3, P4, P5>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2, P3, P4, P5) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.camelToSnakeCase(),
            binds = binds,
            flags = flags,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            callback(
                args[0] as P0,
                args[1] as P1,
                args[2] as P2,
                args[3] as P3,
                args[4] as P4,
                args[5] as P5,
            )
        }
    }

    override fun <P0, P1, P2, P3, P4, P5, P6> Signal7<P0, P1, P2, P3, P4, P5, P6>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2, P3, P4, P5, P6) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.camelToSnakeCase(),
            binds = binds,
            flags = flags,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            callback(
                args[0] as P0,
                args[1] as P1,
                args[2] as P2,
                args[3] as P3,
                args[4] as P4,
                args[5] as P5,
                args[6] as P6,
            )
        }
    }

    override fun <P0, P1, P2, P3, P4, P5, P6, P7> Signal8<P0, P1, P2, P3, P4, P5, P6, P7>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2, P3, P4, P5, P6, P7) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.camelToSnakeCase(),
            binds = binds,
            flags = flags,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            callback(
                args[0] as P0,
                args[1] as P1,
                args[2] as P2,
                args[3] as P3,
                args[4] as P4,
                args[5] as P5,
                args[6] as P6,
                args[7] as P7,
            )
        }
    }

    override fun signalConnectableSignalCallback0Args(signalName: String) {
        triggerCallback(signalName = signalName, args = listOf())
    }

    override fun signalConnectableSignalCallback1Args(arg0: Any?, signalName: String) {
        triggerCallback(
            signalName = signalName,
            args = listOf(
                arg0,
            )
        )
    }

    override fun signalConnectableSignalCallback2Args(arg0: Any?, arg1: Any?, signalName: String) {
        triggerCallback(
            signalName = signalName,
            args = listOf(
                arg0,
                arg1,
            )
        )
    }

    override fun signalConnectableSignalCallback3Args(arg0: Any?, arg1: Any?, arg2: Any?, signalName: String) {
        triggerCallback(
            signalName = signalName,
            args = listOf(
                arg0,
                arg1,
                arg2,
            )
        )
    }

    override fun signalConnectableSignalCallback4Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        signalName: String
    ) {
        triggerCallback(
            signalName = signalName,
            args = listOf(
                arg0,
                arg1,
                arg2,
                arg3,
            )
        )
    }

    override fun signalConnectableSignalCallback5Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        signalName: String
    ) {
        triggerCallback(
            signalName = signalName,
            args = listOf(
                arg0,
                arg1,
                arg2,
                arg3,
                arg4,
            )
        )
    }

    override fun signalConnectableSignalCallback6Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        signalName: String
    ) {
        triggerCallback(
            signalName = signalName,
            args = listOf(
                arg0,
                arg1,
                arg2,
                arg3,
                arg4,
                arg5,
            )
        )
    }

    override fun signalConnectableSignalCallback7Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        arg6: Any?,
        signalName: String
    ) {
        triggerCallback(
            signalName = signalName,
            args = listOf(
                arg0,
                arg1,
                arg2,
                arg3,
                arg4,
                arg5,
                arg6,
            )
        )
    }

    override fun signalConnectableSignalCallback8Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        arg6: Any?,
        arg7: Any?,
        signalName: String
    ) {
        triggerCallback(
            signalName = signalName,
            args = listOf(
                arg0,
                arg1,
                arg2,
                arg3,
                arg4,
                arg5,
                arg6,
                arg7,
            )
        )
    }
}