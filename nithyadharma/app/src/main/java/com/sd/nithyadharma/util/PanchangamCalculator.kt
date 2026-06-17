package com.sd.nithyadharma.util

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.sd.nithyadharma.model.PanchangaAttributes
import com.sd.nithyadharma.model.PanchangaAttributes.DynamicPanchangam
import com.sd.nithyadharma.model.PanchangaAttributes.Karana
import com.sd.nithyadharma.model.PanchangaAttributes.computeVaaraFromSunrise
import com.sd.nithyadharma.model.PanchangaAttributes.getGulikaiSegment
import com.sd.nithyadharma.model.PanchangaAttributes.getRahukalamSegment
import com.sd.nithyadharma.model.PanchangaAttributes.getYamakandamSegment
import com.sd.nithyadharma.model.PanchangaAttributes.Nakshatra
import com.sd.nithyadharma.model.PanchangaAttributes.Paksha
import com.sd.nithyadharma.model.PanchangaAttributes.Rasi
import com.sd.nithyadharma.model.PanchangaAttributes.StaticGulikaiKalamMap
import com.sd.nithyadharma.model.PanchangaAttributes.StaticPanchangam
import com.sd.nithyadharma.model.PanchangaAttributes.StaticRahuKalamMap
import com.sd.nithyadharma.model.PanchangaAttributes.StaticYamaGandamMap
import com.sd.nithyadharma.model.PanchangaAttributes.TamilMonth
import com.sd.nithyadharma.model.PanchangaAttributes.Thithi
import com.sd.nithyadharma.model.PanchangaAttributes.Vaara
import com.sd.nithyadharma.model.PanchangaAttributes.Yoga
import com.sd.nithyadharma.model.TimeRange
import com.sd.nithyadharma.model.TimeWindow
import com.sd.nithyadharma.util.Constants.RASI_DEGREES_PER_SEGMENT
import com.sd.nithyadharma.util.Constants.YOGA_DEGREES
import com.sd.nithyadharma.util.PanchangamCalculator.lunarPhaseDeg
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import swisseph.DblObj
import java.io.File
import java.time.DayOfWeek
import java.time.Instant
import kotlin.math.floor
import kotlin.math.roundToLong
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.ceil

//    1. Nakshatra ⭐⭐⭐⭐⭐ 33.33 max marks
//    2. Tithi ⭐⭐⭐⭐  26.67 max
//    3. Yoga ⭐⭐⭐  20 max
//    4. Karana ⭐⭐  13.33 max
//    5. Vaara ⭐  6.67 max

enum class NakshatraType(val score: Double) {
    MRIDU(16.67),      // mildly good
    KSHIPRA(22.22),    // good
    DHRUVA(33.33),     // very good fixed (max)
    CHARA(33.33),      // very good moving
    UGRA(-22.22),      // very bad
    TIKSHNA(-11.11),   // bad
    MISHRA(0.0)        // neutral
}

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
    Nakshatra.MULA to NakshatraType.TIKSHNA,

    // 🟣 Mishra (Mixed)
    Nakshatra.KRITHTHIKA to NakshatraType.MISHRA,
    Nakshatra.VISHAKHA to NakshatraType.MISHRA,
    Nakshatra.SHRAVANA to NakshatraType.MISHRA,
    Nakshatra.DHANISHTA to NakshatraType.MISHRA,
    Nakshatra.SHATABHISHA to NakshatraType.MISHRA,

    // 🟣 Chara (Moving)
    Nakshatra.PUNARVASU to NakshatraType.CHARA,
    Nakshatra.SWAATHI to NakshatraType.CHARA
)

fun nakshatraScore(nakshatra: Nakshatra): Double =
    nakshatraTypeMap[nakshatra]?.score ?: 0.0

fun thithiScore(thithi: Thithi): Double =
    when (thithi) {
        Thithi.DWITIYA,
        Thithi.TRITIYA,
        Thithi.PANCHAMI,
        Thithi.SAPTAMI,
        Thithi.DASHAMI,
        Thithi.EKADASHI -> 26.67

        Thithi.PRATHAMA,
        Thithi.SHASHTI,
        Thithi.NAVAMI,
        Thithi.DWADASHI,
        Thithi.TRAYODASHI,
        Thithi.PURNIMA -> 17.78

        Thithi.CHATURTHI,
        Thithi.CHATURDASHI -> 8.89

        Thithi.ASHTAMI,
        Thithi.AMAVASYA -> 4.44
    }

