package com.sd.nithyadharma.cards

import LocaleManager
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.nithyadharma.model.HoroscopeAttr
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.model.PanchangaAttr.rasiName
import com.sd.nithyadharma.util.DailyRasiPalanReport
import com.sd.nithyadharma.util.LocalAppLanguage
import com.sd.nithyadharma.util.OutcomeResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun RasiPalanCardContent(
    paramsMap: Map<String, JsonElement>,
    textColor: Color
) {
    val currentLang = LocalAppLanguage.current

    val report: DailyRasiPalanReport? = paramsMap["rasi_palan_data"]
        ?.jsonPrimitive
        ?.content
        ?.let { rawJsonString ->
            runCatching { Json.decodeFromString<DailyRasiPalanReport>(rawJsonString) }.getOrNull()
        }

    if (report == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = textColor, strokeWidth = 2.dp)
        }
        return
    }

    val userRasiDisplayName = rasiName(report.userRasi, currentLang)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. HERO HEADER PILL (User Rasi & Overall Rating) ---
        Surface(
            color = textColor.copy(alpha = 0.12f),
            shape = CircleShape
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = userRasiDisplayName,
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "•",
                    color = textColor.copy(alpha = 0.5f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★ ${String.format("%.1f", report.overallDayRating)} / 5.0",
                        color = Color(0xFFFFB300), // Star Gold Accent
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- 2. CHANDRASHTAMA WARNING BANNER (Shows only during Chandrashtama) ---
        if (report.isChandrashtama) {
            Surface(
                color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Chandrashtama Active — Caution Required Today",
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- 3. OUTCOMES BREAKDOWN CARD ---
        Surface(
            color = textColor.copy(alpha = 0.08f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                report.outcomes.forEach { outcome ->
                    OutcomeRow(
                        outcome = outcome,
                        currentLang = currentLang,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun OutcomeRow(
    outcome: OutcomeResult,
    currentLang: NDLanguage,
    textColor: Color
) {
    val planetDisplayName = HoroscopeAttr.planetName(outcome.leadingPlanet, currentLang)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Line 1: Category Title
        Text(
            text = LocaleManager.getString(outcome.category.title, currentLang),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor.copy(alpha = 0.9f)
        )

        // Line 2: Planet + House Badge & Star Rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Planet Name - H<num>
            Text(
                text = "($planetDisplayName - H${outcome.transitHouseFromUser})",
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.55f)
            )

            // Visual Star Rating Display
            Row(verticalAlignment = Alignment.CenterVertically) {
                val fullStars = outcome.starRating.toInt()
                val hasHalfStar = (outcome.starRating % 1) >= 0.5

                repeat(fullStars) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(13.dp)
                    )
                }
                if (hasHalfStar) {
                    Icon(
                        imageVector = Icons.Default.StarHalf,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Line 3: Summary Text
//        Log.d("Rasipalancardcontent", "outcome.summary is ${outcome.summary}")

        Text(
            text = LocaleManager.getString(outcome.summary, currentLang),
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.75f),
            lineHeight = 15.sp
        )
    }
}