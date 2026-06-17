package com.sd.nithyadharma.screen

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.nithyadharma.R
import androidx.compose.foundation.lazy.items // 🔑 Ensure this import is present
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.PlatformTextStyle
import androidx.core.content.edit
import com.sd.nithyadharma.model.PanchangaAttributes.Rasi
import com.sd.nithyadharma.model.PanchangaAttributes.computeVaaraFromSunrise
import com.sd.nithyadharma.model.PanchangaAttributes.karanaName
import com.sd.nithyadharma.model.PanchangaAttributes.nakshatraName
import com.sd.nithyadharma.model.PanchangaAttributes.rasiName
import com.sd.nithyadharma.model.PanchangaAttributes.tithiName
import com.sd.nithyadharma.model.PanchangaAttributes.vaaraName
import com.sd.nithyadharma.model.PanchangaAttributes.yogaName
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.model.PanchangaAttributes
import com.sd.nithyadharma.util.Constants
import com.sd.nithyadharma.util.Constants.RAHU_YAMA_GULIKAN_NALLANERAM_SCHEDULE_HOUR
import com.sd.nithyadharma.util.Constants.RAHU_YAMA_GULIKAN_NALLANERAM_SCHEDULE_MINUTE
import com.sd.nithyadharma.util.DailyRefreshFlow
import com.sd.nithyadharma.util.PanchangamCalculator.calculateDynamicPanchangamDetails
import com.sd.nithyadharma.util.PanchangamCalculator.calculateStaticPanchangamDetails
import com.sd.nithyadharma.util.PreferencesManager
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.sd.nithyadharma.model.PanchangaAttributes.DynamicPanchangam
import com.sd.nithyadharma.model.PanchangaAttributes.StaticPanchangam
import com.sd.nithyadharma.model.PanchangaAttributes.maasamName
import com.sd.nithyadharma.model.TimeRange
import com.sd.nithyadharma.model.TimeWindow
import com.sd.nithyadharma.util.PanchangamCalculator.calculateFuturePanchangam
import kotlinx.coroutines.launch
import kotlin.collections.joinToString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    preferencesManager : PreferencesManager,
    onNavigate: (String) -> Unit,
    onPreferencesClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onRALClick: () -> Unit,
    onHoroscopeClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Scroll state for the background screen
    val mainScrollState = rememberScrollState()

    // Scroll state for the Dialog (inside the 'if' block)
    val dialogScrollState = rememberScrollState()

    val currentLang by preferencesManager.getSelectedLanguage()
        .collectAsState(initial = NDLanguage.EN)

    val userRasi by preferencesManager.getSelectedRasi()
        .collectAsState(initial = Rasi.MESHA)

    // used for subsequent calculations
    val currDttm = LocalDateTime.now(ZoneId.of("Asia/Kolkata"))

    // section 1 - for static panchangam refresh at set time

    // this triggers a daily recalc of the rahu kalam, yamagandam etc once a day
    val currentStaticTriggerDateTime by DailyRefreshFlow.observeRefreshForStaticPanchangamDetails(
        triggerTime = LocalTime.of(RAHU_YAMA_GULIKAN_NALLANERAM_SCHEDULE_HOUR,
            RAHU_YAMA_GULIKAN_NALLANERAM_SCHEDULE_MINUTE) // Refresh at 4:30 AM
    ).collectAsState(initial = currDttm)

    // per grok, this recomputes when date changes at whatever ealy mornin time we set.
    val staticPanchangam by produceState<StaticPanchangam?>(initialValue = null,
        currentStaticTriggerDateTime) {
        Log.d("MainScreen", "calculateStaticPanchangamDetails triggered at: ${currDttm}")
        value = calculateStaticPanchangamDetails(currDttm)  // Safe suspend call
    }

    // section 2 - for dynamic panchangam refresh at calculated time
    // No manual while loop here! observeDynamicRefresh handles it.

    val dynamicPanchangam by DailyRefreshFlow.observeRefreshForDynamicPanchangamDetails(
        calculator = {currDttm ->
            Log.d("MainScreen", "calculateDynamicPanchangamDetails triggered at: ${currDttm}")
            calculateDynamicPanchangamDetails(currDttm, userRasi, true)// true mode indicates do full dynamic panchanga calc
        },
        getExpiry = { data -> data.expiryDttm }
    ).collectAsState(initial = null)

    // section 3 - for next N days panchangam
    val futurePanchangam by produceState<List<Pair<StaticPanchangam, DynamicPanchangam>>?>(
        initialValue = null,
        currDttm,
        userRasi
    ) {

        Log.d(
            "MainScreen",
            "calculateFuturePanchangam triggered"
        )

        value = calculateFuturePanchangam(
            days = Constants.FUTURE_PANCHANGAM_CALCULATION_DAYS,
            userRasi = userRasi
        )

        //remove once verified todo
        value?.forEachIndexed { index, (staticInfo, dynamicInfo) ->

            Log.d(
                "--------FuturePanchangam------",
                """
            Day ${index + 1}
            Date=${dynamicInfo.calcDttm.toLocalDate()}
            Vaara=${dynamicInfo.vaara}
            Thithi=${dynamicInfo.thithi}
            Nakshatra=${dynamicInfo.nakshatra}
            Nakshatra end time=${dynamicInfo.nakshatraEndTime}
            Chandrashtama=${dynamicInfo.chandrashtamaRasi}
            Rahu=${staticInfo.rahuKalam}
            Yama=${staticInfo.yamaGandam}
            Score=${dynamicInfo.score}
            """.trimIndent()
            )
        }
    }

    ///---- new feature dialog ---

    // 2. The "One-Time" Logic
    // 1. State to control the Dialog
    var showUpdateDialog by remember { mutableStateOf(false) }

    // change it with every release and also the subsequent showupdate alertdialog
    val featureKey = "v_"+ Constants.APP_VERSION+"_features"

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        // Using a version-specific key so all existing users see this once
        val hasSeenUpdate = prefs.getBoolean(featureKey, false)

        if (!hasSeenUpdate) {
            showUpdateDialog = true
        }
    }

    // 3. The Dialog UI
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { /* Optional: showUpdateDialog = false */ },
            confirmButton = {
                Button(
                    onClick = {
                        // Mark as seen and close
                        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        // todo - important, uncomment this for testing and enable only at time of
                        // production build
                        prefs.edit() { putBoolean(featureKey, true) }
                        showUpdateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
                ) {
                    Text("Next (அடுத்தது)")
                }
            },
            title = { Text("What's New! / புதிய அம்சங்கள்") },
            text = {
                Column (modifier = Modifier
                    .verticalScroll(dialogScrollState)
                    .fillMaxWidth()
                ){
                    FeatureRow(
                        icon = Icons.Default.Language,
                        title = "Language / மொழி",
                        desc = "You may use App in both English and Tamizh / இப்போது தமிழ் மற்றும் ஆங்கிலத்தில் பயன்படுத்தலாம்."
                    )
                    FeatureRow(
                        icon = Icons.Default.CalendarMonth,
                        title = "Next 7 days panchangam / அடுத்த ஏழு நாட்கள்",
                        desc = "See upcoming 7 days panchangam / அடுத்த ஏழு நாட்களைப் பற்றிய விவரங்கள் மற்றும் சுப முகூர்த்த நாட்கள் "
                    )
//                    FeatureRow(
//                        icon = Icons.Default.NotificationsActive,
//                        title = "Notifications / அறிவிப்புகள்",
//                        desc = "Special announcements on days that matter / தினசரி பஞ்சாங்கம் மற்றும் விசேஷ அறிவிப்புகள்."
//                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
    /// --- end of new feature dialog ---

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),

                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Image(
//                            painter = painterResource(id = R.drawable.dakshinamurthy),
//                            contentDescription = null,
//                            modifier = Modifier.size(32.dp)
//                        )
//                        Spacer(Modifier.width(8.dp))
                        Text(LocaleManager.getString("app_title", currentLang),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                },
                actions = {
                    Spacer(Modifier.width(8.dp)) // Air at the edge

                    LanguageToggle(
                        selectedLang = currentLang,
                        onLangSelected = { newLang ->
                            scope.launch {
                                preferencesManager.saveSelectedLanguage(newLang)
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp)) // Air at the edge

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                        onDismissRequest = { showMenu = false },
                    )
                    {
                        DropdownMenuItem(text = { Text(LocaleManager.getString("btn_pf", currentLang))
                        }, onClick = {
                            showMenu = false; onPreferencesClick()
                        })
                        DropdownMenuItem(text = { Text(LocaleManager.getString("btn_rh", currentLang))
                        }, onClick = {
                            showMenu = false; onRALClick()
                        })
                        DropdownMenuItem(text = { Text(LocaleManager.getString("btn_fb", currentLang))
                        }, onClick = {
                            showMenu = false; onFeedbackClick()
                        })
                        DropdownMenuItem(text = { Text(LocaleManager.getString("btn_hr", currentLang))
                        }, onClick = {
                            showMenu = false; onHoroscopeClick()
                        })
                        DropdownMenuItem(text = { Text(LocaleManager.getString("btn_ab", currentLang))
                        }, onClick = {
                            showMenu = false; onAboutClick()
                        })
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                if (dynamicPanchangam != null) {
                    BottomScoreBlock(dynamicPanchangam!!, currentLang)
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(mainScrollState)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ───────────── all content ─────────────
            // for the title and kural
            SectionCard {
                HeaderBlock(currentLang)
            }

            // for the scrolling buttons
            SectionCard {
//                ButtonGrid(onNavigate,currentLang)
                ButtonScrollRow(onNavigate,currentLang)
            }

            // for the current panchanga details
            if (dynamicPanchangam != null) {
                SectionCard {
                    PanchangamDetailsSection(staticPanchangam, dynamicPanchangam!!, currentLang)
                }
            }

            // for the next N days forecast
            SectionCard {
                futurePanchangam?.let {
                    FuturePanchangamSection(
                        futurePanchangam = it,
                        currentLang = currentLang
                    )
                }            }
        }
    }
}

// ... (ScoreMeter is not included here, assuming it's elsewhere or a typo for Canvas)

@Composable
fun LanguageToggle(
    selectedLang: NDLanguage,
    onLangSelected: (NDLanguage) -> Unit
) {
    // Colors consistent with your Ivory/Sand palette
    val toggleBg = Color(0xFFDACFAB) // Slightly darker than Ivory for contrast
    val activeColor = Color(0xFFBCAE8E) // Your "Sand" active color

    Row(
        modifier = Modifier
            .height(36.dp)
            .width(100.dp)
            .clip(RoundedCornerShape(18.dp)) // Fully rounded "pill"
            .background(toggleBg)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NDLanguage.entries.forEach { language ->
            val isSelected = language == selectedLang

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) activeColor else Color.Transparent)
                    .clickable { onLangSelected(language) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (language == NDLanguage.TA) "த" else "En", // Compact for App Bar
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color.White else Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class ScoreInfo(val label: String, val color: Color)

fun getScoreLabelColor(score: Int, currentLang: NDLanguage): ScoreInfo {
    val scoreColor = when {
        score >= 75 -> Color(0xFF4CAF50) // Green
        score >= 50 -> Color(0xFFFF7700) // Orange
        score >= 25 -> Color(0xFFF44336) // Red
        else -> Color(0xFF562421) // Dark brown
    }
    val scoreLabel = when {
        score >= 75 -> LocaleManager.getString("str_ex", currentLang)
        score >= 50 -> LocaleManager.getString("str_gd", currentLang)
        score >= 25 -> LocaleManager.getString("str_av", currentLang)
        else -> LocaleManager.getString("str_pr", currentLang)
    }
    return ScoreInfo(label = scoreLabel, color = scoreColor)
}

@Composable
fun HeaderBlock(currentLang: NDLanguage) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp) // semantic spacing only
    ) {

        // Row 1: title + icon
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start ) {

            Text(
                text = LocaleManager.getString("advance_title", currentLang),
//                modifier = Modifier.weight(0.8f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.width(24.dp))

            Card(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Row 2: motto
        Text(
            text = LocaleManager.getString("app_motto", currentLang),
            lineHeight = 18.sp,
        )
    }
}

@Composable
fun ButtonScrollRow(onNavigate: (String) -> Unit, currentLang: NDLanguage) {
    val buttons = listOf(
        Triple(LocaleManager.getString("btn_tm", currentLang), "tm", Color(0xFFB9A39D)),
        Triple(LocaleManager.getString("btn_pp", currentLang), "pp", Color(0xFFBEA27B)),
        Triple(LocaleManager.getString("btn_mc", currentLang), "mc", Color(0xFFA0B09F)),
        Triple(LocaleManager.getString("btn_hc", currentLang), "hc", Color(0xFFBEA58F)),
        Triple(LocaleManager.getString("btn_ll", currentLang), "ll", Color(0xFF988E84)),
        Triple(LocaleManager.getString("btn_mp", currentLang), "mp", Color(0xFF939076)),
    ).toMutableList()

    if (Constants.PAYING_CUSTOMER) {
        buttons.add(Triple(LocaleManager.getString("btn_md", currentLang), "md", Color(0xFFB1A3C9)))
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 1.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(buttons) { (label, id, color) ->
                // Compact button designed for horizontal flow
                Card(
                    modifier = Modifier
                        .width(110.dp)
                        .height(70.dp)
                        .clickable { onNavigate(id) },
                    colors = CardDefaults.cardColors(containerColor = color),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge.copy(
                                lineHeight = 16.sp, // 🔑 Ensures two lines don't overlap
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false // 🔑 Removes extra space to keep it centered
                                )
                            ),
                            overflow = TextOverflow.Ellipsis,
                            color = Color.DarkGray // High contrast for the dark tones you chose
                        )
                    }
                }
            }
        }
        //
        // 3. The Floating Overlay (The "Sticky" Indicator)
        // We only show this if there's more to see
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd) // 🔑 This keeps it stuck to the right
                .fillMaxHeight()
                .width(40.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFF5F5DC).copy(alpha = 0.9f)),
                        startX = 0f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Scroll More",
                tint = Color.Gray.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 4.dp)
            )
        }
    }
}

