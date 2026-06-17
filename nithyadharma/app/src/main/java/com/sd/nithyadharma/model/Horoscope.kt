package com.sd.nithyadharma.model

import com.sd.nithyadharma.model.Horoscope.HoroscopePeriodType
import com.sd.nithyadharma.model.PanchangaAttributes.Karana
import com.sd.nithyadharma.model.PanchangaAttributes.Nakshatra
import com.sd.nithyadharma.model.PanchangaAttributes.Rasi
import com.sd.nithyadharma.model.PanchangaAttributes.Thithi
import com.sd.nithyadharma.model.PanchangaAttributes.Vaara
import com.sd.nithyadharma.model.PanchangaAttributes.Yoga
import java.time.LocalDate
import java.time.LocalTime

object Horoscope {

    const val NAKSHATRA_SIZE = 13.333333333333334 // 13°20'
    const val VIMSHOTTARI_TOTAL_YEARS = 120.0
    const val VIMSHOTTARI_YEAR_DAYS = 360.0

    enum class Planet {
        SUN, MOON, MARS, MERCURY, JUPITER, VENUS, SATURN, RAHU, KETU
    }

    private val planetEn = mapOf(
        Planet.SUN to "Sun",
        Planet.MOON to "Moon",
        Planet.MARS to "Mars",
        Planet.MERCURY to "Mercury",
        Planet.JUPITER to "Jupiter",
        Planet.VENUS to "Venus",
        Planet.SATURN to "Saturn",
        Planet.RAHU to "Rahu",
        Planet.KETU to "Ketu"
    )

    private val planetShortEn = mapOf(
        Planet.SUN to "Su",
        Planet.MOON to "Mo",
        Planet.MARS to "Ma",
        Planet.MERCURY to "Me",
        Planet.JUPITER to "Ju",
        Planet.VENUS to "Ve",
        Planet.SATURN to "Sa",
        Planet.RAHU to "Ra",
        Planet.KETU to "Ke"
    )

    private val planetTa = mapOf(
        Planet.SUN to "சூரியன்",
        Planet.MOON to "சந்திரன்",
        Planet.MARS to "செவ்வாய்",
        Planet.MERCURY to "புதன்",
        Planet.JUPITER to "குரு",
        Planet.VENUS to "சுக்கிரன்",
        Planet.SATURN to "சனி",
        Planet.RAHU to "ராகு",
        Planet.KETU to "கேது"
    )

    private val planetShortTa = mapOf(
        Planet.SUN to "சூரி",
        Planet.MOON to "சந்",
        Planet.MARS to "செவ்",
        Planet.MERCURY to "புத",
        Planet.JUPITER to "குரு",
        Planet.VENUS to "சுக்",
        Planet.SATURN to "சனி",
        Planet.RAHU to "ராகு",
        Planet.KETU to "கேது"
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

    fun dbaName(pt: HoroscopePeriodType, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> dbaEn[pt]!!
            NDLanguage.TA -> dbaTa[pt]!!
        }

    fun planetShortName(p: Planet, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> planetShortEn[p]!!
            NDLanguage.TA -> planetShortTa[p]!!
        }

    fun planetName(p: Planet, lang: NDLanguage): String =
        when (lang) {
            NDLanguage.EN -> planetEn[p]!!
            NDLanguage.TA -> planetTa[p]!!
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
        Planet.KETU to 7.0,
        Planet.VENUS to 20.0,
        Planet.SUN to 6.0,
        Planet.MOON to 10.0,
        Planet.MARS to 7.0,
        Planet.RAHU to 18.0,
        Planet.JUPITER to 16.0,
        Planet.SATURN to 19.0,
        Planet.MERCURY to 17.0
    )

    val vimshottariDashaOrder = listOf(
        Planet.KETU,
        Planet.VENUS,
        Planet.SUN,
        Planet.MOON,
        Planet.MARS,
        Planet.RAHU,
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
        Planet.KETU,     // Ashwini
        Planet.VENUS,    // Bharani
        Planet.SUN,      // Krittika
        Planet.MOON,     // Rohini
        Planet.MARS,     // Mrigashirsha
        Planet.RAHU,     // Ardra
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