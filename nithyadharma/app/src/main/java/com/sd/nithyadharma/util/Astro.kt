package com.sd.nithyadharma.util

import com.sd.nithyadharma.util.ContinuousShadbala.classify
import swisseph.*
import java.time.*
import kotlin.math.*

// -------------------------------------------------
// INPUT MODEL
// -------------------------------------------------

data class InputParams(
    val date: LocalDate,
    val time: LocalTime,
    val latitude: Double,
    val longitude: Double
)

val ZODIAC_SIGNS = listOf(
    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)

fun signIndex(lon: Double): Int {
    return (((lon % 360) + 360) % 360 / 30).toInt()
}

val NAKSHATRAS = listOf(
    "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
    "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
    "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
    "Mula", "Purva Ashada", "Uttara Ashada", "Shravana", "Dhanishta",
    "Shatabhisha", "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
)

fun nakshatraIndex(lon: Double): Int {
    val size = 360.0 / 27.0
    return ((lon % 360) / size).toInt()
}

enum class Planet(val displayName: String) {
    SUN("Sun"),
    MOON("Moon"),
    MARS("Mars"),
    MERCURY("Mercury"),
    JUPITER("Jupiter"),
    VENUS("Venus"),
    SATURN("Saturn"),
    RAHU("Rahu"),
    KETU("Ketu")
}

// -------------------------------------------------
// COSMIC STATE (raw astronomy snapshot)
// -------------------------------------------------

data class CosmicState(
    // input
    val time: LocalDateTime,
    val latitude: Double,
    val longitude: Double,

    // output
    val lagnaLon: Double,
    val longitudeMap: Map<Planet, Double>,     // ecliptic longitude
    val speedMap: Map<Planet, Double>,         // deg/day
    val altitudeMap: Map<Planet, Double>,      // degrees
    val distanceMap: Map<Planet, Double>,      // AU
    val declinationMap: Map<Planet, Double>    // degrees

) {
    fun longitudeOf(p: Planet): Double = longitudeMap[p] ?: 0.0

    // like speed and altitude, modify longitude to a map todo
    fun speedOf(p: Planet) = speedMap[p] ?: 0.0
    fun altitudeOf(p: Planet) = altitudeMap[p] ?: 0.0

    // todo later addition
    fun isRetrograde(p: Planet): Boolean = speedOf(p) < 0
}

data class PlanetState(
    val name: String,         // SUN, MOON, etc.
    val longitude: Double,    // ecliptic longitude
    val altitude: Double,     // altitude
    val speed: Double,        // motion
    val zodiacSign: String,   // e.g., Libra
    val degreeInSign: Double, // e.g., 5.48
    val nakshatra: String,    // e.g., Chitra
    val total: Double,        // total score
    val status: String,       // DOMINANT, etc.
    val dign: Double,
    val mot: Double,
    val vis: Double,
    val asp: Double,
    val phase: Double,
    val nak: Double,
    val retrograde: Boolean,
    val house: Int,
    val exaltDebil: String,      // "Exalted", "Debilitated", or ""
    val combustion: Boolean
)

data class ShadbalaScore(
    val total: Double,
    val status: String,

    val dign: Double,
    val mot: Double,
    val vis: Double,
    val asp: Double,
    val phase: Double,
    val nak: Double
)


fun zodiacFromLongitude(lon: Double): Pair<String, Double> {
    val norm = ((lon % 360) + 360) % 360
    val signIndex = (norm / 30).toInt()
    val sign = ZODIAC_SIGNS[signIndex]
    val degreeInSign = norm % 30
    return sign to degreeInSign
}

fun nakshatraFromLongitude(lon: Double): String {
    val norm = ((lon % 360) + 360) % 360
    val size = 360.0 / 27.0
    val index = (norm / size).toInt().coerceIn(0, 26)
    return NAKSHATRAS[index]
}
// -------------------------------------------------
// PANCHANGAM STATE (calendar math)
// -------------------------------------------------

data class PanchangamState(
    val vara: String,
    val tithi: Int,
    val nakshatra: Int,
    val yoga: Int,
    val karana: Int
)

