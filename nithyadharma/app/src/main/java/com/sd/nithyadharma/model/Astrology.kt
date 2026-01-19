package com.sd.nithyadharma.model

import java.time.DayOfWeek
import java.time.LocalDateTime

enum class Paksha {
    SHUKLA,
    KRISHNA
}

enum class Vaara {
    RAVI,
    SOMA,
    MANGAL,
    BUDHA,
    GURU,
    SHUKRA,
    SHANI
}

enum class Nakshatra {
    ASHWINI, BHARANI, KRITHTHIKA,
    ROHINI, MRIGASHIRSHA, AARDHRAA,
    PUNARVASU, PUSHYA, ASHLESHA,
    MAGHA, PURVA_PHALGUNI, UTTARA_PHALGUNI,
    HASTHA, CHITRA, SWAATHI,
    VISHAKHA, ANURADHA, JYESHTHA,
    MULA, PURVA_ASHADA, UTTARA_ASHADA,
    SHRAVANA, DHANISHTA, SHATABHISHA,
    PURVA_BHADRAPADA, UTTARA_BHADRAPADA, REVATHI
}

enum class Rasi {
    MESHA,
    VRISHABHA,
    MITHUNA,
    KARKATAKA,
    SIMHA,
    KANYA,
    THULAA,
    VRISHCHIKA,
    DHANUS,
    MAKARA,
    KUMBHA,
    MEENA
}

enum class Thithi {
    PRATHAMA, DWITIYA, TRITIYA, CHATURTHI, PANCHAMI,
    SHASHTI, SAPTAMI, ASHTAMI, NAVAMI, DASHAMI,
    EKADASHI, DWADASHI, TRAYODASHI, CHATURDASHI,
    PURNIMA, AMAVASYA
}

enum class Yoga {
    VISHKAMBHA, PREETI, AYUSHMAN, SAUBHAGYA, SHOBHANA,
    ATIGANDA, SUKARMA, DHRITI, SHULA, GANDA,
    VRIDDHI, DHRUVA, VYAGHATA, HARSHANA, VAJRA,
    SIDDHI, VYATIPATA, VARIYAN, PARIGHA, SHIVA,
    SIDDHA, SADHYA, SHUBHA, SHUKLA, BRAHMA,
    INDRA, VAIDHRITI
}

enum class Karana {
    BAVA, BALAVA, KAULAVA, TAITILA, GARAJA, VANIJA, VISHTI,
    SHAKUNI, CHATUSHPADA, NAGA, KIMSTHUGNA
}

enum class NakshatraType {
    MRIDU,
    KSHIPRA,
    DHRUVA,
    UGRA,
    TIKSHNA,
    MISHRA
}

enum class NakshatraScore(val value: Int) {
    VERY_GOOD(3),   // Dhruva
    GOOD(2),        // Kshipra
    MILD(1),        // Mridu
    NEUTRAL(0),     // Mishra
    BAD(-1),        // Tikshna
    VERY_BAD(-2)    // Ugra
}

data class TimeRange(
    val start: LocalDateTime,
    val end: LocalDateTime
)

object Astrology {

    // functions

    fun computeVaaraFromSunrise(now: LocalDateTime, sunrise: LocalDateTime): Vaara =
        when (if (now.isBefore(sunrise)) now.minusDays(1).dayOfWeek else now.dayOfWeek) {
            DayOfWeek.SUNDAY    -> Vaara.RAVI
            DayOfWeek.MONDAY    -> Vaara.SOMA   // In Tamil tradition: Monday = Moon → Mangal? Wait — see note below
            DayOfWeek.TUESDAY   -> Vaara.MANGAL     // Actually Tuesday = Mars (Sevvai), but you named it GUJA?
            DayOfWeek.WEDNESDAY -> Vaara.BUDHA
            DayOfWeek.THURSDAY  -> Vaara.GURU
            DayOfWeek.FRIDAY    -> Vaara.SHUKRA
            DayOfWeek.SATURDAY  -> Vaara.SHANI
        }

    // Helper function (assuming you have the same NDLanguage enum)
    fun rasiName(r: Rasi, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> rasiEn[r]!!
            NDLanguage.TA -> rasiTa[r]!!
        }

    fun vaaraName(v: Vaara, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> vaaraEn[v]!!
            NDLanguage.TA -> vaaraTa[v]!!
        }

