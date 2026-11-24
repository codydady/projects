package com.sd.nithyadharma.cards

import LocaleManager
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Start
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.nithyadharma.model.HinduCalendarAttr.HinduCalendar
import com.sd.nithyadharma.model.HinduCalendarAttr.ayanamName
import com.sd.nithyadharma.model.HinduCalendarAttr.karanaName
import com.sd.nithyadharma.model.HinduCalendarAttr.maasamName
import com.sd.nithyadharma.model.HinduCalendarAttr.nakshatraName
import com.sd.nithyadharma.model.HinduCalendarAttr.pakshaName
import com.sd.nithyadharma.model.HinduCalendarAttr.rasiName
import com.sd.nithyadharma.model.HinduCalendarAttr.ruthuName
import com.sd.nithyadharma.model.HinduCalendarAttr.samvatsaraName
import com.sd.nithyadharma.model.HinduCalendarAttr.tithiName
import com.sd.nithyadharma.model.HinduCalendarAttr.vaaraName
import com.sd.nithyadharma.model.HinduCalendarAttr.yogaName
import com.sd.nithyadharma.model.TimeRange
import com.sd.nithyadharma.util.CommonFunctions
import com.sd.nithyadharma.util.LocalAppLanguage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PanchangamCardContent(
    hindCal: HinduCalendar?,
    textColor: Color
) {
    val currentLang = LocalAppLanguage.current

    if (hindCal == null) {
        Log.w("PanchangamLog", "Data incomplete -> dp is null: ${hindCal == null}")
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

    val samvatsara = hindCal.samvatsara?.let { samvatsaraName(it, currentLang) } ?: "—"
    val ayanam = hindCal.ayanam?.let { ayanamName(it, currentLang) } ?: "—"
    val ruthu = hindCal.ruthu?.let { ruthuName(it, currentLang) } ?: "—"
    val maasam = hindCal.maasam?.let { maasamName(it, currentLang) } ?: "—"
    val paksha = hindCal.paksha?.let { pakshaName(it, currentLang) } ?: "—"
    val vaara = hindCal.vaara?.let { vaaraName(it, currentLang) } ?: "—"
    val thithi = hindCal.thithi?.let { tithiName(it, currentLang) } ?: "—"
    val nakshatra = hindCal.nakshatra?.let { nakshatraName(it, currentLang) } ?: "—"
    val yoga = hindCal.yoga?.let { yogaName(it, currentLang) } ?: "—"
    val karana = hindCal.karana?.let { karanaName(it, currentLang) } ?: "—"
    val chandrashtamam = hindCal.chandrashtamaRasi?.let { rasiName(it, currentLang) } ?: "—"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. ELEGANT HERO PILL (Today + Vaara + Muhurtha) ---
        Surface(
            color = textColor.copy(alpha = 0.12f),
            shape = CircleShape
        ) {
            if (hindCal.muhurthaDay == true) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "✨ " + LocaleManager.getString("str_muhurtha", currentLang),
                        color = Color.Green,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } // row
        }

        // --- 3. DETAILS CARD ---
        Surface(
            color = textColor.copy(alpha = 0.08f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElegantDetailRow(
                    icon = Icons.Default.Grain,
                    label = LocaleManager.getString("str_samvatsara", currentLang),
                    value = samvatsara,
                    textColor = textColor
                )
                ElegantDetailRow(
                    icon = Icons.Default.AcUnit,
                    label = LocaleManager.getString("str_ayana", currentLang),
                    value = ayanam,
                    textColor = textColor
                )
                ElegantDetailRow(
                    icon = Icons.Default.Start,
                    label = LocaleManager.getString("str_ruthu", currentLang),
                    value = ruthu,
                    textColor = textColor
                )
                ElegantDetailRow(
                    icon = Icons.Default.CalendarMonth,
                    label = LocaleManager.getString("str_month", currentLang),
                    value = maasam,
                    textColor = textColor
                )
                ElegantDetailRow(
                    icon = Icons.Default.Brightness5,
                    label = LocaleManager.getString("str_pk", currentLang),
                    value = paksha,
                    textColor = textColor
                )
                ElegantDetailRow(
                    icon = Icons.Default.AutoAwesome,
                    label = LocaleManager.getString("str_nk", currentLang),
                    value = CommonFunctions.getPanchangaTimesString( nakshatra,
                                        hindCal.nakshatraEndTime!!, currentLang ),
                    textColor = textColor
                )

                ElegantDetailRow(
                    icon = Icons.Default.Brightness5,
                    label = LocaleManager.getString("str_tt", currentLang),
                    value = CommonFunctions.getPanchangaTimesString( thithi,
                        hindCal.thithiEndTime!!, currentLang ),
                    textColor = textColor
                )

                ElegantDetailRow(
                    icon = Icons.Default.SelfImprovement,
                    label = LocaleManager.getString("str_yg", currentLang),
                    value = CommonFunctions.getPanchangaTimesString( yoga,
                        hindCal.yogaEndTime!!, currentLang ),
                    textColor = textColor
                )

                ElegantDetailRow(
                    icon = Icons.Default.Grain,
                    label = LocaleManager.getString("str_kr", currentLang),
                    value = CommonFunctions.getPanchangaTimesString( karana,
                        hindCal.karanaEndTime!!, currentLang ),
                    textColor = textColor
                )

                ElegantDetailRow(
                    icon = Icons.Default.Shield,
                    label = LocaleManager.getString("str_cr", currentLang),
                    value = chandrashtamam,
                    textColor = textColor
                )
            }
        } //3. panchanga details end

        // --- 3. INAUSPICIOUS TIMINGS CAPSULES (Rahu / Yamagandam / Gulika) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TimingCapsule(
                label = LocaleManager.getString("str_rg", currentLang),
                value = hindCal.rahuKalam?.toDisplayString() ?: "—",
                textColor = textColor,
                modifier = Modifier.weight(1f)
            )
            TimingCapsule(
                label = LocaleManager.getString("str_ya", currentLang),
                value = hindCal.yamaGandam?.toDisplayString() ?: "—",
                textColor = textColor,
                modifier = Modifier.weight(1f)
            )
