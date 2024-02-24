package ch.hippmann.godot.utilities.logging

import godot.OS
import godot.global.GD

@PublishedApi
internal val isDebugBuild by lazy { OS.isDebugBuild() }

fun debug(message: String, t: Throwable? = null) = debug(t) { message }
inline fun debug(t: Throwable? = null, message: () -> String) {
    if (isDebugBuild) {
        log(
            logPrefix = "DEBUG",
            t = t,
            message = message()
        )
    }
}
fun info(message: String, t: Throwable? = null) = info(t) { message }
inline fun info(t: Throwable? = null, message: () -> String) = log(
    logPrefix = "INFO",
    t = t,
    message = message()
)
fun warn(message: String, t: Throwable? = null) = warn(t) { message }
inline fun warn(t: Throwable? = null, message: () -> String) = log(
    logPrefix = "WARN",
    t = t,
    message = message()
)
fun err(message: String, t: Throwable? = null) = err(t) { message }
inline fun err(t: Throwable? = null, message: () -> String) = logError(
    logPrefix = "ERROR",
    t = t,
    message = message()
)


@PublishedApi
internal fun log(logPrefix: String, t: Throwable?, message: String) {
    GD.print("$logPrefix:\t$message")
    t?.let { throwable -> GD.print(throwable.stackTraceToString()) }
}

@PublishedApi
internal fun logError(logPrefix: String, t: Throwable?, message: String) {
    GD.printErr("$logPrefix:\t$message")
    t?.let { throwable -> GD.print(throwable.stackTraceToString()) }
}
