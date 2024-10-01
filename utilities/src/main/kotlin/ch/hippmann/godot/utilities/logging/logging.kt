@file:Suppress("NOTHING_TO_INLINE")

package ch.hippmann.godot.utilities.logging

import ch.hippmann.godot.utilities.datetime.localNow
import ch.hippmann.godot.utilities.logging.Log.peerIdForLogging
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
}

object Log {
    var peerIdForLogging: Int? = null

    inline fun trace(t: Throwable? = null, message: () -> String) = trace(message(), t)
    inline fun trace(message: String, t: Throwable? = null) = __log(LogLevel.TRACE, message, t)
    inline fun debug(t: Throwable? = null, message: () -> String) = debug(message(), t)
    inline fun debug(message: String, t: Throwable? = null) = __log(LogLevel.DEBUG, message, t)
    inline fun info(t: Throwable? = null, message: () -> String) = info(message(), t)
    inline fun info(message: String, t: Throwable? = null) = __log(LogLevel.INFO, message, t)
    inline fun warn(t: Throwable? = null, message: () -> String) = warn(message(), t)
    inline fun warn(message: String, t: Throwable? = null) = __log(LogLevel.WARN, message, t)
    inline fun err(t: Throwable? = null, message: () -> String) = err(message(), t)
    inline fun err(message: String, t: Throwable? = null) = __log(LogLevel.ERROR, message, t)
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
internal inline fun __log(level: LogLevel, message: String, t: Throwable?) {
    val stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
    val className = stackWalker.walk { stream ->
        stream.map(StackWalker.StackFrame::getClassName)
            .findFirst()
            .orElse("Unknown")
    }

    val peerIdString = if (peerIdForLogging != null) {
        " PeerId($peerIdForLogging)"
    } else {
        ""
    }
    val formattedMessage = "${__logDateTimeFormatter.format(localNow())} [${Thread.currentThread().name}] ${level.toString().padEnd(5)} ${className}$peerIdString ${message}${t?.stackTraceToString()?.let { "\n$it" } ?: ""}"

    when(level) {
        LogLevel.ERROR -> println(formattedMessage)
        LogLevel.WARN -> println(formattedMessage)
        LogLevel.INFO -> println(formattedMessage)
        LogLevel.DEBUG -> println(formattedMessage)
        LogLevel.TRACE -> println(formattedMessage)
    }
}