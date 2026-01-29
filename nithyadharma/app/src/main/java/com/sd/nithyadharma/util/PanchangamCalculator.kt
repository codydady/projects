package com.sd.nithyadharma.util

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.sd.nithyadharma.model.Astrology.computeVaaraFromSunrise
import com.sd.nithyadharma.model.Astrology.getGulikaiSegment
import com.sd.nithyadharma.model.Astrology.getRahukalamSegment
import com.sd.nithyadharma.model.Astrology.getYamakandamSegment
import com.sd.nithyadharma.model.Karana
import com.sd.nithyadharma.model.Nakshatra
import com.sd.nithyadharma.model.Paksha
import com.sd.nithyadharma.model.Rasi
import com.sd.nithyadharma.model.Thithi
import com.sd.nithyadharma.model.TimeRange
import com.sd.nithyadharma.model.Vaara
import com.sd.nithyadharma.model.Yoga
import com.sd.nithyadharma.util.Constants.RASI_DEGREES_PER_SEGMENT
import com.sd.nithyadharma.util.Constants.YOGA_DEGREES
import com.sd.nithyadharma.util.PanchangamCalculator.lunarPhaseDeg
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import swisseph.DblObj
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.floor
import kotlin.math.roundToLong
import java.time.Duration
import java.time.LocalDateTime

fun thithiScore(thithi: Thithi): Int =
    when (thithi) {
        Thithi.DWITIYA,
        Thithi.TRITIYA,
        Thithi.PANCHAMI,
        Thithi.SAPTAMI,
        Thithi.DASHAMI,
        Thithi.EKADASHI -> 30

        Thithi.PRATHAMA,
        Thithi.SHASHTI,
        Thithi.NAVAMI,
        Thithi.DWADASHI,
        Thithi.TRAYODASHI,
        Thithi.PURNIMA -> 20

        Thithi.CHATURTHI,
        Thithi.CHATURDASHI -> 10

        Thithi.ASHTAMI,
        Thithi.AMAVASYA -> 5
    }

enum class NakshatraQuality(val score: Int) {
    GOOD(25),
    BAD(10),
    NEUTRAL(15)
}

val nakshatraQualityMap: Map<Nakshatra, NakshatraQuality> = mapOf(

    // ✅ Good Nakshatras
    Nakshatra.ROHINI to NakshatraQuality.GOOD,
    Nakshatra.UTTARA_PHALGUNI to NakshatraQuality.GOOD,
    Nakshatra.HASTHA to NakshatraQuality.GOOD,
    Nakshatra.SWAATHI to NakshatraQuality.GOOD,
    Nakshatra.ANURADHA to NakshatraQuality.GOOD,
    Nakshatra.SHRAVANA to NakshatraQuality.GOOD,
    Nakshatra.REVATHI to NakshatraQuality.GOOD,

    // ❌ Bad Nakshatras
    Nakshatra.ASHLESHA to NakshatraQuality.BAD,
    Nakshatra.MAGHA to NakshatraQuality.BAD,
    Nakshatra.JYESHTHA to NakshatraQuality.BAD,
    Nakshatra.MULA to NakshatraQuality.BAD,
    Nakshatra.PURVA_BHADRAPADA to NakshatraQuality.BAD
)

fun nakshatraScore(nakshatra: Nakshatra): Int =
    nakshatraQualityMap[nakshatra]?.score
        ?: NakshatraQuality.NEUTRAL.score

enum class YogaQuality(val score: Int) {
    GOOD(15),
    BAD(5),
    NEUTRAL(10)
}
val yogaQualityMap: Map<Yoga, YogaQuality> = mapOf(

    // ✅ Highly auspicious yogas
    Yoga.SHUBHA to YogaQuality.GOOD,
    Yoga.SHUKLA to YogaQuality.GOOD,
    Yoga.BRAHMA to YogaQuality.GOOD,
    Yoga.INDRA to YogaQuality.GOOD,

    // ❌ Inauspicious yogas
    Yoga.VYATIPATA to YogaQuality.BAD,
    Yoga.PARIGHA to YogaQuality.BAD,
    Yoga.VAJRA to YogaQuality.BAD,
    Yoga.VYAGHATA to YogaQuality.BAD
)

fun yogaScore(yoga: Yoga): Int =
    yogaQualityMap[yoga]?.score
        ?: YogaQuality.NEUTRAL.score

enum class KaranaQuality(val score: Int) {
    GOOD(10),
    BAD(5),
    NEUTRAL(7)
}

