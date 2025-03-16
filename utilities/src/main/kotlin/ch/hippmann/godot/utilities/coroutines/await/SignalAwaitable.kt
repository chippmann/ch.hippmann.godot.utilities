package ch.hippmann.godot.utilities.coroutines.await

import ch.hippmann.godot.utilities.coroutines.scope.GodotCoroutineScope
import godot.api.Node
import godot.annotation.RegisterFunction
import godot.core.Signal

@Deprecated(message = "This functionality is now part of the official godot-coroutine-library! See: https://godot-kotl.in/en/stable/user-guide/coroutines/")
interface SignalAwaitable: GodotCoroutineScope {
    @Deprecated(message = "This functionality is now part of the official godot-coroutine-library! See: https://godot-kotl.in/en/stable/user-guide/coroutines/")
    fun <T: Node> T.initSignalAwait()

    @Deprecated(message = "This functionality is now part of the official godot-coroutine-library! See: https://godot-kotl.in/en/stable/user-guide/coroutines/")
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