// -------------------------------------------------
// ENGINE
// -------------------------------------------------

class CosmicEngine {

    private val swe = SwissEph()
    private val SID_FLAGS = SweConst.SEFLG_SWIEPH or SweConst.SEFLG_SIDEREAL

    init {
        swe.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0.0, 0.0)
    }

    private fun planetState(jd: Double, planet: Int): DoubleArray {
        val xx = DoubleArray(6)
        val err = StringBuffer()
        swe.swe_calc_ut(jd, planet, SID_FLAGS or SweConst.SEFLG_SPEED, xx, err)
        return xx
    }

    private fun calcAltitude(jd: Double, lat: Double, lon: Double, xx: DoubleArray): Double {
        val geopos = doubleArrayOf(lon, lat, 0.0)
        val xin = doubleArrayOf(xx[0], xx[1], xx[2])
        val xaz = DoubleArray(3)

        swe.swe_azalt(jd, SweConst.SE_EQU2HOR, geopos, 0.0, 0.0, xin, xaz)
        return xaz[1]
    }

    private fun toJulianDayUTC(zdt: ZonedDateTime): Double {
        val hour =
            zdt.hour +
                    zdt.minute / 60.0 +
                    zdt.second / 3600.0 +
                    zdt.nano / 3_600_000_000_000.0

        return SweDate(
            zdt.year,
            zdt.monthValue,
            zdt.dayOfMonth,
            hour
        ).julDay
    }

    private fun planetLon(jd: Double, planet: Int): Double {
        val xx = DoubleArray(6)
        val err = StringBuffer()
        swe.swe_calc_ut(jd, planet, SID_FLAGS, xx, err)
        return (xx[0] + 360.0) % 360.0
    }

    private fun computeLagna(jd: Double, lat: Double, lon: Double): Double {
        val cusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        swe.swe_houses(jd, SID_FLAGS, lat, lon, 'P'.code, cusps, ascmc)
        return (ascmc[0] + 360.0) % 360.0
    }

    private fun calcDeclination(jd: Double, planet: Int): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        swe.swe_calc_ut(
            jd,
            planet,
            SweConst.SEFLG_SWIEPH or SweConst.SEFLG_EQUATORIAL,
            xx,
            serr
        )

        // xx[1] = declination in degrees
        return xx[1]
    }

    fun compute(input: InputParams): Pair<CosmicState, PanchangamState> {

        val localDt = LocalDateTime.of(input.date, input.time)

        val zoned = localDt
            .atZone(ZoneId.of("Asia/Kolkata"))
            .withZoneSameInstant(ZoneOffset.UTC)

        val jd = toJulianDayUTC(zoned)

        val sunX = planetState(jd, SweConst.SE_SUN)
        val moonX = planetState(jd, SweConst.SE_MOON)
        val marsX = planetState(jd, SweConst.SE_MARS)
        val mercX = planetState(jd, SweConst.SE_MERCURY)
        val jupX = planetState(jd, SweConst.SE_JUPITER)
        val venX = planetState(jd, SweConst.SE_VENUS)
        val satX = planetState(jd, SweConst.SE_SATURN)
        val rahuX = planetState(jd, SweConst.SE_TRUE_NODE)

        val lagna = computeLagna(jd, input.latitude, input.longitude)

        val longitudeMap: Map<Planet, Double> by lazy {
            mapOf<Planet, Double>(
                Planet.SUN to sunX[0],
                Planet.MOON to moonX[0],
                Planet.MARS to marsX[0],
                Planet.MERCURY to mercX[0],
                Planet.JUPITER to jupX[0],
                Planet.VENUS to venX[0],
                Planet.SATURN to satX[0],
                Planet.RAHU to rahuX[0],
                Planet.KETU to -((rahuX[0] + 180.0) % 360.0)
            )
        }
        val speedMap: Map<Planet, Double> by lazy {
            mapOf<Planet, Double>(
                Planet.SUN to sunX[3],
                Planet.MOON to moonX[3],
                Planet.MARS to marsX[3],
                Planet.MERCURY to mercX[3],
                Planet.JUPITER to jupX[3],
                Planet.VENUS to venX[3],
                Planet.SATURN to satX[3],
                Planet.RAHU to rahuX[3],
                Planet.KETU to -rahuX[3]
            )
        }

        val altitudeMap: Map<Planet, Double> by lazy {
            mutableMapOf<Planet, Double>().apply {
                put(Planet.SUN,     calcAltitude(jd, input.latitude, input.longitude, sunX))
                put(Planet.MOON,    calcAltitude(jd, input.latitude, input.longitude, moonX))
                put(Planet.MARS,    calcAltitude(jd, input.latitude, input.longitude, marsX))
                put(Planet.MERCURY, calcAltitude(jd, input.latitude, input.longitude, mercX))
                put(Planet.JUPITER, calcAltitude(jd, input.latitude, input.longitude, jupX))
                put(Planet.VENUS,   calcAltitude(jd, input.latitude, input.longitude, venX))
                put(Planet.SATURN,  calcAltitude(jd, input.latitude, input.longitude, satX))
                put(Planet.RAHU,    calcAltitude(jd, input.latitude, input.longitude, rahuX))
                put(Planet.KETU,    calcAltitude(jd, input.latitude, input.longitude, rahuX))
            }
        }

        val distanceMap = mapOf(
            Planet.SUN to sunX[2],
            Planet.MOON to moonX[2],
            Planet.MARS to marsX[2],
            Planet.MERCURY to mercX[2],
            Planet.JUPITER to jupX[2],
            Planet.VENUS to venX[2],
            Planet.SATURN to satX[2],
            Planet.RAHU to rahuX[2],
            Planet.KETU to rahuX[2]
        )

        val declinationMap = mapOf(
            Planet.SUN to calcDeclination(jd, SweConst.SE_SUN),
            Planet.MOON to calcDeclination(jd, SweConst.SE_MOON),
            Planet.MARS to calcDeclination(jd, SweConst.SE_MARS),
            Planet.MERCURY to calcDeclination(jd, SweConst.SE_MERCURY),
            Planet.JUPITER to calcDeclination(jd, SweConst.SE_JUPITER),
            Planet.VENUS to calcDeclination(jd, SweConst.SE_VENUS),
            Planet.SATURN to calcDeclination(jd, SweConst.SE_SATURN),
            Planet.RAHU to calcDeclination(jd, SweConst.SE_TRUE_NODE),
            Planet.KETU to calcDeclination(jd, SweConst.SE_TRUE_NODE)
        )

        val cosmic = CosmicState(
            time = localDt,
            latitude = input.latitude,
            longitude = input.longitude,

            lagnaLon = lagna,

            longitudeMap = longitudeMap,
            speedMap = speedMap,
            altitudeMap = altitudeMap,
            distanceMap = distanceMap,
            declinationMap = declinationMap
        )

        val sun = longitudeMap[Planet.SUN] ?: 0.0
        val moon = longitudeMap[Planet.MOON] ?: 0.0

        val tithi = (((moon - sun + 360) % 360) / 12).toInt() + 1
        val nak = (moon / 13.333333).toInt() + 1
        val yoga = (((sun + moon) % 360) / 13.333333).toInt() + 1
        val karana = (((moon - sun + 360) % 360) / 6).toInt() + 1

        val vara = zoned.dayOfWeek.name

        val panchangam = PanchangamState(
            vara = vara,
            tithi = tithi,
            nakshatra = nak,
            yoga = yoga,
            karana = karana
        )

        return cosmic to panchangam
    }
}

