package com.sd.nithyadharma.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

object CommonFunctions {

    fun getCurrentTime(): LocalDateTime {
        return LocalDateTime.now(Constants.INDIA_ZONE)
    }

    fun getCurrentDate(): LocalDate {
        return LocalDate.now(Constants.INDIA_ZONE)
    }

    // for uniq whatsapp id to see if they are doin it on the day they say they are
    // doin it
    fun generateNDRId(): String {
        // 1. Get current day of week (3-letter uppercase abbreviation)
        val dayOfWeek = LocalDate.now()
            .dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            .lowercase()  // "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"

        // 2. Generate UUID and take first 7 chars
        val uuid = UUID.randomUUID().toString()
        val uuidPart = uuid.take(9)  // e.g. "a1b2c3d"

        // 3. Interleave day-of-week chars into uuidPart at fixed (but not obvious) positions
        // Positions: 0, 3, 6 (spread out, not consecutive)
        val sb = StringBuilder(uuidPart)
        sb.setCharAt(1, dayOfWeek[0])   // 1st char → day[0] e.g. 'T' for Thu
        sb.setCharAt(3, dayOfWeek[1])   // 4th char → day[1] e.g. 'h'
        sb.setCharAt(6, dayOfWeek[2])   // 7th char → day[2] e.g. 'u'

        val finalId = sb.toString()

        println("Generated ID: $finalId (Day embedded: $dayOfWeek)")
        return finalId
    }
    // todo should swissephemeris be initialised in once place may be here in init
}