package com.silentwitness.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.formatFull(): String =
    this.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"))

fun LocalDateTime.formatDate(): String =
    this.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

fun LocalDate.formatDate(): String =
    this.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

fun LocalDateTime.formatTime(): String =
    this.format(DateTimeFormatter.ofPattern("h:mm a"))

fun categoryLabel(category: String): String = when (category) {
    "physical" -> "Physical"
    "verbal" -> "Verbal"
    "financial" -> "Financial"
    "digital" -> "Digital"
    else -> "Other"
}
