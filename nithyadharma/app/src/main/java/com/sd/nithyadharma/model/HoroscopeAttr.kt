package com.sd.nithyadharma.model

import com.sd.nithyadharma.model.PanchangaAttr.Karana
import com.sd.nithyadharma.model.PanchangaAttr.Nakshatra
import com.sd.nithyadharma.model.PanchangaAttr.Rasi
import com.sd.nithyadharma.model.PanchangaAttr.Thithi
import com.sd.nithyadharma.model.PanchangaAttr.Vaara
import com.sd.nithyadharma.model.PanchangaAttr.Yoga
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

object HoroscopeAttr {

    const val NAKSHATRA_SIZE = 13.333333333333334 // 13°20'
    const val VIMSHOTTARI_TOTAL_YEARS = 120.0
    const val VIMSHOTTARI_YEAR_DAYS = 360.0

    @Serializable
    enum class Planet {
        SUN, MOON, MARS, MERCURY, JUPITER, VENUS, SATURN, RAAHU, KETHU
    }

    @Serializable
    private val planetEn = mapOf(
        Planet.SUN to "Sun",
        Planet.MOON to "Moon",
        Planet.MARS to "Mars",
        Planet.MERCURY to "Mercury",
        Planet.JUPITER to "Jupiter",
        Planet.VENUS to "Venus",
        Planet.SATURN to "Saturn",
        Planet.RAAHU to "Rahu",
        Planet.KETHU to "Ketu"
    )

    @Serializable
    private val planetShortEn = mapOf(
        Planet.SUN to "Su",
        Planet.MOON to "Mo",
        Planet.MARS to "Ma",
        Planet.MERCURY to "Me",
        Planet.JUPITER to "Ju",
        Planet.VENUS to "Ve",
        Planet.SATURN to "Sa",
        Planet.RAAHU to "Ra",
        Planet.KETHU to "Ke"
    )

    @Serializable
    private val planetTa = mapOf(
        Planet.SUN to "சூரியன்",
        Planet.MOON to "சந்திரன்",
        Planet.MARS to "செவ்வாய்",
        Planet.MERCURY to "புதன்",
        Planet.JUPITER to "குரு",
        Planet.VENUS to "சுக்கிரன்",
        Planet.SATURN to "சனி",
        Planet.RAAHU to "ராகு",
        Planet.KETHU to "கேது"
    )

    @Serializable
    private val planetShortTa = mapOf(
        Planet.SUN to "சூரி",
        Planet.MOON to "சந்",
        Planet.MARS to "செவ்",
        Planet.MERCURY to "புத",
        Planet.JUPITER to "குரு",
        Planet.VENUS to "சுக்",
        Planet.SATURN to "சனி",
        Planet.RAAHU to "ராகு",
        Planet.KETHU to "கேது"
    )

    @Serializable
    private val planetKa = mapOf(
        Planet.SUN to "ಸೂರ್ಯ",
        Planet.MOON to "ಚಂದ್ರ",
        Planet.MARS to "ಮಂಗಳ",
        Planet.MERCURY to "ಬುಧ",
        Planet.JUPITER to "ಗುರು",
        Planet.VENUS to "ಶುಕ್ರ",
        Planet.SATURN to "ಶನಿ",
        Planet.RAAHU to "ರಾಹು",
        Planet.KETHU to "ಕೇತು"
    )

    @Serializable
    private val planetShortKa = mapOf(
        Planet.SUN to "ಸೂ",
        Planet.MOON to "ಚಂ",
        Planet.MARS to "ಮಂ",
        Planet.MERCURY to "ಬು",
        Planet.JUPITER to "ಗು",
        Planet.VENUS to "ಶು",
        Planet.SATURN to "ಶ",
        Planet.RAAHU to "ರಾ",
        Planet.KETHU to "ಕೇ"
    )

    @Serializable
    private val planetHi = mapOf(
        Planet.SUN to "सूर्य",
        Planet.MOON to "चंद्र",
        Planet.MARS to "मंगल",
        Planet.MERCURY to "बुध",
        Planet.JUPITER to "गुरु",
        Planet.VENUS to "शुक्र",
        Planet.SATURN to "शनि",
        Planet.RAAHU to "राहु",
        Planet.KETHU to "केतु"
    )

    @Serializable
    private val planetShortHi = mapOf(
        Planet.SUN to "सू",
        Planet.MOON to "चं",
        Planet.MARS to "मं",
        Planet.MERCURY to "बु",
        Planet.JUPITER to "गु",
        Planet.VENUS to "शु",
        Planet.SATURN to "श",
        Planet.RAAHU to "रा",
        Planet.KETHU to "के"
    )

    enum class HoroscopePeriodType(val level: Int) {
        DASHA(0),
        BHUKTI(1),
        ANTHARA(2),
        SOOKSHMA(3),
        PRATYANTARA(4)
    }


    data class AstrologyAccess(
        val maxPeriodLevel: HoroscopePeriodType
    )

    val FREE_USER = AstrologyAccess(HoroscopePeriodType.BHUKTI)
    val PREMIUM_USER = AstrologyAccess(HoroscopePeriodType.ANTHARA)
    val SUPER_USER = AstrologyAccess(HoroscopePeriodType.PRATYANTARA) // future safe

    private val dbaEn = mapOf(
        HoroscopePeriodType.DASHA to "Mahadasha",
        HoroscopePeriodType.BHUKTI to "Bhukti",
        HoroscopePeriodType.ANTHARA to "Anthara",
        HoroscopePeriodType.SOOKSHMA to "Sookshma",
        HoroscopePeriodType.PRATYANTARA to "Prathyanthara",
    )

