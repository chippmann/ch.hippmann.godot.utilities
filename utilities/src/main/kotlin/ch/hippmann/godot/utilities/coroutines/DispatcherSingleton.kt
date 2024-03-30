package ch.hippmann.godot.utilities.coroutines

import godot.Node
import godot.annotation.RegisterClass
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.ContinuationInterceptor

@RegisterClass
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