enum class YogaQuality(val score: Double) {
    GOOD(20.0),
    NEUTRAL(13.33),
    BAD(6.67)
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

fun yogaScore(yoga: Yoga): Double =
    yogaQualityMap[yoga]?.score
        ?: YogaQuality.NEUTRAL.score

enum class KaranaQuality(val score: Double) {
    GOOD(13.33),
    NEUTRAL(9.33),
    BAD(6.67)
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

fun karanaScore(karana: Karana): Double =
    karanaQualityMap[karana]?.score
        ?: KaranaQuality.NEUTRAL.score

enum class VaaraQuality(val score: Double) {
    EXCELLENT(6.67),
    GOOD(4.67),
    WEAK(3.33)
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

fun vaaraScore(vaara: Vaara): Double =
    vaaraQualityMap[vaara]?.score
        ?: VaaraQuality.GOOD.score

// added by on 12 june 2026 for future day score calc omitting rg,yama, guli , yoga , karana
// and also bringing these new functions
private fun futureNakshatraScore(
    nakshatra: Nakshatra
): Int {

    return when (nakshatraTypeMap[nakshatra]) {

        NakshatraType.DHRUVA,
        NakshatraType.CHARA -> 50

        NakshatraType.KSHIPRA -> 40

        NakshatraType.MRIDU -> 35

        NakshatraType.MISHRA -> 25

        NakshatraType.TIKSHNA -> 10

        NakshatraType.UGRA -> 0

        null -> 25
    }
}

private fun futureThithiScore(
    thithi: Thithi
): Int {

    return when (thithi) {

        Thithi.DWITIYA,
        Thithi.TRITIYA,
        Thithi.PANCHAMI,
        Thithi.SAPTAMI,
        Thithi.DASHAMI,
        Thithi.EKADASHI -> 35

        Thithi.PRATHAMA,
        Thithi.SHASHTI,
        Thithi.NAVAMI,
        Thithi.DWADASHI,
        Thithi.TRAYODASHI,
        Thithi.PURNIMA -> 25

        Thithi.CHATURTHI,
        Thithi.CHATURDASHI -> 15

        Thithi.ASHTAMI,
        Thithi.AMAVASYA -> 5
    }
}

private fun futureVaaraScore(
    vaara: Vaara
): Int {

    return when (vaaraQualityMap[vaara]) {

        VaaraQuality.EXCELLENT -> 15

        VaaraQuality.GOOD -> 10

        VaaraQuality.WEAK -> 5

        null -> 10
    }
}

private fun calculateFutureDayScore(
    thithi: Thithi,
    nakshatra: Nakshatra,
    vaara: Vaara,
    userRasi: Rasi,
    chandrashtamaRasi: Rasi
): Int {

    var score = 0

    score += futureNakshatraScore(nakshatra)
    score += futureThithiScore(thithi)
    score += futureVaaraScore(vaara)

    if (userRasi == chandrashtamaRasi) {
        score -= 15
    }

    return score.coerceIn(1, 100)
}

private const val SID_FLAGS = SweConst.SEFLG_SWIEPH or SweConst.SEFLG_SIDEREAL

private fun julianDayUtToLocalDttm(jdUt: Double): LocalDateTime {
    val millis = ((jdUt - 2440587.5) * 86400000.0).roundToLong()

    return Instant.ofEpochMilli(millis)
        .atZone(Constants.INDIA_ZONE)
        .toLocalDateTime()
}

fun julianDayUt(dttm: LocalDateTime): Double {

    val currentUtc = dttm
        .atZone(Constants.INDIA_ZONE)
        .withZoneSameInstant(Constants.UTC_ZONE)

    val hourDecimal =
        currentUtc.hour +
                currentUtc.minute / 60.0 +
                currentUtc.second / 3600.0 +
                currentUtc.nano / 3_600_000_000_000.0

    return SweDate(
        currentUtc.year,
        currentUtc.monthValue,
        currentUtc.dayOfMonth,
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

    // todo, for future scaleup, geopos must come from a enum where i get user location

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
        // was
//        val (rahu, yama, gulikai) = calculateDynamicRahuYamaGulikai(sunriseLocal, sunsetLocal)
        // now
        val (rahu, yama, gulikai) = getRahuKaalamYamaGandamGulikaiPeriods(sunriseLocal, sunsetLocal, useDynamic = false)

//        Log.d(TAG, "******calced r y g are : $rahu,  $yama,  $gulikai")

        // this sends actual sunrise and sunset but users are used to static so we
        // will comment this and use next fn
//        val nallaNeramWindows = getHighPrecisionNallaNeramWindows(currDttm ,
//            sunriseLocal.toLocalTime(),
//            sunsetLocal.toLocalTime()
//        )
        // todo , on jun 18,2026, i think this is not mature or correct enough to be included
        // but this must be conquered

//        val nallaNeramWindows = getHighPrecisionNallaNeramWindows(
//            currDttm = currDttm,
//            sunrise = LocalTime.of(6, 0),  // Hardcoded 06:00 AM
//            sunset = LocalTime.of(18, 0)   // Hardcoded 06:00 PM (18:00)
//        )

        var staticPanchangamForTheDay = StaticPanchangam(
            calcDttm = currDttm,
            sunrise = sunriseLocal,
            sunset = sunsetLocal,
            vaara = vaara,
            rahuKalam = rahu,
            yamaGandam = yama,
            gulikan = gulikai,
            nallaNeram = null //nallaNeramWindows
        )

        return staticPanchangamForTheDay
    }

    // --- Original calculation logic, now private and suspendable ---
    // todo , we need a light and full version of the function as most karana , yoga , end times etc
    // are not required for light version which is used for next N days future panchanga.
    // todo, for future scaleup, geopos must come from a enum where i get user location

    @RequiresApi(Build.VERSION_CODES.O)
    public fun calculateDynamicPanchangamDetails(currDttm: LocalDateTime,
                                                 userRasi: Rasi,
                                                 currentMode: Boolean): DynamicPanchangam {
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

        swissEph.swe_calc_ut(julianDayUt(currDttm), SweConst.SE_MOON, SID_FLAGS, result, error)
        if (error.isNotEmpty()) Log.e(TAG, "Moon calc error at sunrise: $error")
        val moonLongitude = if (error.isEmpty()) result[0] else 0.0

        swissEph.swe_calc_ut(julianDayUt(currDttm), SweConst.SE_SUN, SID_FLAGS, result, error)
        if (error.isNotEmpty()) Log.e(TAG, "Sun calc error at sunrise: $error")
        val sunLongitude = if (error.isEmpty()) result[0] else 0.0

        // Thithi Calculation

        val lunarPhase = ((moonLongitude - sunLongitude) + 360.0) % 360.0
        val tithiIndex = floor(lunarPhase / 12.0).toInt()  // 0..29
        val paksha = if (tithiIndex < 15) Paksha.SHUKLA else Paksha.KRISHNA
        val baseIndex = tithiIndex % 15
        val thithi = when {
            paksha == Paksha.SHUKLA && baseIndex == 14 -> Thithi.PURNIMA
            paksha == Paksha.KRISHNA && baseIndex == 14 -> Thithi.AMAVASYA
            else -> Thithi.values()[baseIndex]
        }
        val tithiEndJd = findTithiEndTime(julianDayUt(currDttm), tithiIndex, swissEph)

        // Yoga Calculation

        val sunLon = (sunLongitude + 360.0) % 360.0
        val moonLon = (moonLongitude + 360.0) % 360.0
        val yogaAngle = (sunLon + moonLon) % 360.0
        val yogaIndex = floor(yogaAngle / YOGA_DEGREES).toInt()
        val safeYogaIndex = yogaIndex.coerceIn(0, 26)
        val yogaEndTimeJulian = findYogaEndTime(julianDayUt(currDttm), swissEph)
        val yoga = Yoga.entries[safeYogaIndex]

        // tamil month calculation
        val tamizhMonth = getTamilMonth(sunLon)

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

        val karanaEndJd = findKaranaEndTime(julianDayUt(currDttm), swissEph)

        // Nakshatra Calculation

        val NAK_DEG = 360.0 / 27.0  // 13.3333333333
        val nakshatraIndex = floor(moonLon / NAK_DEG).toInt()
        val safeNakIndex = nakshatraIndex.coerceIn(0, 26)
        val nakshatra = Nakshatra.entries[safeNakIndex]
        val nakOffset = moonLon % NAK_DEG
        val nakshatraPada = floor(nakOffset / (NAK_DEG / 4.0)).toInt() + 1
        val nakshatraEndTimeJulian = findNakshatraEndTime(julianDayUt(currDttm), swissEph)

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

        val vaaraCalcTimeForMuhurtha =
            if (currentMode) { // true means for now when the user is seeing
                Log.i(TAG, "current day so callin vaara with $currDttm ")
                currDttm
            } else { // flase means future day
                Log.i(TAG, "future day so callin vaara with $currDttm.plusHours(8) ")
                currDttm.plusHours(8)
            }

        val vaara = computeVaaraFromSunrise(
            now = vaaraCalcTimeForMuhurtha,
            sunrise = sunriseLocal
        )

        // compute nalla neram windows
        // todo , on jun 18,2026, i think this is not mature or correct enough to be included

//        val nallaNeramWindows = getHighPrecisionNallaNeramWindows(currDttm ,
//            sunriseLocal.toLocalTime(),
//            sunsetLocal.toLocalTime()
//        )
        val nallaNeramWindows = null

        // for calculating auspicious score, we need more accurate yama ,
        // rahu and gulikai kalams based on sunrise
        // was
//        val (rahuKalam, yamaGandam, gulikai) = calculateDynamicRahuYamaGulikai(sunriseLocal, sunsetLocal)

        // now
        val (rahuKalam, yamaGandam, gulikai) = getRahuKaalamYamaGandamGulikaiPeriods(sunriseLocal, sunsetLocal, useDynamic = false)

        // calculate score based on panchangam parameters
        Log.i(TAG, "going to call calculateAuspiciousnessScore")
        // if currentMode is true , means do full calc as i have everything and come with accurate number
        //  if false , it is for futureday score calc so use only less args and new function
        val currentScore =
            if (currentMode) {

                calculateAuspiciousnessScore(
                    thithi = thithi,
                    nakshatra = nakshatra,
                    yoga = yoga,
                    karana = karana,
                    vaara = vaara,
                    userRasi = userRasi,
                    affectedJanmaRasi = affectedJanmaRasi,
                    rahuKalam = rahuKalam,
                    yamaGandam = yamaGandam,
                    nallaNeram = null, //nallaNeramWindows,
                    gulikai = gulikai,
                    currentDttm = currDttm, // passing it for rahu kalam , yama gandam calc and adjustment
                    sunrise = sunriseLocal, // todo this aint great - for nalla neram calc
                    sunset = sunsetLocal   // do
                )

            } else {

                calculateFutureDayScore(
                    thithi = thithi,
                    nakshatra = nakshatra,
                    vaara = vaara,
                    userRasi = userRasi,
                    chandrashtamaRasi = affectedJanmaRasi
                )
            }

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
            rahuKalamStartTime, rahuKalamEndTime, yamaGandamStartTime, yamaGandamEndTime, nallaNeramWindows)

        // find if it is a muhurtha day
        // for this the thithi and nakshatra should be the ones that are at the time of sunrise
        // this function calculates scores wrt to currentDttm everything
        // but we need to know what nakshatra or thithi was there at the time of sunrise.
        // to pass to this function. we have nakshatraEndTime, thithiEndTime, sunriseLocal

        val sunriseThithi: Thithi
        val sunriseNakshatra: Nakshatra

        with(PanchangaAttributes) {

            sunriseThithi =
                if (thithiEndTime.isBefore(sunriseLocal))
                    thithi.next()
                else
                    thithi

            sunriseNakshatra =
                if (nakshatraEndTime.isBefore(sunriseLocal))
                    nakshatra.next()
                else
                    nakshatra
        }
        Log.d(TAG, "====calling isMuhurthaDay wth = $sunriseThithi , $sunriseNakshatra , $tamizhMonth, $vaara")

        val isMuhurthaDay = isMuhurthaDay(
            tamizhMonth,
            vaara,
            sunriseThithi,
            sunriseNakshatra
        )

        var currentDynamicPanchangam = DynamicPanchangam(
            calcDttm = currDttm,
            sunrise = sunriseLocal,
            sunset = sunsetLocal,
            janmaRasi = userRasi,
            paksha = paksha,
            maasam = tamizhMonth,
            vaara = vaara,
            thithi = thithi,
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
            muhurthaDay = isMuhurthaDay,
            score = currentScore
        )

        return currentDynamicPanchangam
    }

