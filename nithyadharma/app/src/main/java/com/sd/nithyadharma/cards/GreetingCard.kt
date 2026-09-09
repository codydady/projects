package com.sd.nithyadharma.cards

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sd.nithyadharma.util.*
import com.sd.nithyadharma.util.LocalAppLanguage
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun GreetingCardContent(
    paramsMap: Map<String, JsonElement>,
    textColor: Color
) {
    val currentLang = LocalAppLanguage.current

    Log.d("GreetingCard", "Greeting card params map: $paramsMap")

    // Extract primitive values keyed on paramsMap references
    val name = remember(paramsMap["name"]) {
        paramsMap["name"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }
    val greeting = remember(paramsMap["greeting"]) {
        paramsMap["greeting"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }
    val motto = remember(paramsMap["motto"]) {
        paramsMap["motto"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        // -------------------------------------------------------------
        // 1. Top Section: Greeting & Recipient Name
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (greeting.isNotEmpty()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            MaterialTheme.typography.bodyLarge.toSpanStyle().copy(
                                color = textColor.copy(alpha = 0.75f),
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append(LocaleManager.getString(greeting, currentLang))
                        }
                        append("  ")
                        withStyle(
                            MaterialTheme.typography.titleLarge.toSpanStyle().copy(
                                color = Constants.DarkIvory,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
                                )
                        ) {
                            append(name)
                        }
                        append("\n")
                        withStyle(
                            MaterialTheme.typography.bodyLarge.toSpanStyle().copy(
                                color = textColor.copy(alpha = 0.75f),
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append(LocaleManager.getString("start_slot", currentLang))
                        }
                    },
//                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // -------------------------------------------------------------
        // 3. Middle/Bottom Section: Custom Motto / Quote
        // -------------------------------------------------------------
        if (motto.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = LocaleManager.getString(motto, currentLang),
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}