val karanaQualityMap: Map<Karana, KaranaQuality> = mapOf(

    // ✅ Auspicious / functional
    Karana.BAVA to KaranaQuality.GOOD,
    Karana.BALAVA to KaranaQuality.GOOD,
    Karana.KAULAVA to KaranaQuality.GOOD,
    Karana.TAITILA to KaranaQuality.GOOD,
    Karana.VANIJA to KaranaQuality.GOOD,

    // ❌ Inauspicious
    Karana.VISHTI to KaranaQuality.BAD,
    Karana.SHAKUNI to KaranaQuality.BAD,
    Karana.CHATUSHPADA to KaranaQuality.BAD,
    Karana.NAGA to KaranaQuality.BAD
)

fun karanaScore(karana: Karana): Int =
    karanaQualityMap[karana]?.score
        ?: KaranaQuality.NEUTRAL.score

enum class VaaraQuality(val score: Int) {
    EXCELLENT(10),
    GOOD(7),
    WEAK(5)
}

val vaaraQualityMap: Map<Vaara, VaaraQuality> = mapOf(
    Vaara.RAVI   to VaaraQuality.EXCELLENT, // Sunday
    Vaara.SOMA   to VaaraQuality.EXCELLENT, // Monday
    Vaara.GURU   to VaaraQuality.EXCELLENT, // Thursday
    Vaara.SHUKRA to VaaraQuality.EXCELLENT, // Friday

    Vaara.MANGAL to VaaraQuality.GOOD,      // Tuesday
    Vaara.BUDHA  to VaaraQuality.GOOD,      // Wednesday

    Vaara.SHANI  to VaaraQuality.WEAK       // Saturday
)

//fun tithiScoreForNallaNeram(tithi: String): Int =
//    when {
//        tithi.contains("Pournami") -> 3
//        tithi.contains("Amavasya") -> -3
//        else -> 1
//    }
//
//fun yogaScoreForNallaNeram(yoga: String): Int =
//    when (yoga) {
//        "Siddha", "Shubha", "Variyan" -> 2
//        "Vyatipata", "Vaidhruti" -> -3
//        else -> 0
//    }

//private val formatter = DateTimeFormatter.ofPattern("d MMM h:mm a")
private const val SID_FLAGS = SweConst.SEFLG_SWIEPH or SweConst.SEFLG_SIDEREAL

private fun julianDayUtToLocalDttm(jdUt: Double): LocalDateTime {
    val millis = ((jdUt - 2440587.5) * 86400000.0).roundToLong()

    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.of("Asia/Kolkata"))
        .toLocalDateTime()
}

// this is the main object holding the panchangam for the calculated date and time , which happens to be now
data class StaticPanchangam(
    val calcDttm: LocalDateTime = LocalDateTime.now(),
    val sunrise: LocalDateTime? = null,
    val sunset: LocalDateTime? = null,
    val vaara: Vaara ,
    val rahuKalam: TimeRange? = null,
    val yamaGandam: TimeRange? = null,
    val gulikan: TimeRange? = null,
    val nallaNeram: TimeRange? = null
)

// this is the main object holding the panchangam for the calculated date and time , which happens to be now
data class DynamicPanchangam(
    val calcDttm: LocalDateTime = LocalDateTime.now(),
    val sunrise: LocalDateTime? = null,
    val sunset: LocalDateTime? = null,
    val janmaRasi: Rasi? = null,
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
    val expiryDttm: LocalDateTime = LocalDateTime.now().plusMinutes(30),
    val score: Int = 0   // for score based on vara , thithi, nakshathra , yoga , karana & chandrashtama
)

fun currentJulianDayUt(): Double {
    val nowUtc = ZonedDateTime
        .now(ZoneId.of("Asia/Kolkata"))
        .withZoneSameInstant(ZoneOffset.UTC)

    val hourDecimal =
        nowUtc.hour +
                nowUtc.minute / 60.0 +
                nowUtc.second / 3600.0 +
                nowUtc.nano / 3_600_000_000_000.0

    return SweDate(
        nowUtc.year,
        nowUtc.monthValue,
        nowUtc.dayOfMonth,
        hourDecimal
    ).julDay
}

object PanchangamCalculator {
    private const val TAG = "PanchangamCalculator"
    private lateinit var swissEph: SwissEph
    private lateinit var applicationContext: Context

    // Chennai Coordinates (approximate, you might want more precise ones)
    private const val LATITUDE_CHENNAI = 13.0827 // North
    private const val LONGITUDE_CHENNAI = 80.2707 // East
    private const val ALTITUDE_CHENNAI = 7.0 // meters above sea level (approx)