    fun getTamilMonth(sunLongitude: Double): TamilMonth {

        return when ((sunLongitude / 30.0).toInt()) {
            0 -> TamilMonth.CHITHIRAI
            1 -> TamilMonth.VAIKASI
            2 -> TamilMonth.AANI
            3 -> TamilMonth.AADI
            4 -> TamilMonth.AVANI
            5 -> TamilMonth.PURATTASI
            6 -> TamilMonth.IYPPASI
            7 -> TamilMonth.KARTHIGAI
            8 -> TamilMonth.MARGAZHI
            9 -> TamilMonth.THAI
            10 -> TamilMonth.MAASI
            11 -> TamilMonth.PANGUNI
            else -> error("Invalid Sun longitude: $sunLongitude")
        }
    }

    // chandrashtama Calculation
    private fun getChandrashtamaRasi(moonLongitude: Double): Rasi {

        val moonRasiIndex = (moonLongitude / RASI_DEGREES_PER_SEGMENT).toInt() % 12
        val affectedJanmaRasiIndex = (moonRasiIndex - 7 + 12) % 12
        val affectedJanmaRasi = Rasi.entries.getOrElse(affectedJanmaRasiIndex) { Rasi.MESHA }
        Log.d(TAG, "getChandrashtamaRasi AffectedJanmaRasi = $affectedJanmaRasi")

        return affectedJanmaRasi
    }

