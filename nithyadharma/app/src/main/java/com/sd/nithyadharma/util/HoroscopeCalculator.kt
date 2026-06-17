package com.sd.nithyadharma.util

import android.util.Log
import com.sd.nithyadharma.model.Horoscope.AstrologyAccess
import com.sd.nithyadharma.model.Horoscope.HoroscopeChart
import com.sd.nithyadharma.model.Horoscope.HoroscopeInputParams
import com.sd.nithyadharma.model.Horoscope.HoroscopePeriod
import com.sd.nithyadharma.model.Horoscope.HoroscopePeriodType
import com.sd.nithyadharma.model.Horoscope.NAKSHATRA_SIZE
import com.sd.nithyadharma.model.Horoscope.NakshatraBalance
import com.sd.nithyadharma.model.Horoscope.Panchanga
import com.sd.nithyadharma.model.Horoscope.Planet
import com.sd.nithyadharma.model.Horoscope.PlanetPosition
import com.sd.nithyadharma.model.Horoscope.VIMSHOTTARI_TOTAL_YEARS
import com.sd.nithyadharma.model.Horoscope.VIMSHOTTARI_YEAR_DAYS
import com.sd.nithyadharma.model.Horoscope.getNakshatraLord
import com.sd.nithyadharma.model.Horoscope.vimshottariDashaOrder
import com.sd.nithyadharma.model.Horoscope.vimshottariDashaYears
import com.sd.nithyadharma.model.PanchangaAttributes.Karana
import com.sd.nithyadharma.model.PanchangaAttributes.Nakshatra
import com.sd.nithyadharma.model.PanchangaAttributes.Rasi
import com.sd.nithyadharma.model.PanchangaAttributes.Thithi
import com.sd.nithyadharma.model.PanchangaAttributes.Vaara
import com.sd.nithyadharma.model.PanchangaAttributes.Yoga
import swisseph.*
import java.time.*

// -------------------------------------------------
// horoscope computation for mobile app
// -------------------------------------------------