    fun nakshatraScore(type: NakshatraType): NakshatraScore =
        when (type) {
            NakshatraType.DHRUVA   -> NakshatraScore.VERY_GOOD
            NakshatraType.KSHIPRA  -> NakshatraScore.GOOD
            NakshatraType.MRIDU    -> NakshatraScore.MILD
            NakshatraType.MISHRA   -> NakshatraScore.NEUTRAL
            NakshatraType.TIKSHNA  -> NakshatraScore.BAD
            NakshatraType.UGRA     -> NakshatraScore.VERY_BAD
        }


    fun getNakshatraType(n: Nakshatra): NakshatraType =
        nakshatraTypeMap[n] ?: NakshatraType.MISHRA

    fun nakshatraName(n: Nakshatra, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> nakshatraEn[n]!!
            NDLanguage.TA -> nakshatraTa[n]!!
        }

    fun tithiName(t: Thithi, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> thithiEn[t]!!
            NDLanguage.TA -> thithiTa[t]!!
        }

    fun yogaName(y: Yoga, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> yogaEn[y]!!
            NDLanguage.TA -> yogaTa[y]!!
        }

    fun karanaName(k: Karana, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> karanaEn[k]!!
            NDLanguage.TA -> karanaTa[k]!!
        }

    /* ---------------- DATA ---------------- */

    private val nakshatraTypeMap: Map<Nakshatra, NakshatraType> = mapOf(

        // 🟢 Mridu
        Nakshatra.ANURADHA to NakshatraType.MRIDU,
        Nakshatra.CHITRA to NakshatraType.MRIDU,
        Nakshatra.REVATHI to NakshatraType.MRIDU,
        Nakshatra.MRIGASHIRSHA to NakshatraType.MRIDU,

        // 🟡 Kshipra
        Nakshatra.ASHWINI to NakshatraType.KSHIPRA,
        Nakshatra.PUSHYA to NakshatraType.KSHIPRA,
        Nakshatra.HASTHA to NakshatraType.KSHIPRA,

        // 🔵 Dhruva
        Nakshatra.ROHINI to NakshatraType.DHRUVA,
        Nakshatra.UTTARA_PHALGUNI to NakshatraType.DHRUVA,
        Nakshatra.UTTARA_ASHADA to NakshatraType.DHRUVA,
        Nakshatra.UTTARA_BHADRAPADA to NakshatraType.DHRUVA,

        // 🔴 Ugra
        Nakshatra.BHARANI to NakshatraType.UGRA,
        Nakshatra.MAGHA to NakshatraType.UGRA,
        Nakshatra.PURVA_PHALGUNI to NakshatraType.UGRA,
        Nakshatra.PURVA_ASHADA to NakshatraType.UGRA,
        Nakshatra.PURVA_BHADRAPADA to NakshatraType.UGRA,

        // ⚫ Tikshna
        Nakshatra.AARDHRAA to NakshatraType.TIKSHNA,
        Nakshatra.ASHLESHA to NakshatraType.TIKSHNA,
        Nakshatra.JYESHTHA to NakshatraType.TIKSHNA,
        Nakshatra.MULA to NakshatraType.TIKSHNA
    )

    private val vaaraEn = mapOf(
        Vaara.RAVI to "Sunday",
        Vaara.SOMA to "Monday",
        Vaara.MANGAL to "Tuesday",
        Vaara.BUDHA to "Wednesday",
        Vaara.GURU to "Thursday",
        Vaara.SHUKRA to "Friday",
        Vaara.SHANI to "Saturday"
    )

    private val vaaraTa = mapOf(
        Vaara.RAVI to "ஞாயிறு",
        Vaara.SOMA to "திங்கள்",
        Vaara.MANGAL to "செவ்வாய்",
        Vaara.BUDHA to "புதன்",
        Vaara.GURU to "வியாழன்",
        Vaara.SHUKRA to "வெள்ளி",
        Vaara.SHANI to "சனி"
    )