    private val dbaTa = mapOf(
        HoroscopePeriodType.DASHA to "தசை",
        HoroscopePeriodType.BHUKTI to "புக்தி",
        HoroscopePeriodType.ANTHARA to "அந்தரம்",
        HoroscopePeriodType.SOOKSHMA to "சூக்ஷ்மா",
        HoroscopePeriodType.PRATYANTARA to "பிரத்யந்தரா",
    )

    private val dbaKa = mapOf(
        HoroscopePeriodType.DASHA to "ಮಹಾದಶೆ",
        HoroscopePeriodType.BHUKTI to "ಭುಕ್ತಿ",
        HoroscopePeriodType.ANTHARA to "ಅಂತರ",
        HoroscopePeriodType.SOOKSHMA to "ಸೂಕ್ಷ್ಮ",
        HoroscopePeriodType.PRATYANTARA to "ಪ್ರತ್ಯಂತರ",
    )

    private val dbaHi = mapOf(
        HoroscopePeriodType.DASHA to "महादशा",
        HoroscopePeriodType.BHUKTI to "भुक्ति",
        HoroscopePeriodType.ANTHARA to "अंतर",
        HoroscopePeriodType.SOOKSHMA to "सूक्ष्म",
        HoroscopePeriodType.PRATYANTARA to "प्रत्यंतर",
    )

    fun dbaName(pt: HoroscopePeriodType, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> dbaEn[pt]!!
            NDLanguage.TA -> dbaTa[pt]!!
            NDLanguage.KA -> dbaKa[pt]!!
            NDLanguage.HI -> dbaHi[pt]!!
        }

    fun planetShortName(p: Planet, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> planetShortEn[p]!!
            NDLanguage.TA -> planetShortTa[p]!!
            NDLanguage.KA -> planetShortKa[p]!!
            NDLanguage.HI -> planetShortHi[p]!!
        }

    fun planetName(p: Planet, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> planetEn[p]!!
            NDLanguage.TA -> planetTa[p]!!
            NDLanguage.KA -> planetKa[p]!!
            NDLanguage.HI -> planetHi[p]!!
        }

    // --------------------------
    // INPUT OBJECT TO HOROSCOPE
    // --------------------------

    data class HoroscopeInputParams(
        val name: String,
        val date: LocalDate,
        val time: LocalTime,
        val latitude: Double,
        val longitude: Double
    )

    // ------------------------------------
    // OUTPUT OBJECTS FROM HOROSCOPE ENGINE
    // ------------------------------------

    data class PlanetPosition(
        val planet: Planet,
        val rasi: Rasi,
        val degree: Double,
        val nakshatra: Nakshatra
    )

    data class Panchanga(
        val vara: Vaara,
        val tithi: Thithi,
        val nakshatra: Nakshatra,
        val yoga: Yoga,
        val karana: Karana
    )

    data class HoroscopeChart(
        val panchanga: Panchanga,
        val lagna: Rasi,
        val planets: List<PlanetPosition>,
        val dbaPeriods: List<HoroscopePeriod>
    )

    val vimshottariDashaYears = mapOf(
        Planet.KETHU to 7.0,
        Planet.VENUS to 20.0,
        Planet.SUN to 6.0,
        Planet.MOON to 10.0,
        Planet.MARS to 7.0,
        Planet.RAAHU to 18.0,
        Planet.JUPITER to 16.0,
        Planet.SATURN to 19.0,
        Planet.MERCURY to 17.0
    )

    val vimshottariDashaOrder = listOf(
        Planet.KETHU,
        Planet.VENUS,
        Planet.SUN,
        Planet.MOON,
        Planet.MARS,
        Planet.RAAHU,
        Planet.JUPITER,
        Planet.SATURN,
        Planet.MERCURY
    )

//    val nakshatraToDashaLord: Map<Nakshatra, Planet> =
//        Nakshatra.values().mapIndexed { index, nak ->
//            nak to vimshottariDashaOrder[index % vimshottariDashaOrder.size]
//        }.toMap()

//    data class DashaBukthiAntharaPeriods(
//        val planet: Planet,
//        val startJulianDay: Double,
//        val endJulianDay: Double,
//        val bukthis: List<DashaBukthiAntharaPeriods> = emptyList()
//    )

    data class HoroscopePeriod(
        val type: HoroscopePeriodType,
        val planet: Planet,
        val startJulianDay: Double,
        val endJulianDay: Double,
        val children: List<HoroscopePeriod> = emptyList()
    ) {
        init {
            children.forEach { child ->
                require(child.type.level == this.type.level + 1) {
                    "Invalid hierarchy: ${child.type} cannot be child of ${this.type}"
                }
            }
        }
    }

    // repeats thrice in order
    val nakshatraLords = listOf(
        Planet.KETHU,     // Ashwini
        Planet.VENUS,    // Bharani
        Planet.SUN,      // Krittika
        Planet.MOON,     // Rohini
        Planet.MARS,     // Mrigashirsha
        Planet.RAAHU,    // Ardra
        Planet.JUPITER,  // Punarvasu
        Planet.SATURN,   // Pushya
        Planet.MERCURY   // Ashlesha
    )

    fun getNakshatraLord(nakIndex: Int): Planet =
        nakshatraLords[nakIndex % 9]

    data class NakshatraBalance(
        val nakIndex: Int,
        val balanceFraction: Double
    )
}