// todo should it be a class or object
class HoroscopeCalculator {

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
            hour,
            SweDate.SE_GREG_CAL
        ).julDay
    }

    private fun zodiacSignFromLongitude(lon: Double): Pair<Rasi, Double> {
        val norm = ((lon % 360) + 360) % 360
        val signIndex = (norm / 30).toInt()
        val degreeInSign = norm % 30
        return Rasi.entries[signIndex] to degreeInSign
    }

    private fun nakshatraFromLongitude(lon: Double): Nakshatra {
        val norm = ((lon % 360) + 360) % 360
        val size = 360.0 / 27.0
        val index = (norm / size).toInt().coerceIn(0, 26)
        return Nakshatra.entries[index]
    }

    private fun computeLagnaLongitude(jd: Double, lat: Double, lon: Double): Double {
        val cusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        swe.swe_houses(jd, SID_FLAGS, lat, lon, 'P'.code, cusps, ascmc)
        return (ascmc[0] + 360.0) % 360.0
    }

    private fun getRasiFromLongitude(longitude: Double): Rasi {
        // Ensure longitude is within 0-360
        val normalizedLon = (longitude % 360.0 + 360.0) % 360.0

        // Divide by 30 to get index 0 to 11
        val index = (normalizedLon / 30.0).toInt()

        // Return the corresponding Enum value
        return Rasi.entries[index]
    }

    fun calcBirthChart(input: HoroscopeInputParams): HoroscopeChart {

        val birthDttm = LocalDateTime.of(input.date, input.time)

        // todo , make all a these relative to user , TIMEZONE MUST BE FROM ANYWHERE
        val zoned = birthDttm
            .atZone(ZoneId.of("Asia/Kolkata"))
            .withZoneSameInstant(ZoneOffset.UTC)

        val jd = toJulianDayUTC(zoned)

        val sunX = planetState(jd, SweConst.SE_SUN)
        val moonX = planetState(jd, SweConst.SE_MOON)
        val marsX = planetState(jd, SweConst.SE_MARS)
        val mercX = planetState(jd, SweConst.SE_MERCURY)
        val venX = planetState(jd, SweConst.SE_VENUS)
        val jupX = planetState(jd, SweConst.SE_JUPITER)
        val satX = planetState(jd, SweConst.SE_SATURN)
        val rahuX = planetState(jd, SweConst.SE_TRUE_NODE)

        val lagnaLon = computeLagnaLongitude(jd, input.latitude, input.longitude)
        val lagna = getRasiFromLongitude(lagnaLon)

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
                Planet.KETU to ((rahuX[0] + 180.0) % 360.0)
            )
        }

        val planetPositions = Planet.entries.map { planet ->

            val lon = longitudeMap[planet] ?: 0.0
            val (sign, deg) = zodiacSignFromLongitude(lon)
            val nak = nakshatraFromLongitude(lon)

            PlanetPosition(
                planet = planet,
                rasi = sign,
                degree = deg,
                nakshatra = nak
            )
        }

        // now do the panchangam part
        val sunLongitude = longitudeMap[Planet.SUN] ?: 0.0
        val moonLongitude = longitudeMap[Planet.MOON] ?: 0.0

        // 1. Get the numeric value from Java (1 to 7)
        val javaIndex = zoned.dayOfWeek.value
        // 2. Adjust for Sunday-start:
        // Java: Mon(1), Tue(2), Wed(3), Thu(4), Fri(5), Sat(6), Sun(7)
        // Goal: Sun(0), Mon(1), Tue(2), Wed(3), Thu(4), Fri(5), Sat(6)
        val vaaraIndex = javaIndex % 7
        // 3. Get from your Enum entries
        val vara = Vaara.entries[vaaraIndex]

        val tithiIndex = (((moonLongitude - sunLongitude + 360) % 360) / 12).toInt()
        val nakIndex = (moonLongitude / 13.333333333333334).toInt()
        val yogaIndex = (((sunLongitude + moonLongitude) % 360) / 13.333333333333334).toInt()
        val karanaIndex = (((moonLongitude - sunLongitude + 360) % 360) / 6).toInt()

        val panchanga = Panchanga(
            vara = vara,
            tithi = Thithi.entries[tithiIndex % 16], // todo , IMPORTANT , NOT SURE MAY BE do paksha calc
            nakshatra = Nakshatra.entries[nakIndex % 27],
            yoga = Yoga.entries[yogaIndex % 27],
            karana = Karana.entries[karanaIndex % 11]
        )

        // lets proceed to calculate dasa bukthi

        Log.i("--horoscopeastrology--", "moon long = $moonLongitude")

        // to control depth
        val accessLevel =
            if (Constants.PAYING_CUSTOMER)
                AstrologyAccess(HoroscopePeriodType.ANTHARA)
            else
                AstrologyAccess(HoroscopePeriodType.BHUKTI)

        val dashaBukthiAntharas = calcVimshottariDasha(
            moonLongitude = moonLongitude,
            birthJulianDay = jd,
            vimshottariDashaYears = vimshottariDashaYears,
            accessLevel = accessLevel
        )

        // when dasha bukthi anthara is returned as list - preferred
        val horoscopeChart = HoroscopeChart(panchanga, lagna, planetPositions, dashaBukthiAntharas)

        return horoscopeChart
    }

    // kinda the equivalent of java static
    companion object {
        fun julianDayToLocalDate(jd: Double): String {
            val sd = SweDate(jd)

            val year = sd.year
            val month = sd.month
            val day = sd.day

            return "%02d-%02d-%04d".format(day, month, year)
        }
    }

    private fun calculateNakshatraBalance(moonLongitude: Double): NakshatraBalance {
        val normalized = (moonLongitude % 360.0 + 360.0) % 360.0

        // Exact position within nakshatra
        val exactNakshatraPosition = normalized / NAKSHATRA_SIZE
        val nakIndex = exactNakshatraPosition.toInt().coerceIn(0, 26)

        // How much of this nakshatra has elapsed
        val elapsedFraction = exactNakshatraPosition - nakIndex

        // Balance is what's remaining
        val balanceFraction = 1.0 - elapsedFraction

        Log.d("NakBalance", "Moon: $normalized°, Nak: $nakIndex, Elapsed: $elapsedFraction, Balance: $balanceFraction")

        return NakshatraBalance(
            nakIndex = nakIndex,
            balanceFraction = balanceFraction
        )
    }

    private fun nextPeriodType(type: HoroscopePeriodType): HoroscopePeriodType? {
        return HoroscopePeriodType.entries.firstOrNull {
            it.level == type.level + 1
        }
    }

    fun buildDashaLevel(
        startJD: Double,
        durationYears: Double,
        lord: Planet,
        type: HoroscopePeriodType,
        vimshottariDashaYears: Map<Planet, Double>,
        accessLevel: AstrologyAccess
    ): HoroscopePeriod {

        val totalDays = durationYears * VIMSHOTTARI_YEAR_DAYS
        val endJD = startJD + totalDays

        // ✅ STOP if user is not allowed deeper levels
        if (type.level >= accessLevel.maxPeriodLevel.level) {
            return HoroscopePeriod(type, lord, startJD, endJD)
        }

        // find next hierarchy level
        val nextType = nextPeriodType(type)

        // no deeper levels → leaf node
        if (nextType == null) {
            return HoroscopePeriod(type, lord, startJD, endJD)
        }

        val children = mutableListOf<HoroscopePeriod>()
        var runningStart = startJD

        val sequence = vimshottariDashaOrder
        val startIndex = sequence.indexOf(lord)

        repeat(sequence.size) { i ->
            val subLord = sequence[(startIndex + i) % sequence.size]

            val subYears =
                durationYears * (vimshottariDashaYears[subLord]!! / VIMSHOTTARI_TOTAL_YEARS)

            val child = buildDashaLevel(
                runningStart,
                subYears,
                subLord,
                nextType,
                vimshottariDashaYears,
                accessLevel
            )

            children.add(child)
            runningStart = child.endJulianDay
        }

        return HoroscopePeriod(type, lord, startJD, endJD, children)
    }

    private fun calcVimshottariDasha(
        moonLongitude: Double,
        birthJulianDay: Double,
        vimshottariDashaYears: Map<Planet, Double>,
        accessLevel: AstrologyAccess
    ): List<HoroscopePeriod> {

        val balance = calculateNakshatraBalance(moonLongitude)

        val startLord = getNakshatraLord(balance.nakIndex)
        val sequence = vimshottariDashaOrder
        val startIndex = sequence.indexOf(startLord)

        val result = mutableListOf<HoroscopePeriod>()
        var runningStart = birthJulianDay

        // First Mahadasha (balance portion)
        val firstFullYears = vimshottariDashaYears[startLord]!!
        val firstBalanceYears = firstFullYears * balance.balanceFraction

        val firstDasha = buildDashaLevel(
            runningStart,
            firstBalanceYears,
            startLord,
            HoroscopePeriodType.DASHA,
            vimshottariDashaYears,
            accessLevel
        )

        result.add(firstDasha)
        runningStart = firstDasha.endJulianDay

        // Remaining Mahadashas
        for (i in 1 until sequence.size) {
            val lord = sequence[(startIndex + i) % sequence.size]
            val years = vimshottariDashaYears[lord]!!

            val dasha = buildDashaLevel(
                runningStart,
                years,
                lord,
                HoroscopePeriodType.DASHA,
                vimshottariDashaYears,
                accessLevel
            )

            result.add(dasha)
            runningStart = dasha.endJulianDay
        }

        return result
    }

}