    // English names (transliterated)
    private val rasiEn = mapOf(
        Rasi.MESHA to "Mesha",
        Rasi.VRISHABHA to "Vrishabha",
        Rasi.MITHUNA to "Mithuna",
        Rasi.KARKATAKA to "Karkataka",
        Rasi.SIMHA to "Simha",
        Rasi.KANYA to "Kanya",
        Rasi.THULAA to "Thulaa",
        Rasi.VRISHCHIKA to "Vrishchika",
        Rasi.DHANUS to "Dhanus",
        Rasi.MAKARA to "Makara",
        Rasi.KUMBHA to "Kumbha",
        Rasi.MEENA to "Meena"
    )

    // Tamil names
    private val rasiTa = mapOf(
        Rasi.MESHA to "மேஷம்",
        Rasi.VRISHABHA to "ரிஷபம்",
        Rasi.MITHUNA to "மிதுனம்",
        Rasi.KARKATAKA to "கடகம்",
        Rasi.SIMHA to "சிம்மம்",
        Rasi.KANYA to "கன்னி",
        Rasi.THULAA to "துலாம்",
        Rasi.VRISHCHIKA to "விருச்சிகம்",
        Rasi.DHANUS to "தனுசு",
        Rasi.MAKARA to "மகரம்",
        Rasi.KUMBHA to "கும்பம்",
        Rasi.MEENA to "மீனம்"
    )

    private val nakshatraEn = mapOf(
        Nakshatra.ASHWINI to "Ashwini",
        Nakshatra.BHARANI to "Bharani",
        Nakshatra.KRITHTHIKA to "Krittika",
        Nakshatra.ROHINI to "Rohini",
        Nakshatra.MRIGASHIRSHA to "Mrigashirsha",
        Nakshatra.AARDHRAA to "Ardra",
        Nakshatra.PUNARVASU to "Punarvasu",
        Nakshatra.PUSHYA to "Pushya",
        Nakshatra.ASHLESHA to "Ashlesha",
        Nakshatra.MAGHA to "Magha",
        Nakshatra.PURVA_PHALGUNI to "Purva Phalguni",
        Nakshatra.UTTARA_PHALGUNI to "Uttara Phalguni",
        Nakshatra.HASTHA to "Hasta",
        Nakshatra.CHITRA to "Chitra",
        Nakshatra.SWAATHI to "Swati",
        Nakshatra.VISHAKHA to "Vishakha",
        Nakshatra.ANURADHA to "Anuradha",
        Nakshatra.JYESHTHA to "Jyeshtha",
        Nakshatra.MULA to "Mula",
        Nakshatra.PURVA_ASHADA to "Purva Ashada",
        Nakshatra.UTTARA_ASHADA to "Uttara Ashada",
        Nakshatra.SHRAVANA to "Shravana",
        Nakshatra.DHANISHTA to "Dhanishta",
        Nakshatra.SHATABHISHA to "Shatabhisha",
        Nakshatra.PURVA_BHADRAPADA to "Purva Bhadrapada",
        Nakshatra.UTTARA_BHADRAPADA to "Uttara Bhadrapada",
        Nakshatra.REVATHI to "Revathi"
    )

    private val nakshatraTa = mapOf(
        Nakshatra.ASHWINI to "அசுவினி",
        Nakshatra.BHARANI to "பரணி",
        Nakshatra.KRITHTHIKA to "கிருத்திகை",
        Nakshatra.ROHINI to "ரோகிணி",
        Nakshatra.MRIGASHIRSHA to "மிருகசீரிஷம்",
        Nakshatra.AARDHRAA to "திருவாதிரை",
        Nakshatra.PUNARVASU to "புனர்பூசம்",
        Nakshatra.PUSHYA to "பூசம்",
        Nakshatra.ASHLESHA to "ஆயில்யம்",
        Nakshatra.MAGHA to "மகம்",
        Nakshatra.PURVA_PHALGUNI to "பூரம்",
        Nakshatra.UTTARA_PHALGUNI to "உத்திரம்",
        Nakshatra.HASTHA to "ஹஸ்தம்",
        Nakshatra.CHITRA to "சித்திரை",
        Nakshatra.SWAATHI to "ஸ்வாதி",
        Nakshatra.VISHAKHA to "விசாகம்",
        Nakshatra.ANURADHA to "அனுஷம்",
        Nakshatra.JYESHTHA to "கேட்டை",
        Nakshatra.MULA to "மூலம்",
        Nakshatra.PURVA_ASHADA to "பூராடம்",
        Nakshatra.UTTARA_ASHADA to "உத்திராடம்",
        Nakshatra.SHRAVANA to "திருவோணம்",
        Nakshatra.DHANISHTA to "அவிட்டம்",
        Nakshatra.SHATABHISHA to "சதயம்",
        Nakshatra.PURVA_BHADRAPADA to "பூரட்டாதி",
        Nakshatra.UTTARA_BHADRAPADA to "உத்திரட்டாதி",
        Nakshatra.REVATHI to "ரேவதி"
    )