fun printPlanetStates(states: List<PlanetState>) {
    println(
        "PLANET     LON       ALT      SPD    SIGN        DEG  NAKSHATRA         TOTAL  STATUS      DIGN    MOT    VIS    ASP     PHASE  NAK"
    )
    println("-".repeat(133))

    states.forEach { ps ->
        println(
            "%-8s  %6.2f°  %6.2f°  %5.1f  %-6s H%2d %-9s %6.2f %-15s   %5.1f   %-5.1f %-5.1f %-5.1f %-5.1f %-5.1f %-5.1f %s %s"
                .format(
                    ps.name,
                    ps.longitude,
                    ps.altitude,
                    ps.speed,
                    ps.zodiacSign,
                    ps.house,
                    ps.exaltDebil,
                    ps.degreeInSign,
                    ps.nakshatra,
                    ps.total,
                    ps.dign,
                    ps.mot,
                    ps.vis,
                    ps.asp,
                    ps.phase,
                    ps.nak,
                    if (ps.retrograde) "R" else "",
                    if (ps.combustion) "☀" else ""
                )
        )
    }

}

fun houseFromLongitude(planetLon: Double, lagnaLon: Double): Int {
    val norm = ((planetLon - lagnaLon + 360) % 360)
    return (norm / 30).toInt() + 1 // 1..12
}

