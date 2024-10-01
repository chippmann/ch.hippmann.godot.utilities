package ch.hippmann.godot.utilities.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Deprecated(message = "This is no longer supported! Use awaitDeferred from godot-coroutine-library to run code on the ui thread instead!")
fun mainDispatcher(): CoroutineDispatcher = requireNotNull(DispatcherSingleton.mainDispatcher) {
    "${DispatcherSingleton.Companion::mainDispatcher.name} is null! Did you forget to add the ${DispatcherSingleton::class.java.simpleName} singleton to the autoload singletons in the godot project settings?"
}


@Deprecated(message = "Coroutine related helpers are being removed in favour of godot-coroutine-library! Copy this helper to your project if you need it")
fun ioDispatcher() = Dispatchers.IO


@Deprecated(message = "Coroutine related helpers are being removed in favour of godot-coroutine-library! Copy this helper to your project if you need it")
fun defaultDispatcher() = Dispatchers.Default