    private val thithiEn = mapOf(
        Thithi.PRATHAMA to "Prathama",
        Thithi.DWITIYA to "Dwitiya",
        Thithi.TRITIYA to "Tritiya",
        Thithi.CHATURTHI to "Chaturthi",
        Thithi.PANCHAMI to "Panchami",
        Thithi.SHASHTI to "Shashti",
        Thithi.SAPTAMI to "Saptami",
        Thithi.ASHTAMI to "Ashtami",
        Thithi.NAVAMI to "Navami",
        Thithi.DASHAMI to "Dasami",
        Thithi.EKADASHI to "Ekadasi",
        Thithi.DWADASHI to "Dwadasi",
        Thithi.TRAYODASHI to "Trayodasi",
        Thithi.CHATURDASHI to "Chaturdasi",
        Thithi.PURNIMA to "Purnima",
        Thithi.AMAVASYA to "Amavasya"
    )

    private val thithiTa = mapOf(
        Thithi.PRATHAMA to "பிரதமை",
        Thithi.DWITIYA to "துவிதியை",
        Thithi.TRITIYA to "திரிதியை",
        Thithi.CHATURTHI to "சதுர்த்தி",
        Thithi.PANCHAMI to "பஞ்சமி",
        Thithi.SHASHTI to "சஷ்டி",
        Thithi.SAPTAMI to "சப்தமி",
        Thithi.ASHTAMI to "அஷ்டமி",
        Thithi.NAVAMI to "நவமி",
        Thithi.DASHAMI to "தசமி",
        Thithi.EKADASHI to "ஏகாதசி",
        Thithi.DWADASHI to "துவாதசி",
        Thithi.TRAYODASHI to "திரயோதசி",
        Thithi.CHATURDASHI to "சதுர்த்தசி",
        Thithi.PURNIMA to "பௌர்ணமி",
        Thithi.AMAVASYA to "அமாவாசை"
    )

    // For Yoga (27 Nithya Yogas)

    private val yogaEn = mapOf(
        Yoga.VISHKAMBHA to "Vishkambha",
        Yoga.PREETI to "Preethi",
        Yoga.AYUSHMAN to "Ayushman",
        Yoga.SAUBHAGYA to "Saubhagya",
        Yoga.SHOBHANA to "Shobhana",
        Yoga.ATIGANDA to "Athiganda",
        Yoga.SUKARMA to "Sukarma",
        Yoga.DHRITI to "Dhriti",
        Yoga.SHULA to "Shula",
        Yoga.GANDA to "Ganda",
        Yoga.VRIDDHI to "Vriddhi",
        Yoga.DHRUVA to "Dhruva",
        Yoga.VYAGHATA to "Vyaghata",
        Yoga.HARSHANA to "Harshana",
        Yoga.VAJRA to "Vajra",
        Yoga.SIDDHI to "Siddhi",
        Yoga.VYATIPATA to "Vyatipata",
        Yoga.VARIYAN to "Variyan",
        Yoga.PARIGHA to "Parigha",
        Yoga.SHIVA to "Shiva",
        Yoga.SIDDHA to "Siddha",
        Yoga.SADHYA to "Sadhya",
        Yoga.SHUBHA to "Shubha",
        Yoga.SHUKLA to "Shukla",
        Yoga.BRAHMA to "Brahma",
        Yoga.INDRA to "Indra",
        Yoga.VAIDHRITI to "Vaidhriti"
    )