private val EXALTATION = mapOf(
    Planet.SUN to 10.0,        // Aries 10° → absolute 10°
    Planet.MOON to 33.0,       // Taurus 3° → 30 + 3 = 33°
    Planet.MARS to 298.0,      // Capricorn 28° → 270 + 28 = 298°
    Planet.MERCURY to 165.0,   // Virgo 15° → 150 + 15
    Planet.JUPITER to 65.0,    // Cancer 5° → 60 + 5
    Planet.VENUS to 357.0,     // Pisces 27° → 330 + 27
    Planet.SATURN to 201.0,    // Libra 21° → 180 + 21
    Planet.RAHU to 0.0,
    Planet.KETU to 180.0
)

private val DEBILITATION = mapOf(
    Planet.SUN to 208.0,        // Libra 28° → 180 + 28
    Planet.MOON to 267.0,       // Scorpio 27° → 240 + 27
    Planet.MARS to 98.0,        // Cancer 8° → 90 + 8
    Planet.MERCURY to 345.0,    // Pisces 15° → 330 + 15
    Planet.JUPITER to 305.0,    // Capricorn 5° → 300 + 5
    Planet.VENUS to 162.0,      // Virgo 12° → 150 + 12
    Planet.SATURN to 5.0,       // Aries 5° → 0 + 5
    Planet.RAHU to 0.0,
    Planet.KETU to 180.0
)


fun circularDiff(a: Double, b: Double): Double {
    val diff = abs(a - b) % 360
    return if (diff > 180) 360 - diff else diff
}

fun exaltDebilStatus(planet: Planet, lon: Double): String {
    val norm = ((lon % 360) + 360) % 360
    val exalt = EXALTATION[planet] ?: Double.NaN
    val debil = DEBILITATION[planet] ?: Double.NaN
    return when {
        circularDiff(norm, exalt) < 10.0 -> "Exalted"  //todo check this
        circularDiff(norm, debil) < 10.0 -> "Debilitated"
        else -> ""
    }
}

fun isCombust(planet: Planet, planetLon: Double, sunLon: Double): Boolean {
    if (planet == Planet.SUN) return false
    val diff = abs((planetLon - sunLon + 360) % 360)
    return diff < 8.5 // threshold can be adjusted
}

fun mergeCosmicShadbala(
    cosmic: CosmicState,
    shadbalaScores: Map<Planet, ShadbalaScore>
): List<PlanetState> {

    return Planet.entries.map { planet ->

        val lon = cosmic. longitudeOf(planet)
        val alt = cosmic.altitudeMap[planet] ?: 0.0
        val spd = cosmic.speedMap[planet] ?: 0.0

        val (sign, deg) = zodiacFromLongitude(lon)
        val nak = nakshatraFromLongitude(lon)

        val score = shadbalaScores[planet] ?: ShadbalaScore(0.0, "", 0.0,0.0,0.0,0.0,0.0,0.0)

        PlanetState(
            name = planet.name,
            longitude = lon,
            altitude = alt,
            speed = spd,
            zodiacSign = sign,
            degreeInSign = deg,
            nakshatra = nak,

            total = score.total,
            status = score.status,
            dign = score.dign,
            mot = score.mot,
            vis = score.vis,
            asp = score.asp,
            phase = score.phase,
            nak = score.nak,
            retrograde = spd < 0,
            house = houseFromLongitude(lon, cosmic.lagnaLon),
            exaltDebil = exaltDebilStatus(planet, lon),
            combustion = isCombust(planet, lon, cosmic.longitudeMap[Planet.SUN]?: 0.0)
        )
    }
}


