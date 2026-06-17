package com.sd.nithyadharma.screen

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.nithyadharma.model.*
import com.sd.nithyadharma.util.PreferencesManager
import com.sd.nithyadharma.model.Horoscope.HoroscopeChart
import com.sd.nithyadharma.model.Horoscope.HoroscopeInputParams
import com.sd.nithyadharma.model.Horoscope.HoroscopePeriod
import com.sd.nithyadharma.model.Horoscope.Panchanga
import com.sd.nithyadharma.model.Horoscope.PlanetPosition
import com.sd.nithyadharma.model.Horoscope.dbaName
import com.sd.nithyadharma.model.Horoscope.planetName
import com.sd.nithyadharma.model.Horoscope.planetShortName
import com.sd.nithyadharma.model.PanchangaAttributes.Rasi
import com.sd.nithyadharma.model.PanchangaAttributes.karanaName
import com.sd.nithyadharma.model.PanchangaAttributes.nakshatraName
import com.sd.nithyadharma.model.PanchangaAttributes.tithiName
import com.sd.nithyadharma.model.PanchangaAttributes.vaaraName
import com.sd.nithyadharma.model.PanchangaAttributes.yogaName
import com.sd.nithyadharma.util.HoroscopeCalculator
import com.sd.nithyadharma.util.Constants.dttmFormatter
import com.sd.nithyadharma.util.FirebaseAppAnalytics
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoroscopeScreen(
    preferencesManager: PreferencesManager,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentLang by preferencesManager.getSelectedLanguage()
        .collectAsState(initial = NDLanguage.EN)

    val customerInfoFlow = preferencesManager.getCustomerInfo()
    val customerInfo by customerInfoFlow.collectAsState(initial = CustomerInfo("", "", "", "", "", "", "", "", "", "", ""))

    var statusMessage by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }

    var horoscopeChart by remember { mutableStateOf<HoroscopeChart?>(null) }
    var isCalculated by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(customerInfo) {
        Log.d("HoroscopeScreen", "LaunchedEffect triggered")
        val (ok, msg) = validate(customerInfo)
        isValid = ok
        statusMessage = msg

        if (ok && !isCalculated) {
            Log.d("HoroscopeScreen", "Starting chart calculation...")

            try {
                val params = HoroscopeInputParams(
                    name = customerInfo.name,
                    date = LocalDate.parse(customerInfo.dttmOfBirth,dttmFormatter),
                    time = LocalTime.parse(customerInfo.dttmOfBirth,dttmFormatter),
                    latitude = customerInfo.lat.toDouble(),
                    longitude = customerInfo.lon.toDouble()
                )
                horoscopeChart = HoroscopeCalculator().calcBirthChart(params)
                isCalculated = true

                // send the user input details to firebase
                FirebaseAppAnalytics.logHoroscopeInputs(params)
                Log.d("HoroscopeScreen", "sent data to firebase")

                // todo - may be we should cache it now.
            } catch (e: Exception) {
                Log.e("HoroscopeScreen", "Error calculating chart", e)
                errorMessage = e.message
            } finally {
                isCalculated = false
//                Log.d("HoroscopeScreen", "Calculation finished. Chart: $horoscopeChart")
            }
        }
        else {
            horoscopeChart = null
            errorMessage = msg
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                title = { Text( LocaleManager.getString("btn_hr", currentLang) ) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,

                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        LocaleManager.getString("hr_bottom", currentLang) ,
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            if (!isValid && statusMessage.isNotBlank()) {
                Text(
                    text = statusMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }

            if (isValid) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()               // ← take full available width
                        .padding(horizontal = 8.dp)  // ← nice symmetric side padding
                        .padding(vertical = 8.dp),    // ← optional vertical breathing room
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // optional subtle shadow
                ){
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text( LocaleManager.getString("hs_birthdtls", currentLang), style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                LocaleManager.getString(
                                    "str_nm",
                                    currentLang
                                ) + " : " + customerInfo.name
                            )
                            Text(
                                LocaleManager.getString(
                                    "str_bdt",
                                    currentLang
                                ) + " : " + customerInfo.dttmOfBirth
                            )
                            Text(
                                LocaleManager.getString(
                                    "str_lat",
                                    currentLang
                                ) + " : " + customerInfo.lat
                            )
                            Text(
                                LocaleManager.getString(
                                    "str_lon",
                                    currentLang
                                ) + " : " + customerInfo.lon
                            )

                        }
                    }
                    // the horoscope box itself
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text( LocaleManager.getString("hs_birthchart", currentLang), style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(4.dp))

                        horoscopeChart?.let { chart ->
                            // Show Panchanga details
                            PanchangaDetails(chart.panchanga, currentLang)
                            // show chart
                            SouthIndianChart(
                                chart = chart,
                                currentLang = currentLang
                            )
                            // lets print dasha bukthis
                            Spacer(modifier = Modifier.height(8.dp))
                            Text( LocaleManager.getString("hs_dba", currentLang), style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(4.dp))

                            DashaBukthiDetails(chart.dbaPeriods, currentLang)

                        } ?: CircularProgressIndicator()

                    }
                }
            }
        }
    }

} // main screen composable ends


