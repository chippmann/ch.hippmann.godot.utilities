package ch.hippmann.godot.utilities.coroutines.scope

import godot.Node
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

interface GodotCoroutineScope : CoroutineScope {
    fun Node.resumeGodotContinuations()
    suspend fun Node.withGodotContext(block: () -> Unit)
}

internal typealias GodotContinuation = Continuation<Unit>
internal typealias GodotContinuationWithBlock = Pair<Continuation<Unit>, () -> Unit>
internal fun GodotContinuation.resume() = resume(Unit)