package com.sd.nithyadharma.model

import com.sd.nithyadharma.util.CommonFunctions
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

// data classes
object PanchangaAttr {

    // enums

    enum class CalendarEvent {
        // Monthly tithi observances
        EKADASI,
        SASHTI,
        ASHTAMI,
        THEIPIRAI_ASHTAMI,
        PRADOSHAM,
        POUNAMI,
        AMAVASAI,
        KRITHIGAI,

        // Monthly and recurring special observances
        SANKATAHARA_CHATURTHI,
        MAASA_SHIVARATRI,

        // Tamil and South Indian festivals
        BHOGI,
        PONGAL,
        MAATTU_PONGAL,
        KAANUM_PONGAL,
        THAI_AMAVASAI,
        THAI_POOSAM,
        VASANTHA_PANCHAMI,
        MAASI_MAGAM,
        MAHA_SHIVARATRI,
        KARADAYAN_NONBU,
        TELUGU_NEW_YEAR,
        SRI_RAMA_NAVAMI,
        PANGUNI_UTHIRAM,
        TAMIL_NEW_YEAR,
        AKSHAYA_TRITIYA,
        CHITRA_POURNAMI,
        VAIGASI_VISAGAM,
        AADI_PERUKKU,
        AADI_AMAVASAI,
        AADI_POORAM,
        VARALAKSHMI_VIRATHAM,
        AAVANI_AVITTAM,
        GOKULASHTAMI,
        GANESH_CHATURTHI,
        MAHALAYA_AMAVASAI,
        NAVARATHIRI_AARAMBAM,
        SARASWATHI_POOJAI,
        VIJAYA_DASAMI,
        DEEPAVALI,
        KANDHA_SASHTI_AARAMBAM,
        KANDHA_SASHTI,
        KARTHIGAI_DEEPAM,
        VAIKUNDA_EKADASI,
        KOODARAVALLI,
        AARUDRA_DARSHANAM,
        TYAGARAJAR_ARADHANAI
    }
    
    enum class Samvatsara {
        PRABHAVA,
        VIBHAVA,
        SHUKLA,
        PRAMODUTA,
        PRAJOTPATTI,
        ANGIRASA,
        SHRIMUKHA,
        BHAVA,
        YUVA,
        DHATU,
        ISHWARA,
        BAHUDHANYA,
        PRAMATHI,
        VIKRAMA,
        VRISHA,
        CHITRABHANU,
        SWABHANU,
        TARANA,
        PARTHIVA,
        VYAYA,
        SARVAJIT,
        SARVADHARI,
        VIRODHI,
        VIKRUTI,
        KHARA,
        NANDANA,
        VIJAYA,
        JAYA,
        MANMATHA,
        DURMUKHI,
        HEVILAMBI,
        VILAMBI,
        VIKARI,
        SHARVARI,
        PLAVA,
        SHUBHAKRIT,
        SHOBHAKRIT,
        KRODHI,
        VISHVAVASU,
        PARABHAVA,
        PLAVANGA,
        KILAKA,
        SAUMYA,
        SADHARANA,
        VIRODHIKRIT,
        PARIDHAVI,
        PRAMADICHA,
        ANANDA,
        RAKSHASA,
        NALA,
        PINGALA,
        KALAYUKTI,
        SIDDHARTHI,
        RAUDRA,
        DURMATI,
        DUNDUBHI,
        RUDHIRODGARI,
        RAKTAKSHI,
        KRODHANA,
        AKSHAYA
    }

    enum class Ruthu {
        VASANTHA,
        GRISHMA,
        VARSHA,
        SHARAD,
        HEMANTHA,
        SHISHIRA
    }

    enum class Ayanam {
        UTTARAYANAM,
        DAKSHINAYANAM
    }

    enum class Paksha {
        SHUKLA,
        KRISHNA
    }

    enum class TamilMonth {
        CHITHIRAI,
        VAIKASI,
        AANI,
        AADI,
        AVANI,
        PURATTASI,
        IYPPASI,
        KARTHIGAI,
        MARGAZHI,
        THAI,
        MAASI,
        PANGUNI
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

    // this is the object holding the static panchangam for the day
    data class StaticPanchangam(
        val calcDttm: LocalDateTime = CommonFunctions.getCurrentTime(),
        val sunrise: LocalDateTime? = null,
        val sunset: LocalDateTime? = null,
        val vaara: Vaara,
        val rahuKalam: TimeRange? = null,
        val yamaGandam: TimeRange? = null,
        val gulikan: TimeRange? = null,
        val nallaNeram: List<TimeWindow>? = null
    )

    // this is the object holding the dynamic panchangam which changes every once a while
    data class DynamicPanchangam(
        val calcDttm: LocalDateTime = CommonFunctions.getCurrentTime(),
        val sunrise: LocalDateTime? = null,
        val sunset: LocalDateTime? = null,
        val tamilDay: Int? = null,
        val samvatsara: Samvatsara? = null, // 0..59; 0 = Prabhava
        val ruthu: Ruthu? = null,
        val ayanam: Ayanam? = null,
        val maasam: TamilMonth? = null,
        val paksha: Paksha? = null,
        val vaara: Vaara? = null,
        val thithi: Thithi? = null,
        val thithiEndTime: LocalDateTime? = null,
        val nakshatra: Nakshatra? = null,
        val nakshatraPaadha: Int? = 1,
        val nakshatraEndTime: LocalDateTime? = null,
        val chandrashtamaRasi: Rasi? = null,
        val yoga: Yoga? = null,
        val yogaEndTime: LocalDateTime? = null,
        val karana: Karana? = null,
        val karanaEndTime: LocalDateTime? = null,
        val expiryDttm: LocalDateTime = CommonFunctions.getCurrentTime().plusMinutes(30),
        val muhurthaDay: Boolean? = false,
        val score: Int = 0
    )

    // functions

    fun computeVaaraFromSunrise(now: LocalDateTime, sunrise: LocalDateTime): Vaara =
        when (if (now.isBefore(sunrise)) now.minusDays(1).dayOfWeek else now.dayOfWeek) {
            DayOfWeek.SUNDAY    -> Vaara.RAVI
            DayOfWeek.MONDAY    -> Vaara.SOMA
            DayOfWeek.TUESDAY   -> Vaara.MANGAL
            DayOfWeek.WEDNESDAY -> Vaara.BUDHA
            DayOfWeek.THURSDAY  -> Vaara.GURU
            DayOfWeek.FRIDAY    -> Vaara.SHUKRA
            DayOfWeek.SATURDAY  -> Vaara.SHANI
        }

