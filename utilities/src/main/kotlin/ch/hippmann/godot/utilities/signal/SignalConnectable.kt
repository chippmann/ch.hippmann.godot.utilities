package ch.hippmann.godot.utilities.signal

import godot.annotation.RegisterFunction
import godot.api.Node
import godot.core.Error
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

@Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
interface SignalConnectable {
    sealed interface ConnectionResult {
        data class Success(val signalHandle: String): ConnectionResult
        data class Failure(val godotError: Error): ConnectionResult
    }

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    fun <T : Node> T.initSignalConnectable()

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    fun Signal0.connect(
        flags: Long = 0,
        binds: VariantArray<Any?>? = null,
        callback: () -> Unit,
    ): ConnectionResult

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    fun <P0> Signal1<P0>.connect(
        flags: Long = 0,
        binds: VariantArray<Any?>? = null,
        callback: (P0) -> Unit,
    ): ConnectionResult

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    fun <P0, P1> Signal2<P0, P1>.connect(
        flags: Long = 0,
        binds: VariantArray<Any?>? = null,
        callback: (P0, P1) -> Unit,
    ): ConnectionResult

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    fun <P0, P1, P2> Signal3<P0, P1, P2>.connect(
        flags: Long = 0,
        binds: VariantArray<Any?>? = null,
        callback: (P0, P1, P2) -> Unit,
    ): ConnectionResult

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    fun <P0, P1, P2, P3> Signal4<P0, P1, P2, P3>.connect(
        flags: Long = 0,
        binds: VariantArray<Any?>? = null,
        callback: (P0, P1, P2, P3) -> Unit,
    ): ConnectionResult

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    fun <P0, P1, P2, P3, P4> Signal5<P0, P1, P2, P3, P4>.connect(
        flags: Long = 0,
        binds: VariantArray<Any?>? = null,
        callback: (P0, P1, P2, P3, P4) -> Unit,
    ): ConnectionResult

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    fun <P0, P1, P2, P3, P4, P5> Signal6<P0, P1, P2, P3, P4, P5>.connect(
        flags: Long = 0,
        binds: VariantArray<Any?>? = null,
        callback: (P0, P1, P2, P3, P4, P5) -> Unit,
    ): ConnectionResult

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    fun <P0, P1, P2, P3, P4, P5, P6> Signal7<P0, P1, P2, P3, P4, P5, P6>.connect(
        flags: Long = 0,
        binds: VariantArray<Any?>? = null,
        callback: (P0, P1, P2, P3, P4, P5, P6) -> Unit,
    ): ConnectionResult

    @Deprecated(message = "This functionality is now part of the official godot-library! Use the signals connect function with the lambda syntax instead! If however you depend on the ability to disconnect these lambdas again, you can keep using these helpers. They will only be removed once this feature is supported officially in godot-kotlin!")
    fun <P0, P1, P2, P3, P4, P5, P6, P7> Signal8<P0, P1, P2, P3, P4, P5, P6, P7>.connect(
        flags: Long = 0,
        binds: VariantArray<Any?>? = null,
        callback: (P0, P1, P2, P3, P4, P5, P6, P7) -> Unit,
    ): ConnectionResult

    @RegisterFunction
    fun signalConnectableSignalCallback0Args(signalName: String)

    @RegisterFunction
    fun signalConnectableSignalCallback1Args(arg0: Any?, signalName: String)

    @RegisterFunction
    fun signalConnectableSignalCallback2Args(arg0: Any?, arg1: Any?, signalName: String)

    @RegisterFunction
    fun signalConnectableSignalCallback3Args(arg0: Any?, arg1: Any?, arg2: Any?, signalName: String)

    @RegisterFunction
    fun signalConnectableSignalCallback4Args(arg0: Any?, arg1: Any?, arg2: Any?, arg3: Any?, signalName: String)

    @RegisterFunction
    fun signalConnectableSignalCallback5Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        signalName: String,
    )

    @RegisterFunction
    fun signalConnectableSignalCallback6Args(
        arg0: Any?,
        arg1: Any?,
        arg2: Any?,
        arg3: Any?,
        arg4: Any?,
        arg5: Any?,
        signalName: String,
    )

    @RegisterFunction
    fun signalConnectableSignalCallback7Args(
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
    fun signalConnectableSignalCallback8Args(
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
}