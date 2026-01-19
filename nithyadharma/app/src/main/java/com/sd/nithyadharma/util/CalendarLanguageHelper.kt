package com.sd.nithyadharma.util

import com.sd.nithyadharma.model.NDLanguage

enum class KnownOccasion {
    PRADOSHAM,
    POURNAMI,
    AARUDRA_DARSHANAM,
    SANKATAHARA_CHATURTHI,
    TYAGARAJAR_ARADHANAI,
    VAIKUNDA_EKADASHI,
    KRITHIGAI,
    ASHTAMI,
    SHASHTI,
    EKADASHI,
    AMAVASAI,

    // Add more as needed, e.g.:
    // NAVARATRI,
    // DEEPAVALI,
    // MAHASHIVARATRI,
    // etc.
}

// English names (display or fallback)
private val occasionEn = mapOf(
    KnownOccasion.PRADOSHAM to "Pradosham",
    KnownOccasion.POURNAMI to "Pournami",
    KnownOccasion.AARUDRA_DARSHANAM to "Aarudra Darshanam",
    KnownOccasion.SANKATAHARA_CHATURTHI to "Sankatahara Chaturthi",
    KnownOccasion.TYAGARAJAR_ARADHANAI to "Tyagarajar Aradhanai",
    KnownOccasion.VAIKUNDA_EKADASHI to "Vaikunda Ekadashi",
    KnownOccasion.KRITHIGAI to "Krithigai",
    KnownOccasion.ASHTAMI to "Ashtami",
    KnownOccasion.SHASHTI to "Shashti",
    KnownOccasion.EKADASHI to "Ekadashi",
    KnownOccasion.AMAVASAI to "Amavasai"
)

// Tamil transliteration / native names
private val occasionTa = mapOf(
    KnownOccasion.PRADOSHAM to "பிரதோஷம்",
    KnownOccasion.POURNAMI to "பௌர்ணமி",
    KnownOccasion.AARUDRA_DARSHANAM to "ஆருத்ரா தரிசனம்",
    KnownOccasion.SANKATAHARA_CHATURTHI to "சங்கடஹர சதுர்த்தி",
    KnownOccasion.TYAGARAJAR_ARADHANAI to "தியாகராஜர் ஆராதனை",
    KnownOccasion.VAIKUNDA_EKADASHI to "வைகுண்ட ஏகாதசி",
    KnownOccasion.KRITHIGAI to "கிருத்திகை",
    KnownOccasion.ASHTAMI to "அஷ்டமி",
    KnownOccasion.SHASHTI to "சஷ்டி",
    KnownOccasion.EKADASHI to "ஏகாதசி",
    KnownOccasion.AMAVASAI to "அமாவாசை"
)
object CalendarLanguageHelper {

    fun occasionName(occasion: KnownOccasion, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> occasionEn[occasion] ?: occasion.name.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase)
            NDLanguage.TA -> occasionTa[occasion] ?: occasion.name  // fallback to enum name if missing
        }

}