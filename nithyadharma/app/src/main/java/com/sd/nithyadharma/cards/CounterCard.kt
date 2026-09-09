package com.sd.nithyadharma.cards

import LocaleManager
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.nithyadharma.R
import com.sd.nithyadharma.util.LocalAppLanguage
import com.sd.nithyadharma.util.SoundManager
import com.sd.nithyadharma.util.TTSManager
import kotlin.random.Random
import androidx.compose.foundation.shape.RoundedCornerShape

private const val TAG = "CounterCard"

// 🔑 Android App Icon Squircle Shape (22% / 22.dp rounded continuous corners)
val AndroidAppIconShape = RoundedCornerShape(22.dp)

@Composable
fun CounterCardContent(
    textColor: Color,
    count: Int,
    alertInterval: Int,
    finalCount: Int,
    onCountChanged: (Int) -> Unit,
    onSettingsChanged: (interval: Int, target: Int) -> Unit,
    //@DrawableRes imageResId: Int? = null // Pass your drawable resource ID here
) {
    val currentLang = LocalAppLanguage.current
    val context = LocalContext.current

    val soundManager = remember { SoundManager.getInstance(context) }

    @DrawableRes val imageResId = R.drawable.nd_munivar

    DisposableEffect(soundManager) {
        onDispose {
            soundManager.release()
        }
    }

    var isEditing by remember { mutableStateOf(false) }
    var tempProgressInput by remember { mutableStateOf("") }
    var tempTargetInput by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf(false) }

    // Increment, Audio, & TTS handler
    val handleIncrement = {
        val incrementedCount = count + 1
        onCountChanged(incrementedCount)

        soundManager.playKuduk()

        if (incrementedCount % alertInterval == 0) {
            TTSManager.speakNumber(incrementedCount)
        }
        if (incrementedCount == finalCount) {
            TTSManager.speak("You have completed $incrementedCount")
            Log.d(TAG, "Final count reached: $count")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // -------------------------------------------------------------
        // 1. Main Tappable Area (Side-by-Side: Count Left, Pic Right)
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxHeight(0.75f)
                .fillMaxWidth()
                .clickable { handleIncrement() }, // Tapping anywhere increments
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Motto at the top
                Text(
                    text = LocaleManager.getString("ct_motto", currentLang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Side-by-side row: Count on Left, Photo on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Count shifted to the left
                    Text(
                        text = "$count",
//                        fontSize = if (imageResId != null) 80.sp else 90.sp, // Slightly auto-adjusted if image is present
                        fontSize = 80.sp, // Slightly auto-adjusted if image is present
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    )

                    // Photo positioned to the right with grunge border
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(AndroidAppIconShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = "Card Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
//        Spacer(modifier = Modifier.width(20.dp))

        // -------------------------------------------------------------
        // 2. Integrated Bottom Section
        // -------------------------------------------------------------
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = textColor.copy(alpha = 0.2f)
        )

        if (isEditing) {
            Text(
                text = if (validationError) "Step must be strictly less than target!" else "Step must be smaller than target",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (validationError) Color.Red else textColor.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 2.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isEditing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(0.72f)
                ) {
                    OutlinedTextField(
                        value = tempProgressInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                tempProgressInput = input
                                validationError = false
                            }
                        },
                        singleLine = true,
                        textStyle = TextStyle(color = textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = textColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                            cursorColor = textColor
                        ),
                        placeholder = { Text("Step", color = textColor.copy(alpha = 0.4f), style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.width(68.dp)
                    )

                    OutlinedTextField(
                        value = tempTargetInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                tempTargetInput = input
                                validationError = false
                            }
                        },
                        singleLine = true,
                        textStyle = TextStyle(color = textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = textColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                            cursorColor = textColor
                        ),
                        placeholder = { Text("Target", color = textColor.copy(alpha = 0.4f), style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.width(68.dp)
                    )

                    IconButton(
                        onClick = {
                            var parsedProgress = tempProgressInput.toIntOrNull() ?: 18
                            var parsedTarget = tempTargetInput.toIntOrNull() ?: 36

                            if (parsedProgress <= 0) parsedProgress = 18
                            if (parsedTarget <= 0) parsedTarget = 36

                            if (parsedProgress >= parsedTarget) {
                                validationError = true
                                parsedProgress = 18
                                parsedTarget = 36
                            }
                            onSettingsChanged(parsedProgress, parsedTarget)

                            isEditing = false
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Target",
                            tint = textColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(0.70f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "${LocaleManager.getString("ct_inter", currentLang)} : $alertInterval",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "${LocaleManager.getString("ct_target", currentLang)} : $finalCount",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor.copy(alpha = 0.9f)
                        )
                    }

                    IconButton(
                        onClick = {
                            tempProgressInput = alertInterval.toString()
                            tempTargetInput = finalCount.toString()
                            validationError = false
                            isEditing = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Progress and Target",
                            tint = textColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            TextButton(
                onClick = {
                    onCountChanged(0)
                    Log.d(TAG, "Counter reset to 0.")
                },
                modifier = Modifier.weight(0.30f)
            ) {
                Text(
                    text = LocaleManager.getString("ct_reset", currentLang),
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}