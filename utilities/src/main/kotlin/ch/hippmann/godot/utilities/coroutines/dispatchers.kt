package ch.hippmann.godot.utilities.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

fun mainDispatcher(): CoroutineDispatcher = requireNotNull(DispatcherSingleton.mainDispatcher) {
    "${DispatcherSingleton.Companion::mainDispatcher.name} is null! Did you forget to add the ${DispatcherSingleton::class.java.simpleName} singleton to the autoload singletons in the godot project settings?"
}

fun ioDispatcher() = Dispatchers.IO

fun defaultDispatcher() = Dispatchers.Default