    fun lunarPhaseDeg(jd: Double, swe: SwissEph): Double {
        val moon = DoubleArray(6)
        val sun = DoubleArray(6)

        swe.swe_calc(jd, SweConst.SE_MOON, SID_FLAGS, moon, null)
        swe.swe_calc(jd, SweConst.SE_SUN, SID_FLAGS, sun, null)

        return ((moon[0] - sun[0]) + 360.0) % 360.0
    }

    // this function uses the pancha anga + rahu kala , yama ganda , chandrashtama etc to arrive at a number,
    // weightage should be given as follows
    //    1. Nakshatra ⭐⭐⭐⭐⭐
    //    2. Tithi ⭐⭐⭐⭐
    //    3. Yoga ⭐⭐⭐
    //    4. Karana ⭐⭐
    //    5. Vaara ⭐
    //    Nakshatra → highest
    //    Tithi → high
    //    Yoga → medium
    //    Karana → low
    //    Vaara → lowest

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
        nallaNeram: List<TimeWindow>?,
        gulikai: TimeRange, // todo use this as weall
        currentDttm: LocalDateTime,
        sunrise: LocalDateTime,
        sunset: LocalDateTime
    ): Int {
        val dayOfWeek = currentDttm.dayOfWeek

        Log.i(TAG, "-----Inputs → Thithi: $thithi (${thithiScore(thithi)}), " +
                "Nakshatra: $nakshatra (${nakshatraScore(nakshatra)}), Yoga: $yoga, " +
                "Karana: $karana," + " Vaara: $vaara (${vaaraScore(vaara)}), " +
                "chandrshtama rasi: $affectedJanmaRasi, JanmaRashi: $userRasi, " +
                "CurrentTime: $currentDttm, DayOfWeek: $dayOfWeek")

        var score = 0.0

        score += thithiScore(thithi)

        score += nakshatraScore(nakshatra)

        score += yogaScore(yoga)

        score += karanaScore(karana)

        score += vaaraScore(vaara)

//        val currentTime = currentDttm.toLocalTime()
//        if (currentTime.let { current ->
//                nallaNeram.any { current in it.start..it.end }
//            }) {
//            // Your code when it's Nalla Neram
//            Log.d(TAG, "---nallaneram condition met, adding 15 marks")
//            score += 15.0
//        }

        if (userRasi.ordinal == affectedJanmaRasi.ordinal) {
            score -= 15.0
            Log.d(TAG, "---Chandrashtama condition met as user rasi ${userRasi} = ${affectedJanmaRasi}. Penalty applied.")
        }

        // ---- RAHU KALAM AND YAMA GANDAM PENALTIES (−20 pts each) ----
        Log.i(TAG, "Rahu Kalam range new "+rahuKalam.start+" -- " + rahuKalam.end)
        if (currentDttm >= rahuKalam.start && currentDttm <= rahuKalam.end) {
            score -= 20.0
            Log.i(TAG, "Rahu Kalam period active (${rahuKalam.start} – ${rahuKalam.end}). Score penalty: -20")
        }

        if (!currentDttm.isBefore(yamaGandam.start) && !currentDttm.isAfter(yamaGandam.end)) {
            score -= 20.0
            Log.i(TAG, "Yama Gandam condition met. Penalty applied.")
        }

        if (!currentDttm.isBefore(gulikai.start) && !currentDttm.isAfter(gulikai.end)) {
            score -= 5.0
            Log.i(TAG, "gulikai condition met. Penalty applied.")
        }
        Log.i(TAG, "Final Score: $score")

        val finalScore = ceil(score).toInt().coerceIn(1, 100)

        return finalScore
    }