    private val yogaTa = mapOf(
        Yoga.VISHKAMBHA to "விஷ்கம்பம்",
        Yoga.PREETI to "பிரீதி",
        Yoga.AYUSHMAN to "ஆயுஷ்மான்",
        Yoga.SAUBHAGYA to "சௌபாக்யம்",
        Yoga.SHOBHANA to "சோபனம்",
        Yoga.ATIGANDA to "அதிகண்டம்",
        Yoga.SUKARMA to "சுகர்மா",
        Yoga.DHRITI to "திரிதி",
        Yoga.SHULA to "சூலம்",
        Yoga.GANDA to "கண்டம்",
        Yoga.VRIDDHI to "விருத்தி",
        Yoga.DHRUVA to "துருவம்",
        Yoga.VYAGHATA to "வ்யாகதம்",
        Yoga.HARSHANA to "ஹர்ஷணம்",
        Yoga.VAJRA to "வஜ்ரம்",
        Yoga.SIDDHI to "சித்தி",
        Yoga.VYATIPATA to "வ்யதிபாதம்",
        Yoga.VARIYAN to "வரியான்",
        Yoga.PARIGHA to "பரிகம்",
        Yoga.SHIVA to "சிவம்",
        Yoga.SIDDHA to "சித்தம்",
        Yoga.SADHYA to "சாத்யம்",
        Yoga.SHUBHA to "சுபம்",
        Yoga.SHUKLA to "சுக்லம்",
        Yoga.BRAHMA to "பிரம்மம்",
        Yoga.INDRA to "இந்திரம்",
        Yoga.VAIDHRITI to "வைத்ரிதி"
    )

    // For Karana (11 Karanas)

    private val karanaEn = mapOf(
        Karana.BAVA to "Bava",
        Karana.BALAVA to "Balava",
        Karana.KAULAVA to "Kaulava",
        Karana.TAITILA to "Taitila",
        Karana.GARAJA to "Garaja",
        Karana.VANIJA to "Vanija",
        Karana.VISHTI to "Vishti",
        Karana.SHAKUNI to "Shakuni",
        Karana.CHATUSHPADA to "Chatushpada",
        Karana.NAGA to "Naaga",
        Karana.KIMSTHUGNA to "Kimsthugna"
    )

    private val karanaTa = mapOf(
        Karana.BAVA to "பவ",
        Karana.BALAVA to "பாலவ",
        Karana.KAULAVA to "கௌலவ",
        Karana.TAITILA to "தைதில",
        Karana.GARAJA to "கரஜ",
        Karana.VANIJA to "வணிஜ",
        Karana.VISHTI to "விஷ்டி",
        Karana.SHAKUNI to "சாகுனி",
        Karana.CHATUSHPADA to "சதுஷ்பாத",
        Karana.NAGA to "நாக",
        Karana.KIMSTHUGNA to "கிம்ஸ்துக்ன"
    )

    private val rahuSegmentMap = mapOf(
        DayOfWeek.SUNDAY to 8,
        DayOfWeek.MONDAY to 2,
        DayOfWeek.TUESDAY to 7,
        DayOfWeek.WEDNESDAY to 5,
        DayOfWeek.THURSDAY to 6,
        DayOfWeek.FRIDAY to 4,
        DayOfWeek.SATURDAY to 3
    )

    fun getRahukalamSegment(dayOfWeek: DayOfWeek): Int {
        return rahuSegmentMap[dayOfWeek] ?: 1 // Returns 1 as a safe fallback
    }

    private val yamaSegmentMap = mapOf(
        DayOfWeek.SUNDAY to 5,
        DayOfWeek.MONDAY to 4,
        DayOfWeek.TUESDAY to 3,
        DayOfWeek.WEDNESDAY to 2,
        DayOfWeek.THURSDAY to 1,
        DayOfWeek.FRIDAY to 7,
        DayOfWeek.SATURDAY to 6
    )

    fun getYamakandamSegment(dayOfWeek: DayOfWeek): Int {
        return yamaSegmentMap[dayOfWeek] ?: 1 // Returns 1 as a safe fallback
    }

    private val gulikaiSegmentMap = mapOf(
        DayOfWeek.SUNDAY to 6,
        DayOfWeek.MONDAY to 5,
        DayOfWeek.TUESDAY to 4,
        DayOfWeek.WEDNESDAY to 3,
        DayOfWeek.THURSDAY to 2,
        DayOfWeek.FRIDAY to 1,
        DayOfWeek.SATURDAY to 7
    )

    fun getGulikaiSegment(dayOfWeek: DayOfWeek): Int {
        return gulikaiSegmentMap[dayOfWeek] ?: 1 // Returns 1 as a safe fallback
    }

}