//            TimingCapsule(
//                label = LocaleManager.getString("str_gk", currentLang),
//                value = sp.gulikan?.toDisplayString() ?: "—",
//                textColor = textColor,
//                modifier = Modifier.weight(1f)
//            )
        }
    }
} //PanchangamCardContent ends

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

fun TimeRange.toDisplayString(): String {
    return "${start.format(timeFormatter)} - ${end.format(timeFormatter)}"
}

fun getAmPm(time: LocalDateTime): String {
    return if (time.hour < 12) "AM" else "PM"
}

// for empty card display string
fun formatMillisToHoursMins(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val mins = (millis / (1000 * 60)) % 60
    return String.format("%d:%02d hours", hours, mins)
}

@Composable
private fun ElegantDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top, // Align icon to top when text wraps to multiple lines
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icon aligned at the top of multi-line text
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(top = 2.dp) // Subtle alignment tweak with the first line of text
                .size(15.dp)
        )

        // Continuous Flowing Text
        Text(
            text = buildAnnotatedString {
                // Label (Medium weight, muted opacity)
                withStyle(
                    style = SpanStyle(
                        color = textColor.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Medium,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                ) {
                    append("$label: ")
                }

                // Value (Bold weight, full opacity)
                withStyle(
                    style = SpanStyle(
                        color = textColor.copy(alpha = 0.87f),
                        fontWeight = FontWeight.Medium,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                ) {
                    append(value)
                }
            },
            modifier = Modifier.weight(1f),
            lineHeight = 16.sp // Gives comfortable spacing when text wraps into 2 lines
        )
    }
}

@Composable
private fun TimingCapsule(
    label: String,
    value: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = textColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}