data class PlanetStrength(
    val lon: Double,
    val dignity: Double,
    val motion: Double,
    val visibility: Double,
    val aspect: Double,
    val phase: Double,
    val nakshatra: Double,
    val total: Double
)

fun PlanetStrength.toShadbalaScore(): ShadbalaScore =
    ShadbalaScore(
        total = total,
        status = classify(total),
        dign = dignity,
        mot = motion,
        vis = visibility,
        asp = aspect,
        phase = phase,
        nak = nakshatra
    )

object ContinuousShadbala {

    private val BASE = mapOf(
        Planet.SUN to 70.0,
        Planet.MOON to 75.0,
        Planet.MARS to 55.0,
        Planet.MERCURY to 65.0,
        Planet.JUPITER to 80.0,
        Planet.VENUS to 78.0,
        Planet.SATURN to 50.0,
        Planet.RAHU to 45.0,
        Planet.KETU to 45.0
    )

    fun classify(x: Double) = when {
        x >= 75 -> "DOMINANT"
        x >= 60 -> "STRONG"
        x >= 45 -> "MILD"
        x >= 30 -> "WEAK"
        else -> "VERY WEAK"
    }

    private fun delta(a: Double, b: Double): Double {
        val d = abs(a - b) % 360
        return min(d, 360 - d)
    }

    private fun dignity(lon: Double): Double {
        val size = 30.0
        val center = floor(lon / size) * size + size / 2
        val d = delta(lon, center)
        return 10 * exp(-d * d / 50)
    }

    private fun motion(speed: Double) = speed * 2

    private fun visibility(alt: Double): Double {
        return 8 * tanh(alt / 15.0)
    }

    private fun phase(lon: Double, sun: Double): Double {
        return 5 * cos(Math.toRadians(delta(lon, sun)))
    }

    private fun nak(lon: Double): Double {
        val size = 360.0 / 27.0
        val center = floor(lon / size) * size + size / 2
        val d = delta(lon, center)
        return 4 * exp(-d * d / 25)
    }

    private fun aspect(d: Double): Double {
        return 6 * exp(-(d - 120).pow(2) / 120) -
                7 * exp(-(d - 180).pow(2) / 120)
    }

    fun compute(c: CosmicState): Map<Planet, PlanetStrength> {

        val out = mutableMapOf<Planet, PlanetStrength>()

        for ((p, lon) in c.longitudeMap) {

            val d = dignity(lon)
            val m = motion(c.speedMap[p] ?: 0.0)
            val v = visibility(c.altitudeMap[p] ?: 0.0)
            val ph = phase(lon, c.longitudeMap[Planet.SUN]?: 0.0)
            val n = nak(lon)

            var a = 0.0
            for ((q, qlon) in c.longitudeMap) if (p != q)
                a += aspect(delta(lon, qlon))

            val raw = BASE[p]!! + d + m + v + ph + n + a

            val total = (50 + (raw - 50) * 0.70).coerceIn(0.0, 100.0)

            out[p] = PlanetStrength(lon, d, m, v, a, ph, n, total)
        }

        return out
    }
}

fun altitudeScore(alt: Double): Double =
    when {
        alt <= 0 -> 0.0
        alt >= 90 -> 1.0
        else -> kotlin.math.sin(Math.toRadians(alt))
    }

fun elongationScore(lon: Double, sunLon: Double): Double {
    val d = kotlin.math.abs((lon - sunLon + 540) % 360 - 180)
    return (d / 180.0).coerceIn(0.0, 1.0)
}

val MEAN_DISTANCE = mapOf(
    Planet.MOON to 0.00257,
    Planet.MERCURY to 0.39,
    Planet.VENUS to 0.72,
    Planet.SUN to 1.0,
    Planet.MARS to 1.52,
    Planet.JUPITER to 5.2,
    Planet.SATURN to 9.58,
    Planet.RAHU to 1.0,
    Planet.KETU to 1.0
)