    fun rasiName(r: Rasi, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> rasiEn[r]!!
            NDLanguage.TA -> rasiTa[r]!!
            NDLanguage.KA -> rasiKa[r]!!
            NDLanguage.HI -> rasiHi[r]!!
        }

    fun vaaraName(v: Vaara, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> vaaraEn[v]!!
            NDLanguage.TA -> vaaraTa[v]!!
            NDLanguage.KA -> vaaraKa[v]!!
            NDLanguage.HI -> vaaraHi[v]!!
        }

    fun Vaara.next(): Vaara {
        val values = Vaara.entries
        return values[(ordinal + 1) % values.size]
    }

    fun samvatsaraName(
        samvatsara: Samvatsara,
        lang: NDLanguage
    ): String =
        when (lang) {
            NDLanguage.EN -> samvatsaraEn[samvatsara]!!
            NDLanguage.TA -> samvatsaraTa[samvatsara]!!
            NDLanguage.KA -> samvatsaraKa[samvatsara]!!
            NDLanguage.HI -> samvatsaraHi[samvatsara]!!
        }

    fun ruthuName(r: Ruthu, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> ruthuEn[r]!!
            NDLanguage.TA -> ruthuTa[r]!!
            NDLanguage.KA -> ruthuKa[r]!!
            NDLanguage.HI -> ruthuHi[r]!!
        }

    fun ayanamName(a: Ayanam, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> ayanamEn[a]!!
            NDLanguage.TA -> ayanamTa[a]!!
            NDLanguage.KA -> ayanamKa[a]!!
            NDLanguage.HI -> ayanamHi[a]!!
        }

    fun maasamName(
        month: TamilMonth,
        lang: NDLanguage
    ): String =
        when (lang) {
            NDLanguage.EN -> tamilMonthEn[month]!!
            NDLanguage.TA -> tamilMonthTa[month]!!
            NDLanguage.KA -> tamilMonthKa[month]!!
            NDLanguage.HI -> tamilMonthHi[month]!!
        }

    fun pakshaName(p: Paksha, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> pakshaEn[p]!!
            NDLanguage.TA -> pakshaTa[p]!!
            NDLanguage.KA -> pakshaKa[p]!!
            NDLanguage.HI -> pakshaHi[p]!!
        }

    fun nakshatraName(n: Nakshatra, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> nakshatraEn[n]!!
            NDLanguage.TA -> nakshatraTa[n]!!
            NDLanguage.KA -> nakshatraKa[n]!!
            NDLanguage.HI -> nakshatraHi[n]!!
        }

    fun Nakshatra.next(): Nakshatra {
        val values = Nakshatra.entries
        return values[(ordinal + 1) % values.size]
    }

    fun tithiName(t: Thithi, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> thithiEn[t]!!
            NDLanguage.TA -> thithiTa[t]!!
            NDLanguage.KA -> thithiKa[t]!!
            NDLanguage.HI -> thithiHi[t]!!
        }

    fun Thithi.next(): Thithi {
        val values = Thithi.entries
        return values[(ordinal + 1) % values.size]
    }

    fun yogaName(y: Yoga, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> yogaEn[y]!!
            NDLanguage.TA -> yogaTa[y]!!
            NDLanguage.KA -> yogaKa[y]!!
            NDLanguage.HI -> yogaHi[y]!!
        }

    fun karanaName(k: Karana, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> karanaEn[k]!!
            NDLanguage.TA -> karanaTa[k]!!
            NDLanguage.KA -> karanaKa[k]!!
            NDLanguage.HI -> karanaHi[k]!!
        }

    /* ---------------- DATA ---------------- */

    private val samvatsaraEn = mapOf(
        Samvatsara.PRABHAVA to "Prabhava",
        Samvatsara.VIBHAVA to "Vibhava",
        Samvatsara.SHUKLA to "Shukla",
        Samvatsara.PRAMODUTA to "Pramoduta",
        Samvatsara.PRAJOTPATTI to "Prajotpatti",
        Samvatsara.ANGIRASA to "Angirasa",
        Samvatsara.SHRIMUKHA to "Shrimukha",
        Samvatsara.BHAVA to "Bhava",
        Samvatsara.YUVA to "Yuva",
        Samvatsara.DHATU to "Dhatu",
        Samvatsara.ISHWARA to "Ishwara",
        Samvatsara.BAHUDHANYA to "Bahudhanya",
        Samvatsara.PRAMATHI to "Pramathi",
        Samvatsara.VIKRAMA to "Vikrama",
        Samvatsara.VRISHA to "Vrisha",
        Samvatsara.CHITRABHANU to "Chitrabhanu",
        Samvatsara.SWABHANU to "Swabhanu",
        Samvatsara.TARANA to "Tarana",
        Samvatsara.PARTHIVA to "Parthiva",
        Samvatsara.VYAYA to "Vyaya",
        Samvatsara.SARVAJIT to "Sarvajit",
        Samvatsara.SARVADHARI to "Sarvadhari",
        Samvatsara.VIRODHI to "Virodhi",
        Samvatsara.VIKRUTI to "Vikriti",
        Samvatsara.KHARA to "Khara",
        Samvatsara.NANDANA to "Nandana",
        Samvatsara.VIJAYA to "Vijaya",
        Samvatsara.JAYA to "Jaya",
        Samvatsara.MANMATHA to "Manmatha",
        Samvatsara.DURMUKHI to "Durmukhi",
        Samvatsara.HEVILAMBI to "Hevilambi",
        Samvatsara.VILAMBI to "Vilambi",
        Samvatsara.VIKARI to "Vikari",
        Samvatsara.SHARVARI to "Sharvari",
        Samvatsara.PLAVA to "Plava",
        Samvatsara.SHUBHAKRIT to "Shubhakrit",
        Samvatsara.SHOBHAKRIT to "Shobhakrit",
        Samvatsara.KRODHI to "Krodhi",
        Samvatsara.VISHVAVASU to "Vishvavasu",
        Samvatsara.PARABHAVA to "Parabhava",
        Samvatsara.PLAVANGA to "Plavanga",
        Samvatsara.KILAKA to "Kilaka",
        Samvatsara.SAUMYA to "Saumya",
        Samvatsara.SADHARANA to "Sadharana",
        Samvatsara.VIRODHIKRIT to "Virodhikrit",
        Samvatsara.PARIDHAVI to "Paridhavi",
        Samvatsara.PRAMADICHA to "Pramadicha",
        Samvatsara.ANANDA to "Ananda",
        Samvatsara.RAKSHASA to "Rakshasa",
        Samvatsara.NALA to "Nala",
        Samvatsara.PINGALA to "Pingala",
        Samvatsara.KALAYUKTI to "Kalayukti",
        Samvatsara.SIDDHARTHI to "Siddharthi",
        Samvatsara.RAUDRA to "Raudra",
        Samvatsara.DURMATI to "Durmati",
        Samvatsara.DUNDUBHI to "Dundubhi",
        Samvatsara.RUDHIRODGARI to "Rudhirodgari",
        Samvatsara.RAKTAKSHI to "Raktakshi",
        Samvatsara.KRODHANA to "Krodhana",
        Samvatsara.AKSHAYA to "Akshaya"
    )