    // adding a new functions wrapper for next n days worth of panchangam
    // added date , 11 jun, 2026

    fun calculateFuturePanchangam(
        days: Int,
        userRasi: Rasi
    ): List<Pair<StaticPanchangam, DynamicPanchangam>> {

        val result =
            mutableListOf<Pair<StaticPanchangam, DynamicPanchangam>>()

        // for this since the calculation is always from today, lets use this
        //val today = LocalDate.now()
        val startDate = LocalDate.now().plusDays(1)

        repeat(days) { offset ->

            //val date = today.plusDays(offset.toLong())
            val date = startDate.plusDays(offset.toLong())

            // day starts at midnight 1 mins so we can calc both naks
            // if they fall on the same day which happens all time.
            // same is true for thithi
            val dayStart = date.atTime(0, 1)

            val staticPanchangam =
                calculateStaticPanchangamDetails(dayStart)

            val sunrise = dayStart

            val dynamicPanchangam =
                calculateDynamicPanchangamDetails(sunrise, userRasi, false) // false means use future score calc and such

            result.add(
                staticPanchangam to dynamicPanchangam
            )
        }

        return result
    }

    fun isMuhurthaDay(
        tamilMonth: TamilMonth,
        vaara: Vaara,
        thithi: Thithi,
        nakshatra: Nakshatra
    ): Boolean {

        val allowedMonths = setOf(
            TamilMonth.CHITHIRAI,
            TamilMonth.VAIKASI,
            TamilMonth.AANI,
            TamilMonth.AVANI,
            TamilMonth.IYPPASI,
            TamilMonth.KARTHIGAI,
            TamilMonth.THAI,
            TamilMonth.MAASI,
            TamilMonth.PANGUNI
        )

        val allowedVaaras = setOf(
            Vaara.SOMA,
            Vaara.BUDHA,
            Vaara.GURU,
            Vaara.SHUKRA
        )

        // Rikta Tithis (Chaturthi, Navami, Chaturdashi) are excluded by default from this list
        val allowedThithis = setOf(
            Thithi.DWITIYA,
            Thithi.TRITIYA,
            Thithi.PANCHAMI,
            Thithi.SAPTAMI,
            Thithi.DASHAMI,
            Thithi.EKADASHI,
            Thithi.DWADASHI,
            Thithi.TRAYODASHI
        )

        val allowedNakshatras = setOf(
            Nakshatra.ROHINI,
            Nakshatra.MRIGASHIRSHA,
            Nakshatra.PUNARVASU,
            Nakshatra.PUSHYA,
            Nakshatra.UTTARA_PHALGUNI,
            Nakshatra.HASTHA,
            Nakshatra.SWAATHI,
            Nakshatra.CHITRA,
            Nakshatra.ANURADHA,
            Nakshatra.UTTARA_ASHADA,
            Nakshatra.UTTARA_BHADRAPADA,
            Nakshatra.REVATHI
        )

        val premiumSundayNakshatras = setOf(
            Nakshatra.ROHINI,
            Nakshatra.PUSHYA,
            Nakshatra.HASTHA,
            Nakshatra.SHATABHISHA,
            Nakshatra.REVATHI,
            Nakshatra.UTTARA_PHALGUNI
        )

        // 1. Avoid certain Tamil months entirely
        if (tamilMonth !in allowedMonths) {
            return false
        }
        Log.i(TAG, "--pass 1 ---")

        // 2. Tuesday (Mangal) and Saturday (Shani) are strictly avoided for Subha Muhurthams
        if (vaara == Vaara.MANGAL || vaara == Vaara.SHANI) {
            return false
        }
        Log.i(TAG, "--pass 2 ---")

        // 3. Rikta Tithi and Chaturthi Exclusion Filter
        // Standard Chaturthi (4th), Navami (9th), Chaturdashi (14th) along with Amavasya/Purnima cycles
        val isChaturthi = (thithi == Thithi.CHATURTHI)
        val isRiktaTithi = isChaturthi || (thithi == Thithi.NAVAMI) || (thithi == Thithi.CHATURDASHI)

        if (isRiktaTithi) {
            // Exception Rule: Thursday Chaturthi forms a neutralizing Siddha Yoga
            // If it is a Thursday Chaturthi, we let it bypass this elimination check.
            val isThursdayChaturthi = (isChaturthi && vaara == Vaara.GURU)
            if (!isThursdayChaturthi) {
                return false
            }
        }

        val hasGoodThithi = thithi in allowedThithis || (isChaturthi && vaara == Vaara.GURU)
        if (!hasGoodThithi) {
            return false
        }
        Log.i(TAG, "--pass 3 ---")

        // 4. Dagdha Yoga (Burnt Day-Tithi Collisions) Check
        val isDagdhaDay = when (vaara) {
            Vaara.RAVI   -> thithi == Thithi.DWITIYA
            Vaara.SOMA   -> thithi == Thithi.CHATURTHI
            Vaara.MANGAL -> thithi == Thithi.PANCHAMI
            Vaara.BUDHA  -> thithi == Thithi.SHASHTI
            Vaara.GURU   -> thithi == Thithi.SAPTAMI
            Vaara.SHUKRA -> thithi == Thithi.ASHTAMI
            Vaara.SHANI  -> thithi == Thithi.NAVAMI
            else         -> false
        }
        if (isDagdhaDay) {
            return false
        }
        Log.i(TAG, "--pass 3.5 Dagdha Checked ---")

        // 5. Sunday (Ravi) Constraint Filter (Check this BEFORE the generic weekday list)
        if (vaara == Vaara.RAVI) {
            val isGoodSundayNakshatra = nakshatra in premiumSundayNakshatras
            Log.i(TAG, "--pass 4 (Sunday Branch: $isGoodSundayNakshatra) ---")
            return isGoodSundayNakshatra
        }
        Log.i(TAG, "--pass 4 ---")

        // 6. Nakshatra Baseline Verification
        val hasGoodNakshatra = nakshatra in allowedNakshatras
        if (!hasGoodNakshatra) {
            return false
        }
        Log.i(TAG, "--pass 5 ---")

        // 7. Monday, Wednesday, Thursday, Friday Output Finalization
        return vaara in allowedVaaras && nakshatra in allowedNakshatras
    }

} // end of object PanchangamCalculator

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

fun calculateStaticRahuYamaGulikai(
    sunrise: LocalDateTime
): Triple<TimeRange, TimeRange, TimeRange> {
    val date = sunrise.toLocalDate()
    val day = sunrise.dayOfWeek

    // Helper to convert LocalTime range to LocalDateTime TimeRange
    fun getStaticRange(map: Map<DayOfWeek, ClosedRange<LocalTime>>): TimeRange {
        val range = map[day]!!
        return TimeRange(
            date.atTime(range.start),
            date.atTime(range.endInclusive)
        )
    }

    return Triple(
        getStaticRange(StaticRahuKalamMap),
        getStaticRange(StaticYamaGandamMap),
        getStaticRange(StaticGulikaiKalamMap)
    )
}

fun calculateDynamicRahuYamaGulikai(
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

fun getRahuKaalamYamaGandamGulikaiPeriods(
    sunrise: LocalDateTime,
    sunset: LocalDateTime,
    useDynamic: Boolean
): Triple<TimeRange, TimeRange, TimeRange> {
    return if (useDynamic) {
        // Calls your original dynamic function
        calculateDynamicRahuYamaGulikai(sunrise, sunset)
    } else {
        // Calls the static mapping function
        calculateStaticRahuYamaGulikai(sunrise)
    }
}

// Flexible rounding extension to easily toggle between 15 or 30 minute snapping
fun LocalTime.roundToNearestMinutes(intervalMinutes: Int): LocalTime {
    val totalMinutes = this.hour * 60 + this.minute
    val roundedMinutes = ((totalMinutes + (intervalMinutes / 2)) / intervalMinutes) * intervalMinutes
    val roundedHour = (roundedMinutes / 60) % 24
    val roundedMinute = roundedMinutes % 60
    return LocalTime.of(roundedHour, roundedMinute)
}
fun LocalTime.floorToMinutes(intervalMinutes: Int): LocalTime {
    val totalMinutes = this.hour * 60 + this.minute
    // Integer division drops the remainder, forcing it to snap backward
    val flooredMinutes = (totalMinutes / intervalMinutes) * intervalMinutes

    val flooredHour = (flooredMinutes / 60) % 24
    val flooredMinute = flooredMinutes % 60
    return LocalTime.of(flooredHour, flooredMinute)
}

enum class HoraPlanet { SATURN, JUPITER, MARS, SUN, VENUS, MERCURY, MOON }

data class HoraWindow(val start: LocalTime, val end: LocalTime, val planet: HoraPlanet)

// 1. Define the data structure blueprint
data class AstronomicalSlotRule(
    val segmentIndex: Int,
    val fallbackSegmentIndex: Int? = null,
    val requiresSolsticeAdjustment: Boolean = false
)

// todo , on jun 18,2026, i think this is not mature or correct enough to be included
// so not calculating it until i am sure
fun getHighPrecisionNallaNeramWindows(
    currDttm: LocalDateTime,
    sunrise: LocalTime,
    sunset: LocalTime
): List<TimeWindow> {

    // 2. Define the structural priority map
    val astronomicalGrid = mapOf(
        DayOfWeek.SUNDAY    to listOf(AstronomicalSlotRule(2), AstronomicalSlotRule(7)),
        DayOfWeek.MONDAY    to listOf(AstronomicalSlotRule(1), AstronomicalSlotRule(8)),
        DayOfWeek.TUESDAY   to listOf(AstronomicalSlotRule(4, fallbackSegmentIndex = 2, requiresSolsticeAdjustment = true), AstronomicalSlotRule(8)),
        DayOfWeek.WEDNESDAY to listOf(AstronomicalSlotRule(3), AstronomicalSlotRule(8)),
        DayOfWeek.THURSDAY  to listOf(AstronomicalSlotRule(4), AstronomicalSlotRule(5)),
        DayOfWeek.FRIDAY    to listOf(AstronomicalSlotRule(3), AstronomicalSlotRule(8)),
        DayOfWeek.SATURDAY  to listOf(AstronomicalSlotRule(2), AstronomicalSlotRule(8))
    )

    // 1. Calculate the 12 Dynamic Daytime Horas
    val dayLength = Duration.between(sunrise, sunset)
    val horaWidth = dayLength.dividedBy(12)

    // 2. Establish the Chaldean Hora Order sequence
    val horaSequence = listOf(
        HoraPlanet.SATURN, HoraPlanet.JUPITER, HoraPlanet.MARS,
        HoraPlanet.SUN, HoraPlanet.VENUS, HoraPlanet.MERCURY, HoraPlanet.MOON
    )

    // 3. Determine the starting planet index based on the Day of the Week
    val dayStartPlanetIndex = when (currDttm.dayOfWeek) {
        DayOfWeek.SUNDAY    -> 3 // Sun
        DayOfWeek.MONDAY    -> 6 // Moon
        DayOfWeek.TUESDAY   -> 2 // Mars
        DayOfWeek.WEDNESDAY -> 5 // Mercury
        DayOfWeek.THURSDAY  -> 1 // Jupiter
        DayOfWeek.FRIDAY    -> 4 // Venus
        DayOfWeek.SATURDAY  -> 0 // Saturn
        null -> 0
    }

    // 4. Build the complete daytime Hora timeline
    val daytimeHoras = (0..11).map { index ->
        val start = sunrise.plus(horaWidth.multipliedBy(index.toLong()))
        val end = start.plus(horaWidth)
        val planet = horaSequence[(dayStartPlanetIndex + index) % 7]
        HoraWindow(start, end, planet)
    }

    // 5. Apply your custom Segment Priorities as the ultimate UI Filter
    val rules = astronomicalGrid[currDttm.dayOfWeek] ?: return emptyList()
    val isSolsticePeak = dayLength.toMinutes() > 740

    return rules.map { rule ->
        // Dynamically choose segment index based on solstice thresholds
        val targetSegment = if (rule.requiresSolsticeAdjustment && isSolsticePeak && rule.fallbackSegmentIndex != null) {
            rule.fallbackSegmentIndex
        } else {
            rule.segmentIndex
        }

        // Map the 8-segment index to an approximate target clock time
        val standard8SegmentWidth = dayLength.dividedBy(8)
        val targetTimeApprox = sunrise.plus(standard8SegmentWidth.multipliedBy((targetSegment - 1).toLong()))

        // Find the absolute closest auspicious Hora running at that specific time
        val matchedHora = daytimeHoras.minByOrNull { hora ->
            Duration.between(hora.start, targetTimeApprox).abs().toMinutes()
        } ?: daytimeHoras[0]

        // Apply our proven 30-minute floor truncation filter for stable UI anchors
        val displayStart = matchedHora.start //.floorToMinutes(30)
        val displayEnd = displayStart.plusMinutes(60)

        TimeWindow(displayStart, displayEnd)
    }
}

private fun getNextPanchangamRefreshTime(
    thithiEndTime: LocalDateTime, nakshatraEndTime: LocalDateTime,
    yogaEndTime: LocalDateTime, karanaEndTime: LocalDateTime,
    rahuKalamStartTime: LocalDateTime, rahuKalamEndTime: LocalDateTime,
    yamaGandamStartTime: LocalDateTime, yamaGandamEndTime: LocalDateTime,
    nallaNeramWindows: Nothing?
): LocalDateTime {

    val now = LocalDateTime.now()
    val today = LocalDate.now()

    // todo - was before ignoring nalla neram - bring back if we find it
    // todo important dont delete the commented section
//    val nextChange = (listOfNotNull(
//        thithiEndTime,
//        nakshatraEndTime,
//        yogaEndTime,
//        karanaEndTime,
//        rahuKalamStartTime,
//        rahuKalamEndTime,
//        yamaGandamStartTime,
//        yamaGandamEndTime
//    ) + nallaNeramWindows.flatMap { window ->
//        listOf(
//            LocalDateTime.of(today, window.start),
//            LocalDateTime.of(today, window.end)
//        )
//    })
//        .filter { it.isAfter(now) }
//        .minOrNull()

    // todo , since nalla neram logic aint good , discarding it for now , jun 18,26
    val nextChange = ( listOfNotNull(
        thithiEndTime,
        nakshatraEndTime,
        yogaEndTime,
        karanaEndTime,
        rahuKalamStartTime,
        rahuKalamEndTime,
        yamaGandamStartTime,
        yamaGandamEndTime
    ) )
        .filter { it.isAfter(now) }
        .minOrNull()

    // If nothing is upcoming, force refresh before next sunrise
    val nextChangeDttm = nextChange ?: now.plusHours(23)
    Log.i("PanchangamCalculator", "----next panchangam refresh time = $nextChangeDttm")

    return nextChangeDttm
}