@Composable
fun PanchangaDetails(panchanga: Panchanga, currentLang: NDLanguage) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(LocaleManager.getString("str_vr", currentLang) + " : " + vaaraName(panchanga.vara, currentLang))
        Text(LocaleManager.getString("str_nk", currentLang) + " : " + nakshatraName(panchanga.nakshatra, currentLang))
        Text(LocaleManager.getString("str_tt", currentLang) + " : " + tithiName(panchanga.tithi, currentLang))
        Text(LocaleManager.getString("str_yg", currentLang) + " : " + yogaName(panchanga.yoga, currentLang))
        Text(LocaleManager.getString("str_kr", currentLang) + " : " + karanaName(panchanga.karana, currentLang))
    }
}

@Composable
fun DashaBukthiDetails(
    dbaPeriods: List<HoroscopePeriod>,
    currentLang: NDLanguage
) {
    Column(modifier = Modifier.padding(8.dp)) {
        dbaPeriods.forEach { dasha ->
            DashaCard(dasha, currentLang)
        }
    }
}

@Composable
private fun DashaCard(
    dasha: HoroscopePeriod,
    currentLang: NDLanguage
) {
    val startDate =
        HoroscopeCalculator.julianDayToLocalDate(dasha.startJulianDay)
    val endDate =
        HoroscopeCalculator.julianDayToLocalDate(dasha.endJulianDay)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text( text = "${dbaName(dasha.type, currentLang)} - ${
                    planetName(dasha.planet, currentLang) }",
                style = MaterialTheme.typography.titleLarge
            )

            Text("$startDate → $endDate")

            Spacer(Modifier.height(8.dp))

            // ⭐ recursive children rendering
            PeriodChildren(dasha.children, currentLang, level = 1)
        }
    }
}

@Composable
private fun PeriodChildren(
    periods: List<HoroscopePeriod>,
    currentLang: NDLanguage,
    level: Int
) {
    periods.forEach { period ->

        val startDate =
            HoroscopeCalculator.julianDayToLocalDate(period.startJulianDay)
        val endDate =
            HoroscopeCalculator.julianDayToLocalDate(period.endJulianDay)

        Column(
            modifier = Modifier.padding(
                start = (level * 16).dp,
                bottom = 6.dp
            )
        ) {
            Text(
                text = "${dbaName(period.type, currentLang)} - ${
                    planetName(period.planet, currentLang)
                }",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "$startDate → $endDate",
                style = MaterialTheme.typography.bodyMedium
            )

            // ⭐ recursion — automatically supports Anthara, Sookshma etc
            if (period.children.isNotEmpty()) {
                PeriodChildren(period.children, currentLang, level + 1)
            }
        }
    }
}

