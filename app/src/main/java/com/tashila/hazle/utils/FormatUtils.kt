package com.tashila.hazle.utils

import android.icu.text.DateFormat
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import java.util.Date
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Instant

fun Instant.toDateTimeString(): String {
    val javaUtilDate = Date(this.toEpochMilliseconds())
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
        .format(javaUtilDate)
}

fun Instant?.toTimeString(): String {
    if (this == null) return ""

    val timeZone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(timeZone)
    val msgTime = this.toLocalDateTime(timeZone)
    val msgDate = msgTime.date

    val timeFormatted = buildString {
        val hour12 = ((msgTime.hour - 1) % 12 + 1)
        append(hour12)
        append(":")
        append(msgTime.minute.toString().padStart(2, '0'))
        append(" ")
        append(if (msgTime.hour < 12) "AM" else "PM")
    }

    return when (msgDate) {
        now.date -> timeFormatted
        now.date.minus(1, DateTimeUnit.DAY) -> "Yesterday"
        else -> "${msgDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${msgDate.day}"
    }
}