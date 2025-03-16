package sample.coroutines.await

import ch.hippmann.godot.utilities.coroutines.await.SignalAwaitable
import ch.hippmann.godot.utilities.coroutines.await.SignalAwaiter
import godot.api.Node
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.core.signal0
import godot.core.signal1
import godot.global.GD
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds

@RegisterClass
class SignalAwaitSample: Node(), SignalAwaitable by SignalAwaiter() {
    @RegisterSignal("someData") val customSignalWithArgs by signal1<String>()
    @RegisterSignal val customSignalForMultiAwaitTest by signal0()

    init {
        initSignalAwait()
    }

    @RegisterFunction
    override fun _enterTree() {
        launch {
            GD.print("Before await for ${::ready.name} signal")
            ready.await()
            GD.print("Node appears to be ready according to signal await")
        }
        launch {
            GD.print("Before await for ${::customSignalWithArgs.name} signal")
            val args = customSignalWithArgs.await()
            GD.print("Custom signal with args emitted. Received args: [${args.joinToString()}]")
        }

        // multi await test
        launch {
            GD.print("Before await for ${::customSignalForMultiAwaitTest.name} signal 1")
            customSignalForMultiAwaitTest.await()
            GD.print("After await for ${::customSignalForMultiAwaitTest.name} signal 1")
        }
        launch {
            GD.print("Before await for ${::customSignalForMultiAwaitTest.name} signal 2")
            customSignalForMultiAwaitTest.await()
            GD.print("After await for ${::customSignalForMultiAwaitTest.name} signal 2")
        }
        runBlocking { delay(1.seconds) } // to give background thread some time to run in this over simplified example
    }

    @RegisterFunction
    override fun _ready() {
        GD.print("Node is ready! Any signal await messages should appear after this line!")
        customSignalWithArgs.emit("some custom data string we emitted for testing")
        customSignalForMultiAwaitTest.emit()

        launch {
            GD.print("Before await for ${::treeEntered.name} signal to test signal emition for already passed events")
            treeEntered.await()
            GD.print("After await for ${::treeEntered.name} signal to test signal emition for already passed events")
        }
    }
}