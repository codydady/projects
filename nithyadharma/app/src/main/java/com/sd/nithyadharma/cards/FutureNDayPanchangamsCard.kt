package com.sd.nithyadharma.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.model.PanchangaAttr
import com.sd.nithyadharma.model.PanchangaAttr.DynamicPanchangam
import com.sd.nithyadharma.model.PanchangaAttr.StaticPanchangam
import com.sd.nithyadharma.model.PanchangaAttr.maasamName
import com.sd.nithyadharma.model.PanchangaAttr.nakshatraName
import com.sd.nithyadharma.model.PanchangaAttr.rasiName
import com.sd.nithyadharma.model.PanchangaAttr.tithiName
import com.sd.nithyadharma.model.PanchangaAttr.vaaraName
import com.sd.nithyadharma.util.CommonFunctions
import com.sd.nithyadharma.util.LocalAppLanguage
import com.sd.nithyadharma.util.LocaleOther
import java.time.format.DateTimeFormatter

/**
 * Renders the forecast content for the upcoming N days Panchangam.
 * Takes List<Pair<StaticPanchangam, DynamicPanchangam>> as primary input.
 */
@Composable
fun FuturePanchangamContent(
    nextNDaysDp: List<Pair<StaticPanchangam, DynamicPanchangam>>?,
    textColor: Color
) {
    val currentLang = LocalAppLanguage.current

    if (nextNDaysDp == null) return

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        nextNDaysDp.forEach { (sp, dp) ->
            FuturePanchangamItemCard(
                sp = sp,
                dp = dp,
                currentLang = currentLang,
                textColor = textColor
            )
        }
    }
}

/**
 * Renders an individual expandable card for a specific day using the theme-adaptive Surface style.
 */
@Composable
private fun FuturePanchangamItemCard(
    sp: StaticPanchangam,
    dp: DynamicPanchangam,
    currentLang: NDLanguage,
    textColor: Color
) {
    val now = remember { CommonFunctions.getCurrentTime() }
    val timeformatter = remember { DateTimeFormatter.ofPattern("h:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM") }

    var expanded by remember { mutableStateOf(false) }

    val maasam = dp.maasam?.let { maasamName(it, currentLang) } ?: "Maasam: Calculating..."
    val vaara = dp.vaara?.let { vaaraName(it, currentLang) } ?: "Vaara: Calculating..."
    val thithi = dp.thithi?.let { tithiName(it, currentLang) } ?: "Tithi: Calculating..."
    val nakshatra = dp.nakshatra?.let { nakshatraName(it, currentLang) } ?: "Nakshatra: Calculating..."

    val thithiNext = with(PanchangaAttr) {
        dp.thithi?.next()?.let { tithiName(it, currentLang) } ?: "Tithi: Calculating..."
    }
    val nakshatraNext = with(PanchangaAttr) {
        dp.nakshatra?.next()?.let { nakshatraName(it, currentLang) } ?: "Nakshatra: Calculating..."
    }

    Surface(
        color = textColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row (Clickable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$vaara • ${dp.calcDttm.toLocalDate().format(dateFormatter)} • ${dp.score}",
                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.SemiBold,
                    color = if (dp.muhurthaDay == true) Color.Green else textColor
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = textColor
                )
            }

            // Expandable Details Section
            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = textColor.copy(alpha = 0.15f))

                    FutureTimingRow(
                        label = LocaleManager.getString("str_month", currentLang),
                        value = maasam,
                        textColor = textColor
                    )

                    FutureTimingRow(
                        label = LocaleManager.getString("str_tt", currentLang),
                        value = LocaleManager.getString(
                            "str_fut_timeend", currentLang, thithi,
                            LocaleOther.getTimeSlotLocalized(dp.thithiEndTime ?: now, currentLang),
                            dp.thithiEndTime?.format(timeformatter) ?: "—",
                            getAmPm(dp.thithiEndTime ?: now),
                            thithiNext
                        ),
                        textColor = textColor
                    )

                    FutureTimingRow(
                        label = LocaleManager.getString("str_nk", currentLang),
                        value = LocaleManager.getString(
                            "str_fut_timeend", currentLang, nakshatra,
                            LocaleOther.getTimeSlotLocalized(dp.nakshatraEndTime ?: now, currentLang),
                            dp.nakshatraEndTime?.format(timeformatter) ?: "—",
                            getAmPm(dp.nakshatraEndTime ?: now),
                            nakshatraNext
                        ),
                        textColor = textColor
                    )

                    FutureTimingRow(
                        label = LocaleManager.getString("str_cr", currentLang),
                        value = dp.chandrashtamaRasi?.let { rasiName(it, currentLang) } ?: "—",
                        textColor = textColor
                    )

                    if (dp.muhurthaDay == true) {
                        Text(
                            text = LocaleManager.getString("str_muhurtha", currentLang),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Green
                        )
                    }

                    HorizontalDivider(color = textColor.copy(alpha = 0.15f))

                    FutureTimingRow(
                        label = LocaleManager.getString("str_rg", currentLang),
                        value = sp.rahuKalam?.toDisplayString() ?: "—",
                        textColor = textColor
                    )

                    FutureTimingRow(
                        label = LocaleManager.getString("str_ya", currentLang),
                        value = sp.yamaGandam?.toDisplayString() ?: "—",
                        textColor = textColor
                    )

                    HorizontalDivider(color = textColor.copy(alpha = 0.15f))

                    FutureTimingRow(
                        label = LocaleManager.getString("str_sc", currentLang),
                        value = dp.score.toString(),
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun FutureTimingRow(
    label: String,
    value: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$label – ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = textColor.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}