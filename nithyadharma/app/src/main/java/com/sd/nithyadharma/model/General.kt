package com.sd.nithyadharma.model

import java.time.LocalDateTime
import java.time.LocalTime

enum class NDLanguage {
    EN, TA
}

fun languageName(lang: NDLanguage): String =
    when (lang) {
        NDLanguage.EN -> "English"
        NDLanguage.TA -> "தமிழ் (Tamil)"
    }

data class TimeRange(
    val start: LocalDateTime,
    val end: LocalDateTime
)

data class TimeWindow(
    val start: LocalTime,
    val end: LocalTime
)