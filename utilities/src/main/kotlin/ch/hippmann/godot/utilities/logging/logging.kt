package ch.hippmann.godot.utilities.logging

import ch.hippmann.godot.utilities.datetime.localNow
import ch.hippmann.godot.utilities.logging.Log.peerIdForLogging
import godot.OS
import godot.global.GD
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

@PublishedApi
internal val isDebugBuild by lazy { OS.isDebugBuild() }

enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
}

object Log {
    var peerIdForLogging: Int? = null

    var logLevel: LogLevel = if (OS.isDebugBuild()) {
        LogLevel.DEBUG
    } else {
        LogLevel.INFO
    }

    context(T) inline fun <reified T> trace(t: Throwable? = null, message: () -> String) = trace(message(), t)
    context(T) inline fun <reified T> trace(message: String, t: Throwable? = null) = __log<T>(LogLevel.TRACE, message, t)
    context(T) inline fun <reified T> debug(t: Throwable? = null, message: () -> String) = debug(message(), t)
    context(T) inline fun <reified T> debug(message: String, t: Throwable? = null) = __log<T>(LogLevel.DEBUG, message, t)
    context(T) inline fun <reified T> info(t: Throwable? = null, message: () -> String) = info(message(), t)
    context(T) inline fun <reified T> info(message: String, t: Throwable? = null) = __log<T>(LogLevel.INFO, message, t)
    context(T) inline fun <reified T> warn(t: Throwable? = null, message: () -> String) = warn(message(), t)
    context(T) inline fun <reified T> warn(message: String, t: Throwable? = null) = __log<T>(LogLevel.WARN, message, t)
    context(T) inline fun <reified T> err(t: Throwable? = null, message: () -> String) = err(message(), t)
    context(T) inline fun <reified T> err(message: String, t: Throwable? = null) = __log<T>(LogLevel.ERROR, message, t)
}

@Suppress("ObjectPropertyName")
@PublishedApi
internal val __logDateTimeFormatter = LocalDateTime.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    dayOfMonth()
    char('T')
    hour()
    char(':')
    minute()
    char(':')
    second()
    char('.')
    secondFraction(3)
}


@Suppress("FunctionName")
@PublishedApi
internal inline fun <reified T> __log(level: LogLevel, message: String, t: Throwable?) {
    val className = T::class.qualifiedName?.let { qualifiedName ->
        if (qualifiedName.contains(".")) {
            qualifiedName
        } else {
            "::$qualifiedName"
        }
    }
    val peerIdString = if (peerIdForLogging != null) {
        " PeerId($peerIdForLogging)"
    } else {
        ""
    }
    val formattedMessage = "${__logDateTimeFormatter.format(localNow())} [${Thread.currentThread().name}] ${level.toString().padEnd(5)} ${className}$peerIdString ${message}${t?.stackTraceToString()?.let { "\n$it" } ?: ""}"

    when(level) {
        LogLevel.ERROR -> GD.printErr(formattedMessage)
        LogLevel.WARN -> GD.print(formattedMessage)
        LogLevel.INFO -> GD.print(formattedMessage)
        LogLevel.DEBUG -> GD.print(formattedMessage)
        LogLevel.TRACE -> GD.print(formattedMessage)
    }
}