    // --- Public Initialization ---
    fun initializeEphimeris(context: Context) {
        this.applicationContext = context.applicationContext

        synchronized(this) {
            if (!::swissEph.isInitialized) {
                swissEph = SwissEph()
                try {
                    val ephePath = "${context.applicationContext.filesDir}/ephe"
                    val epheDir = File(ephePath)
                    if (!epheDir.exists()) {
                        epheDir.mkdirs()
                    }
                    swissEph.swe_set_ephe_path(ephePath)
                    Log.d(TAG, "Swiss Ephemeris path set to: $ephePath")
                    swissEph.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0.0, 0.0)

                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing Swiss Ephemeris, PANCHANGAM WONT WORK: ${e.message}", e)
                }
            }
        }
    }

    // we need 2 kinds of details from ephemeris. one is that that doesnt change during the day like rahu kalam
    // yama kandam etc and second is things like yoga, karana, nakshatra etc which might change during any
    // part of the day. so we will make 2 functions to handle this.

    public fun calculateStaticPanchangamDetails(currDttm: LocalDateTime): StaticPanchangam {
        if (!::swissEph.isInitialized) {
            Log.e(TAG, "Swiss Ephemeris not initialized in calculatePanchangamInternal.")
            return StaticPanchangam(
                calcDttm = currDttm,
                rahuKalam = null,
                yamaGandam = null,
                sunrise = null,
                sunset = null,
                vaara = Vaara.GURU,
                gulikan = null,
                nallaNeram = null
            )
        }

        val geopos = doubleArrayOf(LONGITUDE_CHENNAI, LATITUDE_CHENNAI, ALTITUDE_CHENNAI)

        // --- STEP 1: Set Topocentric Coordinates ---
        // This is necessary to inform the library where the observer is.
        swissEph.swe_set_topo(geopos[0], geopos[1], geopos[2])

//        swissEph.swe_calc_ut(currentJulianDayUt(), SweConst.SE_MOON, SID_FLAGS, result, error)
//        if (error.isNotEmpty()) Log.e(TAG, "Moon calc error at sunrise: $error")

        val (sunriseJd, sunsetJd) = calculateSunriseSunset(
            swissEph,
            currDttm.year,
            currDttm.monthValue,
            currDttm.dayOfMonth,
            LATITUDE_CHENNAI,
            LONGITUDE_CHENNAI
        )

        val sunriseLocal = julianDayUtToLocalDttm(sunriseJd)
        val sunsetLocal = julianDayUtToLocalDttm(sunsetJd)

        val vaara = computeVaaraFromSunrise(
            now = currDttm,
            sunrise = sunriseLocal
        )

        // for calculating nalla neram, we need more accurate yama , rahu and gulikai kalams based on sunrise
        val (rahu, yama, gulikai) = calculateRahuYamaGulikai(sunriseLocal, sunsetLocal)
//        Log.d(TAG, "******calced r y g are : $rahu,  $yama,  $gulikai")

        // for chandrashtama , get which rasi is affected now - needed only for nallaneram i guess but let it stay here
//        val affectedJanmaRasi = getChandrashtamaRasi(moonLongitude)
//        val chandrashtamaApplicable = isChandrashtamaApplicable(janmaRasi.value, affectedJanmaRasi)

//        val nallaNeramWindows = calculateNallaNeram(
//            sunrise = sunriseLocal,
//            sunset = sunsetLocal,
//            rahu = rahu,
//            yama = yama,
//            gulikai = gulikai,
//            panchangam = currentPanchangam,
//            nakshatraTypeMap = nakshatraTypeMap
//        )

//        if (nallaNeramWindows.isEmpty()) {
//            Log.i(TAG, "No Nalla Neram windows today.")
//        } else {
//            Log.i(TAG, "Nalla Neram windows (${nallaNeramWindows.size}):")
//
//            nallaNeramWindows.forEach { window ->
//                Log.i(TAG, "✔-nallaneram-- ${window.reason}, ${window.start.toLocalTime()} - ${window.end.toLocalTime()} (score=${window.score})"
//                )
//            }
//        }

        var staticPanchangamForTheDay = StaticPanchangam(
            calcDttm = currDttm,
            sunrise = sunriseLocal,
            sunset = sunsetLocal,
            vaara = vaara,
            rahuKalam = rahu,
            yamaGandam = yama,
            gulikan = gulikai,
            nallaNeram = null // todo lets find whats the best window to even calculate this as it involves thithi,yoga,karana which change during the day
        )

        return staticPanchangamForTheDay
    }

    // --- Original calculation logic, now private and suspendable ---
    @RequiresApi(Build.VERSION_CODES.O)
    public fun calculateDynamicPanchangamDetails(currDttm: LocalDateTime,
                                                 userRasi: Rasi): DynamicPanchangam {
        // Ensure Swiss Ephemeris is initialized. (Should be called by initialize() first)
        if (!::swissEph.isInitialized) {
            Log.e(TAG, "Swiss Ephemeris not initialized in calculatePanchangamInternal.")
            return DynamicPanchangam(calcDttm = currDttm)
        }
        val geopos = doubleArrayOf(LONGITUDE_CHENNAI, LATITUDE_CHENNAI, ALTITUDE_CHENNAI)
        val error = StringBuffer()
        val result = DoubleArray(6)

        // --- STEP 1: Set Topocentric Coordinates ---
        // This is necessary to inform the library where the observer is.
        swissEph.swe_set_topo(geopos[0], geopos[1], geopos[2])

        swissEph.swe_calc_ut(currentJulianDayUt(), SweConst.SE_MOON, SID_FLAGS, result, error)
        if (error.isNotEmpty()) Log.e(TAG, "Moon calc error at sunrise: $error")
        val moonLongitude = if (error.isEmpty()) result[0] else 0.0

        swissEph.swe_calc_ut(currentJulianDayUt(), SweConst.SE_SUN, SID_FLAGS, result, error)
        if (error.isNotEmpty()) Log.e(TAG, "Sun calc error at sunrise: $error")
        val sunLongitude = if (error.isEmpty()) result[0] else 0.0

        // Thithi Calculation

        val lunarPhase = ((moonLongitude - sunLongitude) + 360.0) % 360.0
        val tithiIndex = floor(lunarPhase / 12.0).toInt()  // 0..29
        val paksha = if (tithiIndex < 15) Paksha.SHUKLA else Paksha.KRISHNA
        val baseIndex = tithiIndex % 15
        val baseTithi = when {
            paksha == Paksha.SHUKLA && baseIndex == 14 -> Thithi.PURNIMA
            paksha == Paksha.KRISHNA && baseIndex == 14 -> Thithi.AMAVASYA
            else -> Thithi.values()[baseIndex]
        }
        val tithiEndJd = findTithiEndTime(currentJulianDayUt(), tithiIndex, swissEph)

        // Yoga Calculation

        val sunLon = (sunLongitude + 360.0) % 360.0
        val moonLon = (moonLongitude + 360.0) % 360.0
        val yogaAngle = (sunLon + moonLon) % 360.0
        val yogaIndex = floor(yogaAngle / YOGA_DEGREES).toInt()
        val safeYogaIndex = yogaIndex.coerceIn(0, 26)
        val yogaEndTimeJulian = findYogaEndTime(currentJulianDayUt(), swissEph)
        val yoga = Yoga.entries[safeYogaIndex]

        // Karana Calculation (simplified for now, based on Thithi)

        val karanaIndex = floor(lunarPhase / 6.0).toInt()  // 0..59
        val karana = when (karanaIndex) {
            // Fixed Karanas
            0 -> Karana.KIMSTHUGNA        // First half of Shukla Prathama
            57 -> Karana.SHAKUNI          // Krishna Chaturdashi 2nd half
            58 -> Karana.CHATUSHPADA      // Amavasya 1st half
            59 -> Karana.NAGA             // Amavasya 2nd half

            // Chara (moving) Karanas — repeat cycle of 7
            else -> {
                val charaKaranas = listOf(
                    Karana.BAVA,
                    Karana.BALAVA,
                    Karana.KAULAVA,
                    Karana.TAITILA,
                    Karana.GARAJA,
                    Karana.VANIJA,
                    Karana.VISHTI
                )
                charaKaranas[(karanaIndex - 1) % 7]
            }
        }

        val karanaEndJd = findKaranaEndTime(currentJulianDayUt(), swissEph)

        // Nakshatra Calculation

        val NAK_DEG = 360.0 / 27.0  // 13.3333333333
        val nakshatraIndex = floor(moonLon / NAK_DEG).toInt()
        val safeNakIndex = nakshatraIndex.coerceIn(0, 26)
        val nakshatra = Nakshatra.entries[safeNakIndex]
        val nakOffset = moonLon % NAK_DEG
        val nakshatraPada = floor(nakOffset / (NAK_DEG / 4.0)).toInt() + 1
        val nakshatraEndTimeJulian = findNakshatraEndTime(currentJulianDayUt(), swissEph)

        // chandrashtama Calculation

        val affectedJanmaRasi = getChandrashtamaRasi(moonLongitude)

        // sunrise and sunset calculation

        val (sunriseJd, sunsetJd) = calculateSunriseSunset(
            swissEph,
            currDttm.year,
            currDttm.monthValue,
            currDttm.dayOfMonth,
            LATITUDE_CHENNAI,
            LONGITUDE_CHENNAI
        )

        val sunriseLocal = julianDayUtToLocalDttm(sunriseJd)
        val sunsetLocal = julianDayUtToLocalDttm(sunsetJd)

        // vaara calculation - perhaps the easiest of yall

        // the following is civil calendar vaara , simple as i said
        // val vaara: String = currDttm.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        // but for precise panchange calcs , vaara is from sunrise to sunset.
        val vaara = computeVaaraFromSunrise(
            now = currDttm,
            sunrise = sunriseLocal
        )

        // for calculating auspicious score, we need more accurate yama ,
        // rahu and gulikai kalams based on sunrise
        val (rahuKalam, yamaGandam, gulikai) = calculateRahuYamaGulikai(sunriseLocal, sunsetLocal)

        // calculate score based on panchangam parameters
        Log.i(TAG, "going to call calculateAuspiciousnessScore")
        val currentScore = calculateAuspiciousnessScore(
            baseTithi,
            nakshatra,
            yoga,
            karana,
            vaara,
            userRasi,
            affectedJanmaRasi,
            rahuKalam,
            yamaGandam,
            gulikai,
            currDttm  // passing it for rahu kalam , yama gandam calc and adjustment
        )

        val thithiEndTime = julianDayUtToLocalDttm(tithiEndJd)
        val nakshatraEndTime = julianDayUtToLocalDttm(nakshatraEndTimeJulian)
        val yogaEndTime = julianDayUtToLocalDttm(yogaEndTimeJulian)
        val karanaEndTime = julianDayUtToLocalDttm(karanaEndJd)
        // on 28 jan,2026 12 noon, priyam found a bug, i dont include rahu kalam and yama gandam
        // end times in this tho they fall within this timeframe
        val rahuKalamStartTime = rahuKalam.start
        val rahuKalamEndTime = rahuKalam.end
        val yamaGandamStartTime = yamaGandam.start
        val yamaGandamEndTime = yamaGandam.end

        // we set this back in object for the daily flow
        var nextRefreshTime = getNextPanchangamRefreshTime(thithiEndTime, nakshatraEndTime, yogaEndTime, karanaEndTime,
            rahuKalamStartTime, rahuKalamEndTime, yamaGandamStartTime, yamaGandamEndTime)

        var currentDynamicPanchangam = DynamicPanchangam(
            calcDttm = currDttm,
            sunrise = sunriseLocal,
            sunset = sunsetLocal,
            janmaRasi = userRasi,
            paksha = paksha,
            vaara = vaara,
            thithi = baseTithi,
            thithiEndTime = thithiEndTime,
            nakshatra = nakshatra,
            nakshatraPaadha = nakshatraPada,
            nakshatraEndTime = nakshatraEndTime,
            chandrashtamaRasi = affectedJanmaRasi,
            yoga = yoga,
            yogaEndTime = yogaEndTime,
            karana = karana,
            karanaEndTime = karanaEndTime,
            expiryDttm = nextRefreshTime,
            score = currentScore
        )

        return currentDynamicPanchangam
    }

    // chandrashtama Calculation
    private fun getChandrashtamaRasi(moonLongitude: Double): Rasi {

        val moonRasiIndex = (moonLongitude / RASI_DEGREES_PER_SEGMENT).toInt() % 12
        val affectedJanmaRasiIndex = (moonRasiIndex - 7 + 12) % 12
        val affectedJanmaRasi = Rasi.entries.getOrElse(affectedJanmaRasiIndex) { Rasi.MESHA }
        Log.d(TAG, "getChandrashtamaRasi AffectedJanmaRasi = $affectedJanmaRasi")

        return affectedJanmaRasi
    }

    // to pass to auspicious time calculator
    fun getDayOfWeek(dateTime: LocalDateTime): Int {
        // java.time DayOfWeek: MONDAY=1, TUESDAY=2, ..., SUNDAY=7
        // Calendar: SUNDAY=1, MONDAY=2, TUESDAY=3, ..., SATURDAY=7
        val javaTimeDay = dateTime.dayOfWeek.value
        return (javaTimeDay % 7) + 1 // Maps MONDAY(1)→2, TUESDAY(2)→3, ..., SUNDAY(7)→1
    }

    fun lunarPhaseDeg(jd: Double, swe: SwissEph): Double {
        val moon = DoubleArray(6)
        val sun = DoubleArray(6)

        swe.swe_calc(jd, SweConst.SE_MOON, SID_FLAGS, moon, null)
        swe.swe_calc(jd, SweConst.SE_SUN, SID_FLAGS, sun, null)

        return ((moon[0] - sun[0]) + 360.0) % 360.0
    }

    fun vaaraScore(vaara: Vaara): Int =
        vaaraQualityMap[vaara]?.score
            ?: VaaraQuality.GOOD.score


    private fun calculateAuspiciousnessScore(
        thithi: Thithi,
        nakshatra: Nakshatra,
        yoga: Yoga,
        karana: Karana,
        vaara: Vaara,
        userRasi: Rasi,
        affectedJanmaRasi: Rasi,
        rahuKalam: TimeRange,
        yamaGandam: TimeRange,
        gulikai: TimeRange, // todo use this as weall
        currentDttm: LocalDateTime
    ): Int {
        val dayOfWeek = getDayOfWeek(currentDttm)

        Log.i(TAG, "Inputs → Thithi: $thithi, Nakshatra: $nakshatra, Yoga: $yoga, Karana: $karana," +
                " Vaara: $vaara, chndrshtama rasi: $affectedJanmaRasi, JanmaRashi: $userRasi, " +
                "CurrentTime: $currentDttm, DayOfWeek: $dayOfWeek")

        var score = 0

        score += thithiScore(thithi)

        score += nakshatraScore(nakshatra)

        score += yogaScore(yoga)

        score += karanaScore(karana)

        score += vaaraScore(vaara)

        if (userRasi.ordinal == affectedJanmaRasi.ordinal) {
            score -= 10
            Log.d(TAG, "---Chandrashtama condition met as user rasi ${userRasi} = ${affectedJanmaRasi}. Penalty applied.")
        }

        // ---- RAHU KALAM AND YAMA GANDAM PENALTIES (−20 pts each) ----
        Log.i(TAG, "Rahu Kalam range new "+rahuKalam.start+" -- " + rahuKalam.end)
        if (currentDttm >= rahuKalam.start && currentDttm <= rahuKalam.end) {
            score -= 20
            Log.i(TAG, "Rahu Kalam period active (${rahuKalam.start} – ${rahuKalam.end}). Score penalty: -20")
        }

        if (!currentDttm.isBefore(yamaGandam.start) && !currentDttm.isAfter(yamaGandam.end)) {
            score -= 20
            Log.i(TAG, "Yama Gandam condition met. Penalty applied.")
        }

        Log.i(TAG, "Final Score: $score")

        return score.coerceIn(1, 100)
    }
}

    fun findTithiEndTime(
        jdStart: Double,
        tithiIndex: Int,
        swe: SwissEph
    ): Double {

        // Target Moon–Sun angle where this Thithi ends
        val targetPhase = (tithiIndex + 1) * 12.0

        val startPhase = lunarPhaseDeg(jdStart, swe)

        var jdLow = jdStart
        var jdHigh = jdStart + 1.5   // safe upper bound (~36 hours)

        // Ensure jdHigh crosses the target phase (handle 360° wrap properly)
        while (true) {
            val phaseHigh = lunarPhaseDeg(jdHigh, swe)
            val delta = (phaseHigh - startPhase + 360.0) % 360.0

            if (startPhase + delta >= targetPhase) break
            jdHigh += 0.25   // step by 6 hours
        }

        // Binary search refinement (~1 second precision)
        repeat(40) {
            val jdMid = (jdLow + jdHigh) / 2.0
            val phaseMid = lunarPhaseDeg(jdMid, swe)
            val deltaMid = (phaseMid - startPhase + 360.0) % 360.0

            if (startPhase + deltaMid < targetPhase) {
                jdLow = jdMid
            } else {
                jdHigh = jdMid
            }
        }

        return (jdLow + jdHigh) / 2.0
    }

    fun findNakshatraEndTime(
        jdStart: Double,
        swe: SwissEph
    ): Double {

        val NAK_DEG = 360.0 / 27.0

        val startLon = moonLongitudeDeg(jdStart, swe)   // 0..360
        val startIndex = floor(startLon / NAK_DEG).toInt()
        val targetLon = (startIndex + 1) * NAK_DEG

        var jdLow = jdStart
        var jdHigh = jdStart + 1.2   // Nakshatra max ~24 hrs (safe)

        // Ensure upper bound crosses target longitude (handle 360 wrap)
        while (true) {
            val lonHigh = moonLongitudeDeg(jdHigh, swe)
            val delta = (lonHigh - startLon + 360.0) % 360.0

            if (startLon + delta >= targetLon) break
            jdHigh += 0.25   // step 6 hours
        }

        // Binary refinement (~1 second accuracy)
        repeat(40) {
            val jdMid = (jdLow + jdHigh) / 2.0
            val lonMid = moonLongitudeDeg(jdMid, swe)
            val deltaMid = (lonMid - startLon + 360.0) % 360.0

            if (startLon + deltaMid < targetLon) {
                jdLow = jdMid
            } else {
                jdHigh = jdMid
            }
        }

        return (jdLow + jdHigh) / 2.0
    }

    fun findYogaEndTime(
        jdStart: Double,
        swe: SwissEph
    ): Double {

        val YOGA_DEG = 360.0 / 27.0

        val sunLonStart = sunLongitudeDeg(jdStart, swe)
        val moonLonStart = moonLongitudeDeg(jdStart, swe)
        val startSum = (sunLonStart + moonLonStart) % 360.0

        val startIndex = floor(startSum / YOGA_DEG).toInt()
        val targetAngle = (startIndex + 1) * YOGA_DEG

        var jdLow = jdStart
        var jdHigh = jdStart + 1.2   // Yoga < 24h (safe)

        // Ensure boundary crossing (wrap-safe)
        while (true) {
            val sumHigh =
                (sunLongitudeDeg(jdHigh, swe) + moonLongitudeDeg(jdHigh, swe)) % 360.0

            val delta = (sumHigh - startSum + 360.0) % 360.0
            if (startSum + delta >= targetAngle) break

            jdHigh += 0.25   // 6-hour step
        }

        // Binary refinement
        repeat(40) {
            val jdMid = (jdLow + jdHigh) / 2.0
            val sumMid =
                (sunLongitudeDeg(jdMid, swe) + moonLongitudeDeg(jdMid, swe)) % 360.0

            val deltaMid = (sumMid - startSum + 360.0) % 360.0

            if (startSum + deltaMid < targetAngle) {
                jdLow = jdMid
            } else {
                jdHigh = jdMid
            }
        }

        return (jdLow + jdHigh) / 2.0
    }

    fun findKaranaEndTime(
        jdStart: Double,
        swe: SwissEph
    ): Double {

        val startPhase = lunarPhaseDeg(jdStart, swe)   // 0..360
        val karanaIndex = floor(startPhase / 6.0).toInt()
        val targetPhase = (karanaIndex + 1) * 6.0

        var jdLow = jdStart
        var jdHigh = jdStart + 0.8   // Karana < ~12 hours (safe)

        // Ensure boundary crossing (wrap-safe)
        while (true) {
            val phaseHigh = lunarPhaseDeg(jdHigh, swe)
            val delta = (phaseHigh - startPhase + 360.0) % 360.0

            if (startPhase + delta >= targetPhase) break
            jdHigh += 0.125   // 3-hour step
        }

        // Binary refinement (~1 second)
        repeat(40) {
            val jdMid = (jdLow + jdHigh) / 2.0
            val phaseMid = lunarPhaseDeg(jdMid, swe)
            val deltaMid = (phaseMid - startPhase + 360.0) % 360.0

            if (startPhase + deltaMid < targetPhase) {
                jdLow = jdMid
            } else {
                jdHigh = jdMid
            }
        }

        return (jdLow + jdHigh) / 2.0
    }

    fun moonLongitudeDeg(jdUt: Double, swe: SwissEph): Double {
        val xx = DoubleArray(6)
        swe.swe_calc_ut(
            jdUt,
            SweConst.SE_MOON,
            SID_FLAGS,
            xx,
            null
        )
        return (xx[0] + 360.0) % 360.0
    }

    fun sunLongitudeDeg(jdUt: Double, swe: SwissEph): Double {
        val xx = DoubleArray(6)
        swe.swe_calc_ut(
            jdUt,
            SweConst.SE_SUN,
            SID_FLAGS,
            xx,
            null
        )
        return (xx[0] + 360.0) % 360.0
    }

    fun calculateSunriseSunset(
        swe: SwissEph,
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double
    ): Pair<Double, Double> {

        // Julian Day at 0h UT
        val sd = SweDate(year, month, day, 0.0, SweDate.SE_GREG_CAL)
        val jd = sd.julDay

        // Geo position: [longitude, latitude, altitude]
        val geopos = doubleArrayOf(
            longitude,
            latitude,
            0.0
        )

        val flags = SweConst.SEFLG_SWIEPH
        val serr = StringBuffer()

        val sunrise = DblObj()
        val sunset = DblObj()

        // Sunrise
        val retRise = swe.swe_rise_trans(
            jd,
            SweConst.SE_SUN,
            null,
            flags,
            SweConst.SE_CALC_RISE,
            geopos,
            0.0,
            0.0,
            sunrise,
            serr
        )

        // Sunset
        val retSet = swe.swe_rise_trans(
            jd,
            SweConst.SE_SUN,
            null,
            flags,
            SweConst.SE_CALC_SET,
            geopos,
            0.0,
            0.0,
            sunset,
            serr
        )

        if (retRise < 0 || retSet < 0) {
            throw RuntimeException("Rise/Set error: $serr")
        }

        val sunriseJD: Double = sunrise.`val`
        val sunsetJD: Double = sunset.`val`

        return Pair(sunriseJD, sunsetJD)
    }


    fun calculateRahuYamaGulikai(
        sunrise: LocalDateTime,
        sunset: LocalDateTime
    ): Triple<TimeRange, TimeRange, TimeRange> {

        val dayOfWeek = sunrise.dayOfWeek

        val dayDurationMinutes = Duration.between(sunrise, sunset).toMinutes()

        val segmentMinutes = dayDurationMinutes / 8

        fun segmentToTimeRange(segmentIndex: Int): TimeRange {
            val start = sunrise.plusMinutes(
                (segmentIndex - 1) * segmentMinutes
            )
            val end = start.plusMinutes(segmentMinutes)
            return TimeRange(start, end)
        }

        val rahu = segmentToTimeRange(getRahukalamSegment(dayOfWeek))
        val yama = segmentToTimeRange(getYamakandamSegment(dayOfWeek))
        val gulikai = segmentToTimeRange(getGulikaiSegment(dayOfWeek))

        return Triple(rahu, yama, gulikai)
    }

    data class NallaNeramWindow(
        val start: LocalDateTime,
        val end: LocalDateTime,
        val score: Int,
        val reason: String
    )

    fun mergeWindows(windows: List<NallaNeramWindow>): List<NallaNeramWindow> {

        if (windows.isEmpty()) return emptyList()

        val merged = mutableListOf<NallaNeramWindow>()
        var current = windows.first()

        for (next in windows.drop(1)) {
            if (current.end == next.start) {
                current = current.copy(
                    end = next.end,
                    score = maxOf(current.score, next.score)
                )
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)

        return merged
    }

    fun calculateNallaNeram(
        sunrise: LocalDateTime,
        sunset: LocalDateTime,
        rahu: TimeRange,
        yama: TimeRange,
        gulikai: TimeRange,
        chandrashtamamApplicable: Boolean,
        vaara: Vaara
    ): List<NallaNeramWindow> {

        val sliceMinutes = 30L
        val slices = mutableListOf<NallaNeramWindow>()

        /* ----------------------------
       1️⃣ Compute Abhijit Muhurta
       ---------------------------- */

        val abhijit: TimeRange? =
            if (chandrashtamamApplicable && vaara != Vaara.BUDHA) {
                val dayDuration = Duration.between(sunrise, sunset)
                val muhurta = dayDuration.dividedBy(15)
                val start = sunrise.plus(muhurta.multipliedBy(7))
                val end = start.plus(muhurta)
                TimeRange(start, end)
            } else {
                null
            }

        /* ----------------------------
       2️⃣ Slice-based Nalla Neram
       ---------------------------- */

        var current = sunrise
        while (current.plusMinutes(sliceMinutes) <= sunset) {

            val sliceStart = current
            val sliceEnd = current.plusMinutes(sliceMinutes)

            // ❌ Chandrashtama blocks everything
            if (chandrashtamamApplicable) {
                current = sliceEnd
                continue
            }

            // 🔑 Abhijit override (ignores doshas)
            if (
                abhijit != null &&
                sliceStart < abhijit.end &&
                sliceEnd > abhijit.start
            ) {
                slices.add(
                    NallaNeramWindow(
                        sliceStart,
                        sliceEnd,
                        score = 5,                // High priority
                        reason = "Abhijit Muhurta"
                    )
                )
                current = sliceEnd
                continue
            }

            // ❌ Dosha overlap check
            fun overlaps(a: TimeRange) =
                sliceStart < a.end && sliceEnd > a.start

            if (
                overlaps(rahu) ||
                overlaps(yama) ||
                overlaps(gulikai)
            ) {
                current = sliceEnd
                continue
            }

//        val score =
//            tithiScore(panchangam.tithi) + yogaScore(panchangam.yoga) +
//                    nakshatraScore((nakshatraTypeMap[panchangam.nakshatra] ?: "").toString())
            val score = 3

            if (score >= 2) {
                slices.add(
                    NallaNeramWindow(
                        sliceStart,
                        sliceEnd,
                        score = score,
                        reason = "DynamicPanchangam"
                    )
                )
            }
            current = sliceEnd
        }
        return mergeWindows(slices)
    }


private fun getNextPanchangamRefreshTime(
    thithiEndTime: LocalDateTime, nakshatraEndTime: LocalDateTime, yogaEndTime: LocalDateTime, karanaEndTime: LocalDateTime,
    rahuKalamStartTime: LocalDateTime, rahuKalamEndTime: LocalDateTime, yamaGandamStartTime: LocalDateTime, yamaGandamEndTime: LocalDateTime
): LocalDateTime {

    val now = LocalDateTime.now()

    val nextChange = listOfNotNull(
        thithiEndTime,
        nakshatraEndTime,
        yogaEndTime,
        karanaEndTime,
        rahuKalamStartTime,
        rahuKalamEndTime,
        yamaGandamStartTime,
        yamaGandamEndTime,
//        LocalDateTime.now().plusMinutes(2)  // todo remove it after test
    )
        .filter { it.isAfter(now) } // todo, how does timezones affect this ??
        .minOrNull()

    // If nothing is upcoming, force refresh before next sunrise
    val nextChangeDttm = nextChange ?: now.plusHours(23)
    Log.i("PanchangamCalculator", "----next panchangam refresh time = $nextChangeDttm")

    return nextChangeDttm
}
