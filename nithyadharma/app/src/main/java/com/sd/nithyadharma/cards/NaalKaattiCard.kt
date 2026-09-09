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
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sd.nithyadharma.model.PanchangaAttr.StaticPanchangam
import com.sd.nithyadharma.model.PanchangaAttr.vaaraName
import com.sd.nithyadharma.util.LocalAppLanguage
import java.time.format.DateTimeFormatter

@Composable
fun NaalKaattiCardContent(
    sp: StaticPanchangam?,
    textColor: Color
) {
    val currentLang = LocalAppLanguage.current

    if (sp == null) {
        Log.w("NaalKaattiCardContent", "Data incomplete -> sp is null: ${sp == null}")
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
//    Log.i("NaalKaattiCardContent", "tryin to make one ")

    val timeformatter = DateTimeFormatter.ofPattern("h:mm a")
    val vaara = sp.vaara?.let { vaaraName(it, currentLang) } ?: "—"

    Column(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. ELEGANT HERO PILL (Today + Vaara) ---
        Surface(
            color = textColor.copy(alpha = 0.12f),
            shape = CircleShape
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${LocaleManager.getString("str_today", currentLang)} • $vaara",
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // --- 2. SINGLE CONTAINER FOR SOLAR & TIMINGS ---
        Surface(
            color = textColor.copy(alpha = 0.08f),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // 1. Sunrise (Suryoday)
                TimingRowContent(
                    label = LocaleManager.getString("str_sunrisefull", currentLang),
                    value = sp.sunrise?.format(timeformatter) ?: "—",
                    icon = Icons.Default.WbSunny,
                    textColor = textColor
                )

                HorizontalDivider(color = textColor.copy(alpha = 0.08f), thickness = 1.dp)

                // 2. Sunset (Asthamana)
                TimingRowContent(
                    label = LocaleManager.getString("str_sunsetfull", currentLang),
                    value = sp.sunset?.format(timeformatter) ?: "—",
                    icon = Icons.Default.NightsStay,
                    textColor = textColor
                )

                HorizontalDivider(color = textColor.copy(alpha = 0.08f), thickness = 1.dp)

                // 3. Rahu Kalam
                TimingRowContent(
                    label = LocaleManager.getString("str_rg", currentLang),
                    value = sp.rahuKalam?.toDisplayString() ?: "—",
                    textColor = textColor
                )

                HorizontalDivider(color = textColor.copy(alpha = 0.08f), thickness = 1.dp)

                // 4. Yamagandam
                TimingRowContent(
                    label = LocaleManager.getString("str_ya", currentLang),
                    value = sp.yamaGandam?.toDisplayString() ?: "—",
                    textColor = textColor
                )

                HorizontalDivider(color = textColor.copy(alpha = 0.08f), thickness = 1.dp)

                // 5. Gulikan
                TimingRowContent(
                    label = LocaleManager.getString("str_gk", currentLang),
                    value = sp.gulikan?.toDisplayString() ?: "—",
                    textColor = textColor
                )
            }
        }
    }
}

// 🟢 Reusable row item inside the unified container
@Composable
private fun TimingRowContent(
    label: String,
    value: String,
    textColor: Color,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.85f)
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}