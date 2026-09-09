package com.sd.nithyadharma.cards

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.LocalAppLanguage

@Composable
fun PanchangamNotificationCardContent(
    paramsMap: Map<String, JsonElement>,
    textColor: Color,
    icon: ImageVector = Icons.Outlined.Notifications
) {
    Log.d(
        "DPNotificationCard",
        "NotificationCardContent Dynamic Panchangam onEach adding notification"
    )

    val currentLang = LocalAppLanguage.current

    // Extract parameters
    val title = paramsMap["title"]?.jsonPrimitive?.contentOrNull ?: "Notification"
    val untilTime = paramsMap["untilTime"]?.jsonPrimitive?.contentOrNull
    val score = paramsMap["score"]?.jsonPrimitive?.intOrNull ?: 0
    // Compute dynamic color & localized label based on score
    val scoreInfo = getScoreLabelColor(score, currentLang)

    val rawMessage = paramsMap["message"]?.jsonPrimitive?.contentOrNull ?: ""

    val displayMessage = if (rawMessage.contains("|")) { // for panchanga transition
        val (oldKey, newKey) = rawMessage.split("|")
        val oldText = LocaleManager.getString(oldKey.trim(), currentLang)
        val newText = LocaleManager.getString(newKey.trim(), currentLang)
        "$oldText ➜ $newText"
    }
    else {      // for chandrashtama or other features
        LocaleManager.getString(rawMessage.trim(), currentLang)
    }

    // Two-column layout:
    // Column 1 = title, message, until time
    // Column 2 = score
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ---------------------------------------------------------
        // COLUMN 1: Icon + Title, Message, Until Time
        // ---------------------------------------------------------
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {

            // Title row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = textColor.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = LocaleManager.getString(title, currentLang) ,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Message
            Text(
                text = displayMessage,// was LocaleManager.getString(message, currentLang),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor.copy(alpha = 0.9f),
                    lineHeight = 18.sp,
                    fontSize = 13.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Until time
            if (!untilTime.isNullOrBlank()) {
                Text(
                    text = LocaleManager.getString("str_until", currentLang) + " $untilTime",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // ---------------------------------------------------------
        // COLUMN 2: Dynamic Colored Score Canvas
        // ---------------------------------------------------------
        Box(
            modifier = Modifier.size(80.dp)
                    .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            ) {
                val strokeWidth = 6.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f

                drawArc(
                    color = scoreInfo.color,
                    startAngle = -90f,
                    sweepAngle = (score / 100f) * 360f,
                    useCenter = false,
                    topLeft = Offset(
                        strokeWidth / 2,
                        strokeWidth / 2
                    ),
                    size = Size(
                        radius * 2,
                        radius * 2
                    ),
                    style = Stroke(
                        strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }

            Text(
                text = score.toString(),
                fontWeight = FontWeight.Bold,
                color = scoreInfo.color,
                style = MaterialTheme.typography.titleLarge
//                lineHeight = 12.sp
            )
        }
    }
}

// aint the right place but as a small scope helper item , its okay
data class ScoreInfo(
    val label: String,
    val color: Color
)

fun getScoreLabelColor(
    score: Int,
    currentLang: NDLanguage
): ScoreInfo {

    val scoreColor = when {
        score >= 75 -> Color(0xFF4CAF50) // Green
        score >= 50 -> Color(0xFFCDDC39) // Orange
        score >= 25 -> Color(0xFF2196F3) // Red
        else -> Color(0xFFFF5722) // Dark brown
    }

    val scoreLabel = when {
        score >= 75 -> LocaleManager.getString("str_ex", currentLang)
        score >= 50 -> LocaleManager.getString("str_gd", currentLang)
        score >= 25 -> LocaleManager.getString("str_av", currentLang)
        else -> LocaleManager.getString("str_pr", currentLang)
    }

    return ScoreInfo(
        label = scoreLabel,
        color = scoreColor
    )
}