package ch.hippmann.godot.utilities.datetime

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@PublishedApi
internal val currentTimeZone = TimeZone.currentSystemDefault()

@PublishedApi
internal fun now(): Instant = Clock.System.now()
@PublishedApi
internal fun localNow(): LocalDateTime = now().toLocalDateTime(currentTimeZone)