package com.sd.nithyadharma.util

import com.sd.nithyadharma.model.NDLanguage
import java.time.LocalDateTime

// this object houses all language specific functions so
// we will have less places to see to introduce new languages etc

object LocaleOther {

    fun getDayRelativeToNow(eventTime: LocalDateTime, currentLang: NDLanguage): String {
        val now = CommonFunctions.getCurrentTime()
        val today = now.toLocalDate()
        val tomorrow = today.plusDays(1)
        val eventDate = eventTime.toLocalDate()

        val key = when (eventDate) {
            today -> "today"
            tomorrow -> "tomorrow"
            else -> if (eventTime.isAfter(now)) "later" else "past"
        }
        val evday = when (currentLang) {
            NDLanguage.EN -> when (key) {
                "today" -> "Today"
                "tomorrow" -> "Tomorrow"
                "later" -> "Day after tomorrow"
                "past" -> "Past"
                else -> ""
            }
            NDLanguage.TA -> when (key) {
                "today" -> "இன்று"
                "tomorrow" -> "நாளை"
                "later" -> "நாளை மறுநாள்"
                "past" -> "கடந்தது"
                else -> ""
            }
            NDLanguage.KA -> when (key) {
                "today" -> "ಇಂದು"
                "tomorrow" -> "ನಾಳೆ"
                "later" -> "ನಾಳೆಯ ಮರುದಿನ"
                "past" -> "ಕಳೆದದ್ದು"
                else -> ""
            }
            NDLanguage.HI -> when (key) {
                "today" -> "आज"
                "tomorrow" -> "कल"          // Kal (tomorrow)
                "later" -> "परसों"           // Parsõ (Day after tomorrow)
                "past" -> "बीत चुका"         // Beet chuka (Past)
                else -> ""
            }
        }

        return evday
    }

    fun getTimeSlotLocalized(time: LocalDateTime, lang: NDLanguage): String {
        val hour = time.hour  // 0-23

        val slot = when {
            hour < 12 -> "morning"
            hour < 16 -> "afternoon"
            hour < 19 -> "evening"
            else -> "night"
        }

        return when (lang) {
            NDLanguage.EN -> when (slot) {
                "morning" -> "Morning"
                "afternoon" -> "Afternoon"
                "evening" -> "Evening"
                "night" -> "Night"
                else -> ""
            }

            NDLanguage.TA -> when (slot) {
                "morning" -> "காலை"
                "afternoon" -> "மதியம்"
                "evening" -> "மாலை"
                "night" -> "இரவு"
                else -> ""
            }

            NDLanguage.KA -> when (slot) {
                "morning" -> "ಬೆಳಿಗ್ಗೆ"
                "afternoon" -> "ಮಧ್ಯಾಹ್ನ"
                "evening" -> "ಸಂಜೆ"
                "night" -> "ರಾತ್ರಿ"
                else -> ""
            }

            NDLanguage.HI -> when (slot) {
                "morning" -> "सुबह"          // Subah
                "afternoon" -> "दोपहर"        // Dopahar
                "evening" -> "शाम"           // Shaam
                "night" -> "रात"            // Raat
                else -> ""
            }
        }
    }

}