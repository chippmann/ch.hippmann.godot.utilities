package ch.hippmann.godot.utilities.logging

import godot.OS
import godot.global.GD

@PublishedApi
internal val isDebugBuild by lazy { OS.isDebugBuild() }

inline fun debug(t: Throwable? = null, message: () -> String) {
    if (isDebugBuild) {
        log(
            logPrefix = "DEBUG",
            t = t,
            message = message()
        )
    }
}
inline fun info(t: Throwable? = null, message: () -> String) = log(
    logPrefix = "INFO",
    t = t,
    message = message()
)
inline fun warn(t: Throwable? = null, message: () -> String) = log(
    logPrefix = "WARN",
    t = t,
    message = message()
)
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
