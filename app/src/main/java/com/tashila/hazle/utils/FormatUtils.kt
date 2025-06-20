package com.tashila.hazle.utils

import android.icu.text.DateFormat
import kotlinx.datetime.Instant
import java.util.Date
import java.util.Locale

fun Instant.toReadableString(): String {
    val javaUtilDate = Date(this.toEpochMilliseconds())
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
        .format(javaUtilDate)
}