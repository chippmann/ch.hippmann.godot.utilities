package ch.hippmann.godot.utilities.signal

import godot.Error
import godot.Node
import godot.core.Callable
import godot.core.Signal
import godot.core.Signal0
import godot.core.Signal1
import godot.core.Signal2
import godot.core.Signal3
import godot.core.Signal4
import godot.core.Signal5
import godot.core.Signal6
import godot.core.Signal7
import godot.core.Signal8
import godot.core.VariantArray
import godot.core.asStringName
import godot.core.toGodotName
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.ref.WeakReference
import java.util.*

@Suppress("unused")
@Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
class SignalConnector : SignalConnectable {
    private val callbackMapLock = Mutex()
    private val callbackMap = mutableMapOf<String, MutableMap<String, (List<Any?>) -> Unit>>()
    private var owner: WeakReference<Node>? = null

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
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

        if (connectionResult == Error.OK) {
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
            methodName = function.name.toGodotName()
        )
    }

    fun Signal.disconnect(callbackHandle: String) {
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

    fun Signal.disconnectAll() {
        val signalName = this.name.toString()
        runBlocking {
            callbackMapLock.withLock {
                callbackMap.remove(signalName)
            }
        }
        disconnect(provideCallable())
    }

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    override fun Signal0.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: () -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback0Args.name.toGodotName().toString(),
            binds = binds,
            flags = flags,
        ) { _ ->
            callback()
        }
    }

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    override fun <P0> Signal1<P0>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.toGodotName().toString(),
            binds = binds,
            flags = flags,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            callback(args[0] as P0)
        }
    }

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    override fun <P0, P1> Signal2<P0, P1>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.toGodotName().toString(),
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

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    override fun <P0, P1, P2> Signal3<P0, P1, P2>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.toGodotName().toString(),
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

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    override fun <P0, P1, P2, P3> Signal4<P0, P1, P2, P3>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2, P3) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.toGodotName().toString(),
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

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    override fun <P0, P1, P2, P3, P4> Signal5<P0, P1, P2, P3, P4>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2, P3, P4) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.toGodotName().toString(),
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

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    override fun <P0, P1, P2, P3, P4, P5> Signal6<P0, P1, P2, P3, P4, P5>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2, P3, P4, P5) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.toGodotName().toString(),
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

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    override fun <P0, P1, P2, P3, P4, P5, P6> Signal7<P0, P1, P2, P3, P4, P5, P6>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2, P3, P4, P5, P6) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.toGodotName().toString(),
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

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    override fun <P0, P1, P2, P3, P4, P5, P6, P7> Signal8<P0, P1, P2, P3, P4, P5, P6, P7>.connect(
        flags: Long,
        binds: VariantArray<Any?>?,
        callback: (P0, P1, P2, P3, P4, P5, P6, P7) -> Unit,
    ): SignalConnectable.ConnectionResult {
        return connectInternal(
            signal = this,
            methodName = ::signalConnectableSignalCallback1Args.name.toGodotName().toString(),
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