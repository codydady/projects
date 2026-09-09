package com.sd.nithyadharma.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sd.nithyadharma.dao.HinduCalendarRepository
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.LocalAppLanguage
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HinduCalendarCardContent(
    textColor: Color
) {
    val scheduleData by HinduCalendarRepository.scheduleData
    val currentLang = LocalAppLanguage.current

//    val isTamil = currentLang == NDLanguage.TA

    val today = remember { LocalDate.now() }
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    val numEvents = 20 // 20 rows to show

    val upcomingEvents = remember(scheduleData) {
        scheduleData.filter { item ->
            val itemDate = runCatching { LocalDate.parse(item.date, formatter) }.getOrNull()
            itemDate != null && !itemDate.isBefore(today)
        }.take(numEvents)
    }

    LaunchedEffect(Unit) {
        HinduCalendarRepository.ensureScheduleLoaded()
    }

    if (scheduleData.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = textColor, strokeWidth = 2.dp)
        }
    } else if (upcomingEvents.isEmpty()) {
        Text(
            text = "No upcoming events",
            color = textColor.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp) // 👈 Reduced from 6.dp to 2.dp
        ) {
            upcomingEvents.forEach { item ->
//                val occasionDtl = if (isTamil) item.occasionTa else item.occasionEn
                val occasionDtl = when (currentLang) {
                    NDLanguage.TA -> item.occasionTa
                    NDLanguage.KA -> item.occasionKa
                    NDLanguage.EN -> item.occasionEn
                    NDLanguage.HI -> item.occasionHi
                }
                val itemDate = runCatching { LocalDate.parse(item.date, formatter) }.getOrNull()
                val isToday = itemDate?.isEqual(today) == true

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isToday) textColor.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        // 👇 Reduced vertical padding from 6.dp to 2.dp
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isToday) "👉 $occasionDtl" else occasionDtl,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatDate(item.date),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }
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