    private val samvatsaraTa = mapOf(
        Samvatsara.PRABHAVA to "பிரபவ",
        Samvatsara.VIBHAVA to "விபவ",
        Samvatsara.SHUKLA to "சுக்ல",
        Samvatsara.PRAMODUTA to "பிரமோதூத",
        Samvatsara.PRAJOTPATTI to "பிரஜோற்பத்தி",
        Samvatsara.ANGIRASA to "ஆங்கீரச",
        Samvatsara.SHRIMUKHA to "ஸ்ரீமுக",
        Samvatsara.BHAVA to "பவ",
        Samvatsara.YUVA to "யுவ",
        Samvatsara.DHATU to "தாது",
        Samvatsara.ISHWARA to "ஈஸ்வர",
        Samvatsara.BAHUDHANYA to "பஹுதான்ய",
        Samvatsara.PRAMATHI to "பிரமாதி",
        Samvatsara.VIKRAMA to "விக்கிரம",
        Samvatsara.VRISHA to "விருஷ",
        Samvatsara.CHITRABHANU to "சித்திரபானு",
        Samvatsara.SWABHANU to "ஸ்வபானு",
        Samvatsara.TARANA to "தாரண",
        Samvatsara.PARTHIVA to "பார்த்திப",
        Samvatsara.VYAYA to "வியய",
        Samvatsara.SARVAJIT to "சர்வஜித்",
        Samvatsara.SARVADHARI to "சர்வதாரி",
        Samvatsara.VIRODHI to "விரோதி",
        Samvatsara.VIKRUTI to "விக்ருதி",
        Samvatsara.KHARA to "கர",
        Samvatsara.NANDANA to "நந்தன",
        Samvatsara.VIJAYA to "விஜய",
        Samvatsara.JAYA to "ஜய",
        Samvatsara.MANMATHA to "மன்மத",
        Samvatsara.DURMUKHI to "துர்முகி",
        Samvatsara.HEVILAMBI to "ஹேவிளம்பி",
        Samvatsara.VILAMBI to "விளம்பி",
        Samvatsara.VIKARI to "விகாரி",
        Samvatsara.SHARVARI to "சார்வரி",
        Samvatsara.PLAVA to "பிலவ",
        Samvatsara.SHUBHAKRIT to "சுபகிருது",
        Samvatsara.SHOBHAKRIT to "சோபகிருது",
        Samvatsara.KRODHI to "குரோதி",
        Samvatsara.VISHVAVASU to "விசுவாவசு",
        Samvatsara.PARABHAVA to "பராபவ",
        Samvatsara.PLAVANGA to "பிலவங்க",
        Samvatsara.KILAKA to "கீலக",
        Samvatsara.SAUMYA to "சௌமிய",
        Samvatsara.SADHARANA to "சாதாரண",
        Samvatsara.VIRODHIKRIT to "விரோதிகிருது",
        Samvatsara.PARIDHAVI to "பரிதாபி",
        Samvatsara.PRAMADICHA to "பிரமாதீச",
        Samvatsara.ANANDA to "ஆனந்த",
        Samvatsara.RAKSHASA to "ராட்சச",
        Samvatsara.NALA to "நள",
        Samvatsara.PINGALA to "பிங்கள",
        Samvatsara.KALAYUKTI to "காளயுக்தி",
        Samvatsara.SIDDHARTHI to "சித்தார்த்தி",
        Samvatsara.RAUDRA to "ரௌத்திரி",
        Samvatsara.DURMATI to "துர்மதி",
        Samvatsara.DUNDUBHI to "துந்துபி",
        Samvatsara.RUDHIRODGARI to "ருதிரோத்காரி",
        Samvatsara.RAKTAKSHI to "ரக்தாட்சி",
        Samvatsara.KRODHANA to "குரோதன",
        Samvatsara.AKSHAYA to "அட்சய"
    )

