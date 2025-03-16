package ch.hippmann.godot.utilities.coroutines.scope

import godot.api.Node
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

@Deprecated(message = "This functionality is now part of the official godot-coroutine-library! See: https://godot-kotl.in/en/stable/user-guide/coroutines/")
interface GodotCoroutineScope : CoroutineScope {
    @Deprecated(message = "This is no longer supported! Use awaitDeferred from godot-coroutine-library instead!")
    fun Node.resumeGodotContinuations()

    @Deprecated(message = "This is no longer supported! Use awaitDeferred from godot-coroutine-library instead!")
    suspend fun Node.withGodotContext(block: () -> Unit)
}

internal typealias GodotContinuation = Continuation<Unit>
internal typealias GodotContinuationWithBlock = Pair<Continuation<Unit>, () -> Unit>
internal fun GodotContinuation.resume() = resume(Unit)