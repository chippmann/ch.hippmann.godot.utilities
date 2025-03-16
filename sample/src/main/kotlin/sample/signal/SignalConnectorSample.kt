package sample.signal

import ch.hippmann.godot.utilities.signal.SignalConnectable
import ch.hippmann.godot.utilities.signal.SignalConnector
import godot.api.Node
import godot.api.Object
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterSignal
import godot.core.signal1
import godot.global.GD

@RegisterClass
class SignalConnectorSample : Node(), SignalConnectable by SignalConnector() {
    @RegisterSignal("someData")
    val customSignalWithArgs by signal1<String>()

    init {
        initSignalConnectable()
    }

    @RegisterFunction
    override fun _enterTree() {
        ready.connect {
            GD.print("${SignalConnectorSample::class.simpleName} is ready")
        }
        customSignalWithArgs.connect(Object.ConnectFlags.CONNECT_ONE_SHOT.id) { arg ->
            GD.print("Received signal emition from ${::customSignalWithArgs.name} with arg: $arg. This signal should only be received once!")
        }
    }

    @RegisterFunction
    override fun _ready() {
        customSignalWithArgs.emit("Should be received")
        customSignalWithArgs.emit("Should NOT be received!!!")
    }
}