//@Composable
//private fun DashaCard(
//    dasha: HoroscopePeriod,
//    currentLang: NDLanguage
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 12.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.background
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(modifier = Modifier.padding(12.dp)) {
//
//            val startDate =
//                HoroscopeCalculator.julianDayToLocalDate(dasha.startJulianDay)
//            val endDate =
//                HoroscopeCalculator.julianDayToLocalDate(dasha.endJulianDay)
//
//            // Dasha header
//            Text(
//                text = "${dbaName(dasha.type, currentLang)} - ${
//                    planetName(dasha.planet, currentLang)
//                }",
//                style = MaterialTheme.typography.titleLarge
//            )
//
//            Spacer(Modifier.height(4.dp))
//
//            Text(
//                text = "$startDate → $endDate",
//                style = MaterialTheme.typography.bodyMedium
//            )
//
//            Spacer(Modifier.height(12.dp))
//
//            // Bhuktis inside same card
//            dasha.children.forEach { bhukti ->
//                BhuktiRow(bhukti, currentLang)
//            }
//        }
//    }
//}

@Composable
private fun BhuktiRow(
    bhukti: HoroscopePeriod,
    currentLang: NDLanguage
) {
    val startDate =
        HoroscopeCalculator.julianDayToLocalDate(bhukti.startJulianDay)
    val endDate =
        HoroscopeCalculator.julianDayToLocalDate(bhukti.endJulianDay)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, bottom = 8.dp)
    ) {
        Text(
            text = "${dbaName(bhukti.type, currentLang)} - ${
                planetName(bhukti.planet, currentLang)
            }",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "$startDate → $endDate",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SouthIndianChart(
    chart: HoroscopeChart,
    currentLang: NDLanguage,
    modifier: Modifier = Modifier
) {
    val planetsByRasi = chart.planets.groupBy { it.rasi }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp)
            .border(1.dp, Color.Black) // Outer border
    ) {
        // Row 1: Houses 12, 1, 2, 3
        Row(modifier = Modifier.weight(1f)) {
            ChartHouse(Rasi.MEENA, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
            ChartHouse(Rasi.MESHA, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
            ChartHouse(Rasi.VRISHABHA, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
            ChartHouse(Rasi.MITHUNA, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
        }

        // Row 2: House 11, empty center, House 4
        Row(modifier = Modifier.weight(1f)) {
            ChartHouse(Rasi.KUMBHA, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
            Box(modifier = Modifier.weight(2f)) // Empty center - no border
            ChartHouse(Rasi.KARKATAKA, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
        }

        // Row 3: House 10, empty center, House 5
        Row(modifier = Modifier.weight(1f)) {
            ChartHouse(Rasi.MAKARA, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
            Box(modifier = Modifier.weight(2f)) // Empty center - no border
            ChartHouse(Rasi.SIMHA, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
        }

        // Row 4: Houses 9, 8, 7, 6
        Row(modifier = Modifier.weight(1f)) {
            ChartHouse(Rasi.DHANUS, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
            ChartHouse(Rasi.VRISHCHIKA, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
            ChartHouse(Rasi.THULAA, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
            ChartHouse(Rasi.KANYA, planetsByRasi, chart.lagna, currentLang,
                Modifier.weight(1f).border(0.dp, Color.Black))
        }
    }
}

@Composable
fun ChartHouse(
    rasi: Rasi,
    planetsByRasi: Map<Rasi, List<PlanetPosition>>,
    lagna: Rasi,
    currentLang: NDLanguage,
    modifier: Modifier = Modifier
) {
    val planetsInThisRasi = planetsByRasi[rasi] ?: emptyList()
    val isLagna = rasi == lagna

    val fontSize = when {
        planetsInThisRasi.size > 4 -> 8.sp
        planetsInThisRasi.size > 2 -> 9.sp
        else -> 10.sp
    }

    Box(
        modifier = modifier
            .padding(2.dp)
    ) {
        // Draw diagonal dash for Lagna
        if (isLagna) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Draw diagonal line from bottom-left to top-right
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = Color.Red,
                        start = Offset(0f, size.height / 2),  // Bottom-left/2
                        end = Offset(size.width * 0.4f, 0f),  // Top-right diagonal
                        strokeWidth = 2.0.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Place "La" or "ல" at the top-left corner
                Text(
                    text = if (currentLang == NDLanguage.EN) "La" else "ல",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(1.dp)
                )
            }
        }

        // Planet names in center
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            planetsInThisRasi.forEach { planetPos ->
                Text(
                    text = planetShortName(planetPos.planet, currentLang),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

fun validate(profile: CustomerInfo): Pair<Boolean, String> {

    if (profile.name.isBlank())
        return false to "Name is required."

    if (profile.dttmOfBirth.isBlank())
        return false to "Date & Time of Birth is required."

    if (profile.lat.isBlank() || profile.lon.isBlank())
        return false to "Latitude and Longitude are required."

    val latD = profile.lat.toDoubleOrNull()
    val lonD = profile.lon.toDoubleOrNull()

    if (latD == null || lonD == null)
        return false to "Latitude / Longitude must be valid numbers."

    if (latD !in -90.0..90.0)
        return false to "Latitude must be between -90 and 90."

    if (lonD !in -180.0..180.0)
        return false to "Longitude must be between -180 and 180."

    return true to "Proceeding to calculate horoscope…"
}