package ch.hippmann.godot.utilities.coroutines

import godot.api.Node
import godot.annotation.RegisterClass
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.ContinuationInterceptor

@RegisterClass
@Deprecated(message = "This is no longer supported! Use awaitDeferred from godot-coroutine-library to run code on the ui thread instead!")
class DispatcherSingleton: Node() {
    companion object {
        var mainDispatcher: CoroutineDispatcher? = null
    }

    init {
        runBlocking {
            mainDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        }
    }
}