    private val samvatsaraKa = mapOf(
        Samvatsara.PRABHAVA to "ಪ್ರಭವ",
        Samvatsara.VIBHAVA to "ವಿಭವ",
        Samvatsara.SHUKLA to "ಶುಕ್ಲ",
        Samvatsara.PRAMODUTA to "ಪ್ರಮೋದೂತ",
        Samvatsara.PRAJOTPATTI to "ಪ್ರಜೋತ್ಪತ್ತಿ",
        Samvatsara.ANGIRASA to "ಆಂಗೀರಸ",
        Samvatsara.SHRIMUKHA to "ಶ್ರೀಮುಖ",
        Samvatsara.BHAVA to "ಭವ",
        Samvatsara.YUVA to "ಯುವ",
        Samvatsara.DHATU to "ಧಾತು",
        Samvatsara.ISHWARA to "ಈಶ್ವರ",
        Samvatsara.BAHUDHANYA to "ಬಹುಧಾನ್ಯ",
        Samvatsara.PRAMATHI to "ಪ್ರಮಾಥಿ",
        Samvatsara.VIKRAMA to "ವಿಕ್ರಮ",
        Samvatsara.VRISHA to "ವೃಷ",
        Samvatsara.CHITRABHANU to "ಚಿತ್ರಭಾನು",
        Samvatsara.SWABHANU to "ಸ್ವಭಾನು",
        Samvatsara.TARANA to "ತಾರಣ",
        Samvatsara.PARTHIVA to "ಪಾರ್ಥಿವ",
        Samvatsara.VYAYA to "ವ್ಯಯ",
        Samvatsara.SARVAJIT to "ಸರ್ವಜಿತ್",
        Samvatsara.SARVADHARI to "ಸರ್ವಧಾರಿ",
        Samvatsara.VIRODHI to "ವಿರೋಧಿ",
        Samvatsara.VIKRUTI to "ವಿಕೃತಿ",
        Samvatsara.KHARA to "ಖರ",
        Samvatsara.NANDANA to "ನಂದನ",
        Samvatsara.VIJAYA to "ವಿಜಯ",
        Samvatsara.JAYA to "ಜಯ",
        Samvatsara.MANMATHA to "ಮನ್ಮಥ",
        Samvatsara.DURMUKHI to "ದುರ್ಮುಖಿ",
        Samvatsara.HEVILAMBI to "ಹೇವಿಳಂಬಿ",
        Samvatsara.VILAMBI to "ವಿಳಂಬಿ",
        Samvatsara.VIKARI to "ವಿಕಾರಿ",
        Samvatsara.SHARVARI to "ಶಾರ್ವರಿ",
        Samvatsara.PLAVA to "ಪ್ಲವ",
        Samvatsara.SHUBHAKRIT to "ಶುಭಕೃತ್",
        Samvatsara.SHOBHAKRIT to "ಶೋಭಕೃತ್",
        Samvatsara.KRODHI to "ಕ್ರೋಧಿ",
        Samvatsara.VISHVAVASU to "ವಿಶ್ವಾವಸು",
        Samvatsara.PARABHAVA to "ಪರಾಭವ",
        Samvatsara.PLAVANGA to "ಪ್ಲವಂಗ",
        Samvatsara.KILAKA to "ಕೀಲಕ",
        Samvatsara.SAUMYA to "ಸೌಮ್ಯ",
        Samvatsara.SADHARANA to "ಸಾಧಾರಣ",
        Samvatsara.VIRODHIKRIT to "ವಿರೋಧಿಕೃತ್",
        Samvatsara.PARIDHAVI to "ಪರಿಧಾವಿ",
        Samvatsara.PRAMADICHA to "ಪ್ರಮಾದೀಚ",
        Samvatsara.ANANDA to "ಆನಂದ",
        Samvatsara.RAKSHASA to "ರಾಕ್ಷಸ",
        Samvatsara.NALA to "ನಳ",
        Samvatsara.PINGALA to "ಪಿಂಗಳ",
        Samvatsara.KALAYUKTI to "ಕಾಲಯುಕ್ತಿ",
        Samvatsara.SIDDHARTHI to "ಸಿದ್ಧಾರ್ಥಿ",
        Samvatsara.RAUDRA to "ರೌದ್ರಿ",
        Samvatsara.DURMATI to "ದುರ್ಮತಿ",
        Samvatsara.DUNDUBHI to "ದುಂದುಭಿ",
        Samvatsara.RUDHIRODGARI to "ರುಧಿರೋದ್ಗಾರಿ",
        Samvatsara.RAKTAKSHI to "ರಕ್ತಾಕ್ಷಿ",
        Samvatsara.KRODHANA to "ಕ್ರೋಧನ",
        Samvatsara.AKSHAYA to "ಅಕ್ಷಯ"
    )

    private val samvatsaraHi = mapOf(
        Samvatsara.PRABHAVA to "प्रभव",
        Samvatsara.VIBHAVA to "विभव",
        Samvatsara.SHUKLA to "शुक्ल",
        Samvatsara.PRAMODUTA to "प्रमोदूत",
        Samvatsara.PRAJOTPATTI to "प्रजोत्पत्ति",
        Samvatsara.ANGIRASA to "आंगीरस",
        Samvatsara.SHRIMUKHA to "श्रीमुख",
        Samvatsara.BHAVA to "भाव",
        Samvatsara.YUVA to "युव",
        Samvatsara.DHATU to "धातु",
        Samvatsara.ISHWARA to "ईश्वर",
        Samvatsara.BAHUDHANYA to "बहुधान्य",
        Samvatsara.PRAMATHI to "प्रमाथी",
        Samvatsara.VIKRAMA to "विक्रम",
        Samvatsara.VRISHA to "वृष",
        Samvatsara.CHITRABHANU to "चित्रभानु",
        Samvatsara.SWABHANU to "स्वभानु",
        Samvatsara.TARANA to "तारण",
        Samvatsara.PARTHIVA to "पार्थिव",
        Samvatsara.VYAYA to "व्यय",
        Samvatsara.SARVAJIT to "सर्वजित्",
        Samvatsara.SARVADHARI to "सर्वधारी",
        Samvatsara.VIRODHI to "विरोधी",
        Samvatsara.VIKRUTI to "विकृति",
        Samvatsara.KHARA to "खर",
        Samvatsara.NANDANA to "नन्दन",
        Samvatsara.VIJAYA to "विजय",
        Samvatsara.JAYA to "जय",
        Samvatsara.MANMATHA to "मन्मथ",
        Samvatsara.DURMUKHI to "दुर्मुखी",
        Samvatsara.HEVILAMBI to "हेविलम्बी",
        Samvatsara.VILAMBI to "विलम्बी",
        Samvatsara.VIKARI to "विकारी",
        Samvatsara.SHARVARI to "शार्वरी",
        Samvatsara.PLAVA to "प्लव",
        Samvatsara.SHUBHAKRIT to "शुभकृत्",
        Samvatsara.SHOBHAKRIT to "शोभकृत्",
        Samvatsara.KRODHI to "क्रोधी",
        Samvatsara.VISHVAVASU to "विश्वावसु",
        Samvatsara.PARABHAVA to "पराभव",
        Samvatsara.PLAVANGA to "प्लवंग",
        Samvatsara.KILAKA to "कीलक",
        Samvatsara.SAUMYA to "सौम्य",
        Samvatsara.SADHARANA to "साधारण",
        Samvatsara.VIRODHIKRIT to "विरोधिकृत्",
        Samvatsara.PARIDHAVI to "परिधावी",
        Samvatsara.PRAMADICHA to "प्रमादीच",
        Samvatsara.ANANDA to "आनन्द",
        Samvatsara.RAKSHASA to "राक्षस",
        Samvatsara.NALA to "नल",
        Samvatsara.PINGALA to "पिंगल",
        Samvatsara.KALAYUKTI to "कालयुक्ति",
        Samvatsara.SIDDHARTHI to "सिद्धार्थी",
        Samvatsara.RAUDRA to "रौद्र",
        Samvatsara.DURMATI to "दुर्मति",
        Samvatsara.DUNDUBHI to "दुन्दुभि",
        Samvatsara.RUDHIRODGARI to "रुधिरोद्गारी",
        Samvatsara.RAKTAKSHI to "रक्ताक्षी",
        Samvatsara.KRODHANA to "क्रोधन",
        Samvatsara.AKSHAYA to "अक्षय"
    )