@Composable
fun ButtonGrid(onNavigate: (String) -> Unit,currentLang: NDLanguage) {

    val buttons = listOf(
        Triple(LocaleManager.getString("btn_hc", currentLang), "hc", Color(0xFFBB9B7E)),
        Triple(LocaleManager.getString("btn_mc", currentLang), "mc", Color(0xFF8C9B8B)),
        Triple(LocaleManager.getString("btn_tm", currentLang), "tm", Color(0xFFB29086)),
        Triple(LocaleManager.getString("btn_ll", currentLang), "ll", Color(0xFF968777)),
        Triple(LocaleManager.getString("btn_mp", currentLang), "mp", Color(0xFF96926F)),
        Triple(LocaleManager.getString("btn_pp", currentLang), "pp", Color(0xFFC29962))
    )
    val mutableButtons = buttons.toMutableList()

    if ( Constants.PAYING_CUSTOMER) {
        val newElement = Triple(LocaleManager.getString("btn_md", currentLang), "md", Color(0xFFAA96CC))
        mutableButtons.add(newElement)
    }

    Column {
        mutableButtons.chunked(2).forEachIndexed { index, row ->

            // 🔑 Explicit vertical spacing BETWEEN rows
            if (index > 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, id, color) ->
                    Button(
                        onClick = { onNavigate(id) },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 64.dp)
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        )
                        {
                            Text(
                                text = label,
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun BottomScoreBlock(dp: DynamicPanchangam, currentLang: NDLanguage) {
    val timeFmt = DateTimeFormatter.ofPattern("hh:mm a")
    val scoreInfo = getScoreLabelColor(dp.score, currentLang)

    // old - to print actual dates in bottom bar
    val vaaraOfStartOfWindow = dp?.let { data ->
        computeVaaraFromSunrise(data.calcDttm!!, data.sunrise!!)
    }?.let { vaaraName(it, currentLang) } ?: "Vaara: Calculating..."

    val vaaraOfEndOfWindow = dp?.let { data ->
        computeVaaraFromSunrise(data.expiryDttm!!, data.sunrise!!)
    }?.let { vaaraName(it, currentLang) } ?: "Vaara: Calculating..."

    // new - Replace the Vaara logic with your Relative Day logic
    val startDayStr = getDayRelativeToNow(dp.calcDttm!!, currentLang)
    val endDayStr = getDayRelativeToNow(dp.expiryDttm!!, currentLang)

    // 🔑 Logic: Check if start and end days are identical
    val isSameDay = startDayStr == endDayStr

    val statusText = if (isSameDay) {
        // Template for: "Today, 10:00 AM to 02:00 PM (Good)"
        LocaleManager.getString("str_currastrostatus_sameday",
            currentLang,
            startDayStr,
            dp.calcDttm.format(timeFmt),
            dp.expiryDttm.format(timeFmt),
            scoreInfo.label)
    }
    else {
        LocaleManager.getString("str_currastrostatus",
            currentLang,
            startDayStr,
            dp.calcDttm.format(timeFmt),
            endDayStr,
            dp.expiryDttm.format(timeFmt),
            scoreInfo.label)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(0.8f)) {
            Text(
                text = statusText,
                lineHeight = 20.sp,   // 🔑 controls air
                maxLines = 3
            )
        }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .weight(0.2f)
                .fillMaxHeight()     // Use full Row height
                .padding(vertical = 8.dp),  // Optional vertical breathing room
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize().padding(4.dp)) { // ← This creates space around the arc) {
                val strokeWidth = 8.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                drawArc(
                    color = scoreInfo.color,
                    startAngle = -90f,
                    sweepAngle = (dp.score / 100f) * 360f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(radius * 2, radius * 2) ,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = dp.score.toString(),
                fontWeight = FontWeight.Bold,
                color = scoreInfo.color,
                lineHeight = 16.sp   // 🔑 tight but scalable
            )
        }
    }
}

@Composable
fun PanchangamItem1Row(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label – ",
            lineHeight = 20.sp   // 🔑 KEY FIX
        )
        Text(
            text = value,
            color = Color(0xFF024649),
            lineHeight = 20.sp   // 🔑 KEY FIX
        )
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

fun TimeRange.toDisplayString(): String {
    return "${start.format(timeFormatter)} - ${end.format(timeFormatter)}"
}

private fun getDayRelativeToNow(eventTime: LocalDateTime, currentLang: NDLanguage): String {
    val now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
    val today = now.toLocalDate()
    val tomorrow = today.plusDays(1)
    val eventDate = eventTime.toLocalDate()

    val key = when (eventDate) {
        today -> "today"
        tomorrow -> "tomorrow"
        else -> if (eventTime.isAfter(now)) "later" else "past"
    }
    val evday = when (currentLang) {
        NDLanguage.EN -> when (key) {
            "today" -> "Today"
            "tomorrow" -> "Tomorrow"
            "later" -> "Day after tomorrow"
            "past" -> "Past"
            else -> ""
        }
        NDLanguage.TA -> when (key) {
            "today" -> "இன்று"
            "tomorrow" -> "நாளை"
            "later" -> "நாளை மறுநாள்" // hoping a nakshatra or thithi can never cross 48 hr window!
            "past" -> "கடந்தது"
            else -> ""
        }
    }
    Log.d("---MainScreen", "getDayRelativeToNow has: ${evday}")

    return evday
}

fun getTimeSlotLocalized(time: LocalDateTime, lang: NDLanguage): String {
    val hour = time.hour  // 0-23

    val slot = when {
        hour < 12 -> "morning"
        hour < 16 -> "afternoon"
        hour < 19 -> "evening"
        else -> "night"
    }

    return when (lang) {
        NDLanguage.EN -> when (slot) {
            "morning"   -> "Morning"
            "afternoon" -> "Afternoon"
            "evening"   -> "Evening"
            "night"     -> "Night"
            else        -> ""
        }
        NDLanguage.TA -> when (slot) {
            "morning"   -> "காலை"
            "afternoon" -> "மதியம்"
            "evening"   -> "மாலை"
            "night"     -> "இரவு"
            else        -> ""
        }
    }
}

fun getAmPm(time: LocalDateTime): String {
    return if (time.hour < 12) "AM" else "PM"
}

fun formatNallaNeramWindowsToString(windows: List<TimeWindow>?): String {
    return windows?.joinToString(separator = "\n") { window ->
        // 1. Convert start hour to 12-hour format
        val startHourRaw = window.start.hour % 12
        val startHour12 = if (startHourRaw == 0) 12 else startHourRaw
        val startMin = window.start.minute.toString().padStart(2, '0')

        // 2. Convert end hour to 12-hour format
        val endHourRaw = window.end.hour % 12
        val endHour12 = if (endHourRaw == 0) 12 else endHourRaw
        val endMin = window.end.minute.toString().padStart(2, '0')

        // 3. Determine AM/PM marker (lowercased)
        // todo make this time a datetime so it can be passed to getAmPm
        val amPmMarker = if (window.start.hour < 12) "AM" else "PM"
//        val amPmMarker = getAmPm(window.start)


        // 4. Combine into a single window line
        "$startHour12:$startMin - $endHour12:$endMin $amPmMarker"
    } ?: "-"
}

@Composable
fun PanchangamDetailsSection(sp: StaticPanchangam?, dp: DynamicPanchangam, currentLang: NDLanguage) {

    if (sp == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val timeformatter = DateTimeFormatter.ofPattern("h:mm")
    //todo , get from supplied currdttm or calcdttm, dont do the asia kolkata thing as its erroneous
    val now = LocalDateTime.now(Constants.INDIA_ZONE)

    val maasam = dp?.maasam?.let { maasam -> maasamName(maasam, currentLang) } ?: "Maasam: Calculating..."
    val vaara = dp?.vaara?.let { vaara -> vaaraName(vaara, currentLang) } ?: "Vaara: Calculating..."
    val thithi = dp?.thithi?.let { tithi -> tithiName(tithi, currentLang) } ?: "Tithi: Calculating..."
    val nakshatra = dp?.nakshatra?.let { nakshatra -> nakshatraName(nakshatra, currentLang) } ?: "Nakshatra: Calculating..."
    val yoga = dp?.yoga?.let { yoga -> yogaName(yoga, currentLang) } ?: "Yoga: Calculating..."
    val karana = dp?.karana?.let { karana -> karanaName(karana, currentLang) } ?: "Karana: Calculating..."
    val chandrashtamam = dp?.chandrashtamaRasi?.let { chandrashtamam -> rasiName(chandrashtamam, currentLang) } ?: "Chnd Rasi: Calculating..."

    // print indru / today here
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Today Text
        Text(
            text = LocaleManager.getString("str_today", currentLang) + " - " + vaara,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        // 2. Conditional Separator and Muhurtha Text
        if (dp.muhurthaDay == true) {
            Text(
                text = " - ",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary // Matches your standard label color
            )
            Text(
                text = LocaleManager.getString("str_muhurtha", currentLang),
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp) // 🔑
    ) {

//        PanchangamItem1Row(LocaleManager.getString("str_sunrise", currentLang), sp.sunrise?.format(timeFormatter) + "") // ending "" is to just make it a string
//        PanchangamItem1Row(LocaleManager.getString("str_sunset", currentLang), sp.sunset?.format(timeFormatter) + "")
        PanchangamItem1Row(LocaleManager.getString("str_sunriseset", currentLang),
            sp.sunrise?.format(timeFormatter) + " / " + sp.sunset?.format(timeFormatter) + "")
        PanchangamItem1Row(LocaleManager.getString("str_rg", currentLang), sp.rahuKalam?.toDisplayString() ?: "")
        PanchangamItem1Row(LocaleManager.getString("str_ya", currentLang), sp.yamaGandam?.toDisplayString() ?: "")
        PanchangamItem1Row(LocaleManager.getString("str_gk", currentLang), sp.gulikan?.toDisplayString() ?: "")

        // todo , on jun 18,2026, i think this is not mature or correct enough to be included
//        PanchangamItem1Row(LocaleManager.getString("str_nn", currentLang),
//            formatNallaNeramWindowsToString(sp.nallaNeram)
//        )

        HorizontalDivider()

        PanchangamItem1Row(LocaleManager.getString("str_month", currentLang), maasam)

        PanchangamItem1Row(LocaleManager.getString("str_nk", currentLang),
            LocaleManager.getString("str_timeend", currentLang, nakshatra,
                getDayRelativeToNow(dp.nakshatraEndTime?:now,currentLang),
                getTimeSlotLocalized(dp.nakshatraEndTime?:now,currentLang),
                dp.nakshatraEndTime?.format(timeformatter) ?: "—",
                getAmPm(dp.nakshatraEndTime?:now ))
        )
        PanchangamItem1Row(LocaleManager.getString("str_tt", currentLang),
            LocaleManager.getString("str_timeend", currentLang, thithi,
                getDayRelativeToNow(dp.thithiEndTime?:now,currentLang),
                getTimeSlotLocalized(dp.thithiEndTime?:now,currentLang),
                dp.thithiEndTime?.format(timeformatter) ?: "—",
                getAmPm(dp.thithiEndTime?:now))
        )
        PanchangamItem1Row(LocaleManager.getString("str_yg", currentLang),
            LocaleManager.getString("str_timeend", currentLang, yoga,
                getDayRelativeToNow(dp.yogaEndTime?:now,currentLang),
                getTimeSlotLocalized(dp.yogaEndTime?:now,currentLang),
                dp.yogaEndTime?.format(timeformatter) ?: "—",
                getAmPm(dp.yogaEndTime?:now ))
        )
        PanchangamItem1Row(LocaleManager.getString("str_kr", currentLang),
            LocaleManager.getString("str_timeend", currentLang, karana,
                getDayRelativeToNow(dp.karanaEndTime?:now,currentLang),
                getTimeSlotLocalized(dp.karanaEndTime?:now,currentLang),
                dp.karanaEndTime?.format(timeformatter) ?: "—",
                getAmPm(dp.karanaEndTime?:now ))
        )
        PanchangamItem1Row(LocaleManager.getString("str_cr", currentLang), chandrashtamam)

    }

}

@Composable
fun FuturePanchangamSection(
    futurePanchangam: List<Pair<StaticPanchangam, DynamicPanchangam>>,
    currentLang: NDLanguage
) {
    val now = LocalDateTime.now(Constants.INDIA_ZONE)
    val timeformatter = DateTimeFormatter.ofPattern("h:mm")

    Text(
        text = LocaleManager.getString(
            "nextndays",
            currentLang,
            Constants.FUTURE_PANCHANGAM_CALCULATION_DAYS
        ),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        futurePanchangam.forEach { (sp, dp) ->

            var expanded by remember {
                mutableStateOf(false)
            }
            val maasam = dp?.maasam?.let { maasam -> maasamName(maasam, currentLang) } ?: "Maasam: Calculating..."

            val vaara = dp?.vaara?.let { vaara -> vaaraName(vaara, currentLang) } ?: "Vaara: Calculating..."

            val thithi = dp?.thithi?.let { tithi -> tithiName(tithi, currentLang) } ?: "Tithi: Calculating..."
            val nakshatra = dp?.nakshatra?.let { nakshatra -> nakshatraName(nakshatra, currentLang) } ?: "Nakshatra: Calculating..."

            val thithiNext = with(PanchangaAttributes) {
                dp?.thithi?.next()?.let { thithiNext -> tithiName(thithiNext, currentLang) } ?: "Tithi: Calculating..."
            }
            val nakshatraNext = with(PanchangaAttributes) {
                dp?.nakshatra?.next()?.let { nakshatraNext -> nakshatraName(nakshatraNext, currentLang) } ?: "Nakshatra: Calculating..."
            }
            // this is only to display next day in the future days title. not valuable
            // as thithinext or nakshatranext - important
            val vaaraNext = with(PanchangaAttributes) {
                dp?.vaara?.next()?.let { vaaraNext -> vaaraName(vaaraNext, currentLang) } ?: "Vaara: Calculating..."
            }

            SectionCard {

                val dateFormatter =
                    DateTimeFormatter.ofPattern("dd MMM")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expanded = !expanded
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            vaara + " - " + dp.calcDttm.toLocalDate().format(dateFormatter) + " - " + dp.score,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (dp.muhurthaDay == true) {
                            Color.Red
                        } else {
                            LocalContentColor.current
                        }
                    )

                    Icon(
                        imageVector =
                            if (expanded)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(expanded) {

                    Column {

                        HorizontalDivider()
                        PanchangamItem1Row(LocaleManager.getString("str_month", currentLang), maasam)

                        PanchangamItem1Row(LocaleManager.getString("str_tt", currentLang),
                            LocaleManager.getString("str_fut_timeend", currentLang, thithi,
                                getTimeSlotLocalized(dp.thithiEndTime?:now,currentLang),
                                dp.thithiEndTime?.format(timeformatter)?: "—",
                                getAmPm(dp.thithiEndTime?:now),
                                thithiNext)
                        )

                        PanchangamItem1Row(LocaleManager.getString("str_nk", currentLang),
                            LocaleManager.getString("str_fut_timeend",
                                currentLang, nakshatra,
                                getTimeSlotLocalized(dp.nakshatraEndTime?:now,currentLang),
                                dp.nakshatraEndTime?.format(timeformatter)?: "—",
                                getAmPm(dp.nakshatraEndTime?:now),
                                nakshatraNext)
                        )

                        // todo , on jun 18,2026, i think this is not mature or correct enough to be included

//                        PanchangamItem1Row(LocaleManager.getString("str_nn", currentLang),
//                            formatNallaNeramWindowsToString(sp.nallaNeram)
//                        )

                        PanchangamItem1Row(
                            LocaleManager.getString("str_cr", currentLang),
                            dp.chandrashtamaRasi?.let {
                                rasiName(it, currentLang)
                            } ?: "-"
                        )

                        if (dp.muhurthaDay == true) {
                            Text(
                                text = LocaleManager.getString("str_muhurtha", currentLang),
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }

                        HorizontalDivider()

                        PanchangamItem1Row(
                            LocaleManager.getString("str_rg", currentLang),
                            sp.rahuKalam?.toDisplayString() ?: "-"
                        )

                        PanchangamItem1Row(
                            LocaleManager.getString("str_ya", currentLang),
                            sp.yamaGandam?.toDisplayString() ?: "-"
                        )

                        PanchangamItem1Row(
                            LocaleManager.getString("str_gk", currentLang),
                            sp.gulikan?.toDisplayString() ?: "-"
                        )

                        HorizontalDivider()

                        PanchangamItem1Row(
                            "Score",
                            dp.score.toString()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
fun FeatureRow(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF009688), // Your theme color
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
    }
}