fun distanceScore(p: Planet, dist: Double): Double {
    val mean = MEAN_DISTANCE[p] ?: 1.0
    return (mean / dist).coerceIn(0.2, 2.0) / 2.0
}

fun phaseScore(lon: Double, sunLon: Double): Double {
    val e = kotlin.math.abs((lon - sunLon + 540) % 360 - 180)
    return kotlin.math.sin(Math.toRadians(e))
}

val MEAN_SPEED = mapOf(
    Planet.MOON to 13.2,
    Planet.MERCURY to 1.2,
    Planet.VENUS to 1.2,
    Planet.SUN to 1.0,
    Planet.MARS to 0.52,
    Planet.JUPITER to 0.083,
    Planet.SATURN to 0.033,
    Planet.RAHU to 0.05,
    Planet.KETU to 0.05
)

fun speedScore(p: Planet, spd: Double): Double {
    val mean = MEAN_SPEED[p] ?: 1.0
    val r = kotlin.math.abs(spd) / mean
    return r.coerceIn(0.0, 2.0) / 2.0
}

fun declinationScore(dec: Double): Double {
    return kotlin.math.abs(dec).coerceIn(0.0, 28.0) / 28.0
}

data class PhysicalDignity(
    val altitude: Double,
    val elongation: Double,
    val distance: Double,
    val phase: Double,
    val speed: Double,
    val declination: Double,
    val total: Double
)

fun computePhysicalDignity(c: CosmicState): Map<Planet, PhysicalDignity> {

    val out = mutableMapOf<Planet, PhysicalDignity>()
    val sunLon = c.longitudeOf(Planet.SUN)

    for (p in Planet.values()) {

        val lon = c.longitudeOf(p)
        val alt = c.altitudeMap[p] ?: 0.0
        val spd = c.speedMap[p] ?: 0.0
        val dist = c.distanceMap[p] ?: 1.0
        val dec = c.declinationMap[p] ?: 0.0

        val a = altitudeScore(alt)
        val e = elongationScore(lon, sunLon)
        val d = distanceScore(p, dist)
        val ph = phaseScore(lon, sunLon)
        val s = speedScore(p, spd)
        val dc = declinationScore(dec)

        val total = (a + e + d + ph + s + dc) / 6.0

        out[p] = PhysicalDignity(a, e, d, ph, s, dc, total)
    }

    return out
}

// -------------------------------------------------
// CLI
// -------------------------------------------------

class Astro {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

            val input = InputParams(
                date = LocalDate.parse("1975-10-23"),
                time = LocalTime.parse("06:35"),
                latitude = 11.795542309330605,
                longitude = 77.81490270295421
            )

//            val input = InputParams(
//                date = LocalDate.parse("2006-03-20"),
//                time = LocalTime.parse("21:09"),
//                latitude = 42.053067438190894,
//                longitude = -88.1411851441599
//            )
            
            val engine = CosmicEngine()
            val (cosmic, panchangam) = engine.compute(input)

            println("Cosmic:")
            println(cosmic)

            println("\nPanchangam:")
            println(panchangam)

            val strengths = ContinuousShadbala.compute(cosmic)

            println("\n=== PLANET RANKING ===")

            val shadbalaScores: Map<Planet, ShadbalaScore> =
                strengths.mapValues { (_, v) -> v.toShadbalaScore() }

            val merged = mergeCosmicShadbala(cosmic, shadbalaScores)
            printPlanetStates(merged)

            val physical = computePhysicalDignity(cosmic)
            
            println("\n=== PHYSICAL DIGNITY (Observer-Based) ===\n")

            physical.entries
                .sortedByDescending { it.value.total }
                .forEachIndexed { i, (p, d) ->

                    println(
                        "%2d. %-8s total=%6.3f  alt=%5.3f  elon=%5.3f  dist=%5.3f  phase=%5.3f  speed=%5.3f  decl=%5.3f"
                            .format(
                                i + 1,
                                p.name,
                                d.total,
                                d.altitude,
                                d.elongation,
                                d.distance,
                                d.phase,
                                d.speed,
                                d.declination
                            )
                    )
                }

        }
    }
}