    private val ruthuEn = mapOf(
        Ruthu.VASANTHA to "Vasantha",
        Ruthu.GRISHMA to "Grishma",
        Ruthu.VARSHA to "Varsha",
        Ruthu.SHARAD to "Sharad",
        Ruthu.HEMANTHA to "Hemanta",
        Ruthu.SHISHIRA to "Shishira"
    )

    private val ruthuTa = mapOf(
        Ruthu.VASANTHA to "வசந்தம்",
        Ruthu.GRISHMA to "கிரீஷ்மம்",
        Ruthu.VARSHA to "வர்ஷம்",
        Ruthu.SHARAD to "சரத்",
        Ruthu.HEMANTHA to "ஹேமந்தம்",
        Ruthu.SHISHIRA to "சிசிரம்"
    )

    private val ruthuKa = mapOf(
        Ruthu.VASANTHA to "ವಸಂತ",
        Ruthu.GRISHMA to "ಗ್ರೀಷ್ಮ",
        Ruthu.VARSHA to "ವರ್ಷಾ",
        Ruthu.SHARAD to "ಶರದ್",
        Ruthu.HEMANTHA to "ಹೇಮಂತ",
        Ruthu.SHISHIRA to "ಶಿಶಿರ"
    )

    private val ruthuHi = mapOf(
        Ruthu.VASANTHA to "वसंत",
        Ruthu.GRISHMA to "ग्रीष्म",
        Ruthu.VARSHA to "वर्षा",
        Ruthu.SHARAD to "शरद",
        Ruthu.HEMANTHA to "हेमंत",
        Ruthu.SHISHIRA to "शिशिर"
    )

    private val ayanamEn = mapOf(
        Ayanam.UTTARAYANAM to "Uttarayanam",
        Ayanam.DAKSHINAYANAM to "Dakshinayanam"
    )

    private val ayanamTa = mapOf(
        Ayanam.UTTARAYANAM to "உத்தராயணம்",
        Ayanam.DAKSHINAYANAM to "தட்சிணாயணம்"
    )

    private val ayanamKa = mapOf(
        Ayanam.UTTARAYANAM to "ಉತ್ತರಾಯಣ",
        Ayanam.DAKSHINAYANAM to "ದಕ್ಷಿಣಾಯಣ"
    )

    private val ayanamHi = mapOf(
        Ayanam.UTTARAYANAM to "उत्तरायण",
        Ayanam.DAKSHINAYANAM to "दक्षिणायन"
    )

    private val tamilMonthEn = mapOf(
        TamilMonth.CHITHIRAI to "Chithirai",
        TamilMonth.VAIKASI to "Vaikasi",
        TamilMonth.AANI to "Aani",
        TamilMonth.AADI to "Aadi",
        TamilMonth.AVANI to "Avani",
        TamilMonth.PURATTASI to "Purattasi",
        TamilMonth.IYPPASI to "Iyppasi",
        TamilMonth.KARTHIGAI to "Karthigai",
        TamilMonth.MARGAZHI to "Margazhi",
        TamilMonth.THAI to "Thai",
        TamilMonth.MAASI to "Maasi",
        TamilMonth.PANGUNI to "Panguni"
    )

    private val tamilMonthTa = mapOf(
        TamilMonth.CHITHIRAI to "சித்திரை",
        TamilMonth.VAIKASI to "வைகாசி",
        TamilMonth.AANI to "ஆனி",
        TamilMonth.AADI to "ஆடி",
        TamilMonth.AVANI to "ஆவணி",
        TamilMonth.PURATTASI to "புரட்டாசி",
        TamilMonth.IYPPASI to "ஐப்பசி",
        TamilMonth.KARTHIGAI to "கார்த்திகை",
        TamilMonth.MARGAZHI to "மார்கழி",
        TamilMonth.THAI to "தை",
        TamilMonth.MAASI to "மாசி",
        TamilMonth.PANGUNI to "பங்குனி"
    )

    private val tamilMonthKa = mapOf(
        TamilMonth.CHITHIRAI to "ಚೈತ್ರ",        // Chaitra
        TamilMonth.VAIKASI to "ವೈಶಾಖ",         // Vaishakha
        TamilMonth.AANI to "ಜ್ಯೇಷ್ಠ",           // Jyeshtha
        TamilMonth.AADI to "ಆಷಾಢ",             // Ashadha
        TamilMonth.AVANI to "ಶ್ರಾವಣ",           // Shravana
        TamilMonth.PURATTASI to "ಭಾದ್ರಪದ",      // Bhadrapada
        TamilMonth.IYPPASI to "ಆಶ್ವಿನ",         // Ashvina
        TamilMonth.KARTHIGAI to "ಕಾರ್ತಿಕ",      // Kartika
        TamilMonth.MARGAZHI to "ಮಾರ್ಗಶಿರ",      // Margashirsha
        TamilMonth.THAI to "ಪೌಷ",              // Pausha
        TamilMonth.MAASI to "ಮಾಘ",             // Magha
        TamilMonth.PANGUNI to "ಫಾಲ್ಗುಣ"        // Phalguna
    )

    private val tamilMonthHi = mapOf(
        TamilMonth.CHITHIRAI to "चैत्र",        // Chaitra
        TamilMonth.VAIKASI to "वैशाख",         // Vaishakha
        TamilMonth.AANI to "ज्येष्ठ",           // Jyeshtha
        TamilMonth.AADI to "आषाढ",             // Ashadha
        TamilMonth.AVANI to "श्रावण",           // Shravana
        TamilMonth.PURATTASI to "भाद्रपद",      // Bhadrapada
        TamilMonth.IYPPASI to "आश्विन",         // Ashvina
        TamilMonth.KARTHIGAI to "कार्तिक",      // Kartika
        TamilMonth.MARGAZHI to "मार्गशीर्ष",    // Margashirsha ✅ (corrected)
        TamilMonth.THAI to "पौष",              // Pausha
        TamilMonth.MAASI to "माघ",             // Magha
        TamilMonth.PANGUNI to "फाल्गुन"        // Phalguna
    )

