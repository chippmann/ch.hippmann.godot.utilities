package ch.hippmann.godot.utilities.coroutines.await

import ch.hippmann.godot.utilities.coroutines.scope.GodotCoroutineScope
import godot.Node
import godot.annotation.RegisterFunction
import godot.signals.Signal

interface SignalAwaitable: GodotCoroutineScope {
    fun <T: Node> T.initSignalAwait()

    suspend fun Signal.await(): Array<Any?>

    @RegisterFunction
    fun signalAwaitableSignalCallback0Args(signalName: String)

    @RegisterFunction
    fun signalAwaitableSignalCallback1Args(arg0: Any?, signalName: String)

    @RegisterFunction
    fun signalAwaitableSignalCallback2Args(arg0: Any?, arg1: Any?, signalName: String)

    @RegisterFunction
    fun signalAwaitableSignalCallback3Args(arg0: Any?, arg1: Any?, arg2: Any?, signalName: String)

    @RegisterFunction
    fun signalAwaitableSignalCallback4Args(arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, signalName: String)

    @RegisterFunction
    fun signalAwaitableSignalCallback5Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        signalName: String,
    )

    @RegisterFunction
    fun signalAwaitableSignalCallback6Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        signalName: String,
    )

    @RegisterFunction
    fun signalAwaitableSignalCallback7Args(
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
    fun signalAwaitableSignalCallback8Args(
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
    fun signalAwaitableTriggerContinuations()
}