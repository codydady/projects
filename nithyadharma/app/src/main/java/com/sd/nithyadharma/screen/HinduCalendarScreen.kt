package com.sd.nithyadharma.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.nithyadharma.dao.DataRepository
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.Constants
import com.sd.nithyadharma.util.PreferencesManager
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HinduCalendarScreen(
    preferencesManager : PreferencesManager,
    onBackClick: () -> Unit) {

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val currentLang by preferencesManager.getSelectedLanguage()
        .collectAsState(initial = NDLanguage.EN)

    // Directly observe the scheduleData from the DataRepository singleton
    // This will trigger recomposition whenever DataRepository._scheduleData.value changes
    val scheduleData by DataRepository.scheduleData // Observe the State object

    // 1️⃣ Load data when screen opens
    LaunchedEffect(Unit) {
        DataRepository.ensureScheduleLoaded()
    }

    val listState = rememberLazyListState()
    var hasAutoScrolled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(scheduleData) {
        if (scheduleData.isEmpty()) return@LaunchedEffect
        if (hasAutoScrolled) return@LaunchedEffect

        delay(500) // let LazyColumn measure

        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val targetIndex = scheduleData.indexOfFirst { item ->
            runCatching {
                LocalDate.parse(item.date, formatter)
            }.getOrNull()?.let {
                it.isEqual(today) || it.isAfter(today)
            } ?: false
        }.coerceAtLeast(0)

        // Soft approach
        val approachIndex = maxOf(0, targetIndex - 4)
        listState.scrollToItem(approachIndex)

        delay(300)

        // Gentle float-in
        listState.animateScrollToItem(
            index = targetIndex + 1, // if header exists
            scrollOffset = -120
        )

        hasAutoScrolled = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                title = { Text(LocaleManager.getString("btn_hc", currentLang)+" – $currentYear") },
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
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        LocaleManager.getString("hc_bottom", currentLang)
                                + Constants.NOTIFICATION_SCHEDULE_HOUR + ":"
                                + Constants.NOTIFICATION_SCHEDULE_MINUTE ,
//                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (scheduleData.isEmpty()) { // Check if data is loaded and not empty
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            else {
                val today = LocalDate.now()
                val nearestIndex = scheduleData?.indexOfFirst { item ->
                    val itemDate = try {
                        LocalDate.parse(item.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    } catch (e: Exception) {
                        null
                    }
                    itemDate != null && (itemDate.isEqual(today) || itemDate.isAfter(today))
                } ?: -1

                val isTamil = currentLang == NDLanguage.TA
                var occasionDtl = "-"
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {

                    item {
                        TableRow(listOf( LocaleManager.getString("cmn_date", currentLang),
                            LocaleManager.getString("cmn_occasion", currentLang)),
                            isHeader = true, rowIndex = 0)
                    }
                    itemsIndexed(scheduleData!!) { index, item ->
                        if (isTamil) {
                            occasionDtl = item.occasionTa
                        } else {
                            occasionDtl = item.occasionEn
                        }
                        TableRow(
                            listOf(formatDate(item.date), occasionDtl),
                            rowIndex = index + 1,
                            isNearest = (index == nearestIndex)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TableRow(
    cells: List<String>,
    isHeader: Boolean = false,
    rowIndex: Int,
    isNearest: Boolean = false
) {

    val backgroundColor = when {
        isHeader ->
            MaterialTheme.colorScheme.background
        isNearest ->
            MaterialTheme.colorScheme.secondaryContainer
        rowIndex % 2 == 0 ->
            MaterialTheme.colorScheme.surface
        else ->
            MaterialTheme.colorScheme.surfaceVariant
    }

    val weights = listOf(0.8f, 1.5f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        cells.forEachIndexed { index, cell ->
            Text(
                text = if (isNearest && index == 0) "👉 $cell" else cell,
                fontSize = if (isHeader) 16.sp else 14.sp,
                modifier = Modifier
                    .weight(weights.getOrElse(index) { 1f })
                    .padding(horizontal = 6.dp)
            )
        }
    }
}

fun formatDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        if (date != null) outputFormat.format(date) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}