    private val pakshaEn = mapOf(
        Paksha.SHUKLA to "Shukla Paksha",
        Paksha.KRISHNA to "Krishna Paksha"
    )

    private val pakshaTa = mapOf(
        Paksha.SHUKLA to "வளர்பிறை",
        Paksha.KRISHNA to "தேய்பிறை"
    )

    private val pakshaKa = mapOf(
        Paksha.SHUKLA to "ಶುಕ್ಲ ಪಕ್ಷ",
        Paksha.KRISHNA to "ಕೃಷ್ಣ ಪಕ್ಷ"
    )

    private val pakshaHi = mapOf(
        Paksha.SHUKLA to "शुक्ल पक्ष",
        Paksha.KRISHNA to "कृष्ण पक्ष"
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

    private val vaaraKa = mapOf(
        Vaara.RAVI to "ಭಾನುವಾರ",
        Vaara.SOMA to "ಸೋಮವಾರ",
        Vaara.MANGAL to "ಮಂಗಳವಾರ",
        Vaara.BUDHA to "ಬುಧವಾರ",
        Vaara.GURU to "ಗುರುವಾರ",
        Vaara.SHUKRA to "ಶುಕ್ರವಾರ",
        Vaara.SHANI to "ಶನಿವಾರ"
    )

    private val vaaraHi = mapOf(
        Vaara.RAVI to "रविवार",
        Vaara.SOMA to "सोमवार",
        Vaara.MANGAL to "मंगलवार",
        Vaara.BUDHA to "बुधवार",
        Vaara.GURU to "गुरुवार",
        Vaara.SHUKRA to "शुक्रवार",
        Vaara.SHANI to "शनिवार"
    )

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

    private val rasiKa = mapOf(
        Rasi.MESHA to "ಮೇಷ",
        Rasi.VRISHABHA to "ವೃಷಭ",
        Rasi.MITHUNA to "ಮಿಥುನ",
        Rasi.KARKATAKA to "ಕರ್ಕಾಟಕ",
        Rasi.SIMHA to "ಸಿಂಹ",
        Rasi.KANYA to "ಕನ್ಯಾ",
        Rasi.THULAA to "ತುಲಾ",
        Rasi.VRISHCHIKA to "ವೃಶ್ಚಿಕ",
        Rasi.DHANUS to "ಧನುಸ್ಸು",
        Rasi.MAKARA to "ಮಕರ",
        Rasi.KUMBHA to "ಕುಂಭ",
        Rasi.MEENA to "ಮೀನ"
    )

    private val rasiHi = mapOf(
        Rasi.MESHA to "मेष",
        Rasi.VRISHABHA to "वृषभ",
        Rasi.MITHUNA to "मिथुन",
        Rasi.KARKATAKA to "कर्क",
        Rasi.SIMHA to "सिंह",
        Rasi.KANYA to "कन्या",
        Rasi.THULAA to "तुला",
        Rasi.VRISHCHIKA to "वृश्चिक",
        Rasi.DHANUS to "धनु",
        Rasi.MAKARA to "मकर",
        Rasi.KUMBHA to "कुंभ",
        Rasi.MEENA to "मीन"
    )

    private val nakshatraEn = mapOf(
        Nakshatra.ASHWINI to "Ashwini",
        Nakshatra.BHARANI to "Bharani",
        Nakshatra.KRITHTHIKA to "Kriththika",
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
        Nakshatra.ASHWINI to "அஸ்வினி",
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

    private val nakshatraKa = mapOf(
        Nakshatra.ASHWINI to "ಅಶ್ವಿನಿ",
        Nakshatra.BHARANI to "ಭರಣಿ",
        Nakshatra.KRITHTHIKA to "ಕೃತ್ತಿಕಾ",
        Nakshatra.ROHINI to "ರೋಹಿಣಿ",
        Nakshatra.MRIGASHIRSHA to "ಮೃಗಶಿರಾ",
        Nakshatra.AARDHRAA to "ಆರಿದ್ರಾ",
        Nakshatra.PUNARVASU to "ಪುನರ್ವಸು",
        Nakshatra.PUSHYA to "ಪುಷ್ಯ",
        Nakshatra.ASHLESHA to "ಆಶ್ಲೇಷಾ",
        Nakshatra.MAGHA to "ಮಖಾ",
        Nakshatra.PURVA_PHALGUNI to "ಪೂರ್ವಾ ಫಾಲ್ಗುಣಿ",
        Nakshatra.UTTARA_PHALGUNI to "ಉತ್ತರಾ ಫಾಲ್ಗುಣಿ",
        Nakshatra.HASTHA to "ಹಸ್ತಾ",
        Nakshatra.CHITRA to "ಚಿತ್ತಾ",
        Nakshatra.SWAATHI to "ಸ್ವಾತಿ",
        Nakshatra.VISHAKHA to "ವಿಶಾಖಾ",
        Nakshatra.ANURADHA to "ಅನುರಾಧಾ",
        Nakshatra.JYESHTHA to "ಜೇಷ್ಠಾ",
        Nakshatra.MULA to "ಮೂಲಾ",
        Nakshatra.PURVA_ASHADA to "ಪೂರ್ವಾಷಾಢ",
        Nakshatra.UTTARA_ASHADA to "ಉತ್ತರಾಷಾಢ",
        Nakshatra.SHRAVANA to "ಶ್ರವಣ",
        Nakshatra.DHANISHTA to "ಧನಿಷ್ಠಾ",
        Nakshatra.SHATABHISHA to "ಶತಭಿಷಾ",
        Nakshatra.PURVA_BHADRAPADA to "ಪೂರ್ವಾಭಾದ್ರ",
        Nakshatra.UTTARA_BHADRAPADA to "ಉತ್ತರಾಭಾದ್ರ",
        Nakshatra.REVATHI to "ರೇವತಿ"
    )

    private val nakshatraHi = mapOf(
        Nakshatra.ASHWINI to "अश्विनी",
        Nakshatra.BHARANI to "भरणी",
        Nakshatra.KRITHTHIKA to "कृत्तिका",
        Nakshatra.ROHINI to "रोहिणी",
        Nakshatra.MRIGASHIRSHA to "मृगशिरा",
        Nakshatra.AARDHRAA to "आर्द्रा",
        Nakshatra.PUNARVASU to "पुनर्वसु",
        Nakshatra.PUSHYA to "पुष्य",
        Nakshatra.ASHLESHA to "आश्लेषा",
        Nakshatra.MAGHA to "मघा",
        Nakshatra.PURVA_PHALGUNI to "पूर्वा फाल्गुनी",
        Nakshatra.UTTARA_PHALGUNI to "उत्तरा फाल्गुनी",
        Nakshatra.HASTHA to "हस्त",
        Nakshatra.CHITRA to "चित्रा",
        Nakshatra.SWAATHI to "स्वाति",
        Nakshatra.VISHAKHA to "विशाखा",
        Nakshatra.ANURADHA to "अनुराधा",
        Nakshatra.JYESHTHA to "ज्येष्ठा",
        Nakshatra.MULA to "मूल",
        Nakshatra.PURVA_ASHADA to "पूर्वाषाढा",
        Nakshatra.UTTARA_ASHADA to "उत्तराषाढा",
        Nakshatra.SHRAVANA to "श्रवण",
        Nakshatra.DHANISHTA to "धनिष्ठा",
        Nakshatra.SHATABHISHA to "शतभिषा",
        Nakshatra.PURVA_BHADRAPADA to "पूर्वा भाद्रपद",
        Nakshatra.UTTARA_BHADRAPADA to "उत्तरा भाद्रपद",
        Nakshatra.REVATHI to "रेवती"
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
        Thithi.DWITIYA to "த்விதியை",
        Thithi.TRITIYA to "த்ரிதியை",
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

    private val thithiKa = mapOf(
        Thithi.PRATHAMA to "ಪ್ರಥಮಾ",
        Thithi.DWITIYA to "ದ್ವಿತೀಯಾ",
        Thithi.TRITIYA to "ತೃತೀಯಾ",
        Thithi.CHATURTHI to "ಚತುರ್ಥಿ",
        Thithi.PANCHAMI to "ಪಂಚಮಿ",
        Thithi.SHASHTI to "ಷಷ್ಠಿ",
        Thithi.SAPTAMI to "ಸಪ್ತಮಿ",
        Thithi.ASHTAMI to "ಅಷ್ಟಮಿ",
        Thithi.NAVAMI to "ನವಮಿ",
        Thithi.DASHAMI to "ದಶಮಿ",
        Thithi.EKADASHI to "ಏಕಾದಶಿ",
        Thithi.DWADASHI to "ದ್ವಾದಶಿ",
        Thithi.TRAYODASHI to "ತ್ರಯೋದಶಿ",
        Thithi.CHATURDASHI to "ಚತುರ್ದಶಿ",
        Thithi.PURNIMA to "ಪೂರ್ಣಿಮಾ",
        Thithi.AMAVASYA to "ಅಮಾವಾಸ್ಯೆ"
    )

    private val thithiHi = mapOf(
        Thithi.PRATHAMA to "प्रथमा",
        Thithi.DWITIYA to "द्वितीया",
        Thithi.TRITIYA to "तृतीया",
        Thithi.CHATURTHI to "चतुर्थी",
        Thithi.PANCHAMI to "पंचमी",
        Thithi.SHASHTI to "षष्ठी",
        Thithi.SAPTAMI to "सप्तमी",
        Thithi.ASHTAMI to "अष्टमी",
        Thithi.NAVAMI to "नवमी",
        Thithi.DASHAMI to "दशमी",
        Thithi.EKADASHI to "एकादशी",
        Thithi.DWADASHI to "द्वादशी",
        Thithi.TRAYODASHI to "त्रयोदशी",
        Thithi.CHATURDASHI to "चतुर्दशी",
        Thithi.PURNIMA to "पूर्णिमा",
        Thithi.AMAVASYA to "अमावस्या"
    )

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
        Yoga.VISHKAMBHA to "விஷ்கம்ப",
        Yoga.PREETI to "ப்ரீதி",
        Yoga.AYUSHMAN to "ஆயுஷ்மான்",
        Yoga.SAUBHAGYA to "சௌபாக்ய",
        Yoga.SHOBHANA to "சோபன",
        Yoga.ATIGANDA to "அதிகண்ட",
        Yoga.SUKARMA to "சுகர்மா",
        Yoga.DHRITI to "த்ரிதி",
        Yoga.SHULA to "சூல",
        Yoga.GANDA to "கண்ட",
        Yoga.VRIDDHI to "வ்ருத்தி",
        Yoga.DHRUVA to "துருவ",
        Yoga.VYAGHATA to "வ்யாகத",
        Yoga.HARSHANA to "ஹர்ஷண",
        Yoga.VAJRA to "வஜ்ர",
        Yoga.SIDDHI to "சித்தி",
        Yoga.VYATIPATA to "வ்யதிபாத",
        Yoga.VARIYAN to "வரியான்",
        Yoga.PARIGHA to "பரிக",
        Yoga.SHIVA to "சிவ",
        Yoga.SIDDHA to "சித்த",
        Yoga.SADHYA to "சாத்ய",
        Yoga.SHUBHA to "சுப",
        Yoga.SHUKLA to "சுக்ல",
        Yoga.BRAHMA to "பிரம்ம",
        Yoga.INDRA to "இந்திர",
        Yoga.VAIDHRITI to "வைத்ரிதி"
    )

    private val yogaKa = mapOf(
        Yoga.VISHKAMBHA to "ವಿಷ್ಕಂಭ",
        Yoga.PREETI to "ಪ್ರೀತಿ",
        Yoga.AYUSHMAN to "ಆಯುಷ್ಮಾನ್",
        Yoga.SAUBHAGYA to "ಸೌಭಾಗ್ಯ",
        Yoga.SHOBHANA to "ಶೋಭನ",
        Yoga.ATIGANDA to "ಅತಿಗಂಡ",
        Yoga.SUKARMA to "ಸುಕರ್ಮಾ",
        Yoga.DHRITI to "ಧೃತಿ",
        Yoga.SHULA to "ಶೂಲ",
        Yoga.GANDA to "ಗಂಡ",
        Yoga.VRIDDHI to "ವೃದ್ಧಿ",
        Yoga.DHRUVA to "ಧ್ರುವ",
        Yoga.VYAGHATA to "ವ್ಯಾಘಾತ",
        Yoga.HARSHANA to "ಹರ್ಷಣ",
        Yoga.VAJRA to "ವಜ್ರ",
        Yoga.SIDDHI to "ಸಿದ್ದಿ",
        Yoga.VYATIPATA to "ವ್ಯತೀಪಾತ",
        Yoga.VARIYAN to "ವರಿಯಾನ್",
        Yoga.PARIGHA to "ಪರಿಘ",
        Yoga.SHIVA to "ಶಿವ",
        Yoga.SIDDHA to "ಸಿದ್ಧ",
        Yoga.SADHYA to "ಸಾಧ್ಯ",
        Yoga.SHUBHA to "ಶುಭ",
        Yoga.SHUKLA to "ಶುಕ್ಲ",
        Yoga.BRAHMA to "ಬ್ರಹ್ಮ",
        Yoga.INDRA to "ಇಂದ್ರ",
        Yoga.VAIDHRITI to "ವೈಧೃತಿ"
    )

    private val yogaHi = mapOf(
        Yoga.VISHKAMBHA to "विष्कंभ",
        Yoga.PREETI to "प्रीति",
        Yoga.AYUSHMAN to "आयुष्मान",
        Yoga.SAUBHAGYA to "सौभाग्य",
        Yoga.SHOBHANA to "शोभन",
        Yoga.ATIGANDA to "अतिगंड",
        Yoga.SUKARMA to "सुकर्मा",
        Yoga.DHRITI to "धृति",
        Yoga.SHULA to "शूल",
        Yoga.GANDA to "गंड",
        Yoga.VRIDDHI to "वृद्धि",
        Yoga.DHRUVA to "ध्रुव",
        Yoga.VYAGHATA to "व्याघात",
        Yoga.HARSHANA to "हर्षण",
        Yoga.VAJRA to "वज्र",
        Yoga.SIDDHI to "सिद्धि",
        Yoga.VYATIPATA to "व्यतीपात",
        Yoga.VARIYAN to "वरियान",
        Yoga.PARIGHA to "परिघ",
        Yoga.SHIVA to "शिव",
        Yoga.SIDDHA to "सिद्ध",
        Yoga.SADHYA to "साध्य",
        Yoga.SHUBHA to "शुभ",
        Yoga.SHUKLA to "शुक्ल",
        Yoga.BRAHMA to "ब्रह्म",
        Yoga.INDRA to "इंद्र",
        Yoga.VAIDHRITI to "वैधृति"
    )

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

    private val karanaKa = mapOf(
        Karana.BAVA to "ಬವ",
        Karana.BALAVA to "ಬಾಲವ",
        Karana.KAULAVA to "ಕೌಲವ",
        Karana.TAITILA to "ತೈತಿಲ",
        Karana.GARAJA to "ಗರಜ",
        Karana.VANIJA to "ವಣಿಜ",
        Karana.VISHTI to "ವಿಷ್ಟಿ",
        Karana.SHAKUNI to "ಶಕುನಿ",
        Karana.CHATUSHPADA to "ಚತುಷ್ಪಾದ",
        Karana.NAGA to "ನಾಗ",
        Karana.KIMSTHUGNA to "ಕಿಂಸ್ತುಘ್ನ"
    )

    private val karanaHi = mapOf(
        Karana.BAVA to "बव",
        Karana.BALAVA to "बालव",
        Karana.KAULAVA to "कौलव",
        Karana.TAITILA to "तैतिल",
        Karana.GARAJA to "गरज",
        Karana.VANIJA to "वणिज",
        Karana.VISHTI to "विष्टि",
        Karana.SHAKUNI to "शकुनि",
        Karana.CHATUSHPADA to "चतुष्पाद",
        Karana.NAGA to "नाग",
        Karana.KIMSTHUGNA to "किम्स्तुघ्न"
    )

    // Standard 90-minute intervals starting from 06:00
    val StaticRahuKalamMap = mapOf(
        DayOfWeek.MONDAY    to (LocalTime.of(7, 30)..LocalTime.of(9, 0)),
        DayOfWeek.TUESDAY   to (LocalTime.of(15, 0)..LocalTime.of(16, 30)),
        DayOfWeek.WEDNESDAY to (LocalTime.of(12, 0)..LocalTime.of(13, 30)),
        DayOfWeek.THURSDAY  to (LocalTime.of(13, 30)..LocalTime.of(15, 0)),
        DayOfWeek.FRIDAY    to (LocalTime.of(10, 30)..LocalTime.of(12, 0)),
        DayOfWeek.SATURDAY  to (LocalTime.of(9, 0)..LocalTime.of(10, 30)),
        DayOfWeek.SUNDAY    to (LocalTime.of(16, 30)..LocalTime.of(18, 0))
    )

    val StaticYamaGandamMap = mapOf(
        DayOfWeek.MONDAY    to (LocalTime.of(10, 30)..LocalTime.of(12, 0)),
        DayOfWeek.TUESDAY   to (LocalTime.of(9, 0)..LocalTime.of(10, 30)),
        DayOfWeek.WEDNESDAY to (LocalTime.of(7, 30)..LocalTime.of(9, 0)),
        DayOfWeek.THURSDAY  to (LocalTime.of(6, 0)..LocalTime.of(7, 30)),
        DayOfWeek.FRIDAY    to (LocalTime.of(15, 0)..LocalTime.of(16, 30)),
        DayOfWeek.SATURDAY  to (LocalTime.of(13, 30)..LocalTime.of(15, 0)),
        DayOfWeek.SUNDAY    to (LocalTime.of(12, 0)..LocalTime.of(13, 30))
    )

    val StaticGulikaiKalamMap = mapOf(
        DayOfWeek.MONDAY    to (LocalTime.of(13, 30)..LocalTime.of(15, 0)),
        DayOfWeek.TUESDAY   to (LocalTime.of(12, 0)..LocalTime.of(13, 30)),
        DayOfWeek.WEDNESDAY to (LocalTime.of(10, 30)..LocalTime.of(12, 0)),
        DayOfWeek.THURSDAY  to (LocalTime.of(9, 0)..LocalTime.of(10, 30)),
        DayOfWeek.FRIDAY    to (LocalTime.of(7, 30)..LocalTime.of(9, 0)),
        DayOfWeek.SATURDAY  to (LocalTime.of(6, 0)..LocalTime.of(7, 30)),
        DayOfWeek.SUNDAY    to (LocalTime.of(15, 0)..LocalTime.of(16, 30))
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
        return rahuSegmentMap[dayOfWeek] ?: 1
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
        return yamaSegmentMap[dayOfWeek] ?: 1
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
        return gulikaiSegmentMap[dayOfWeek] ?: 1
    }

}