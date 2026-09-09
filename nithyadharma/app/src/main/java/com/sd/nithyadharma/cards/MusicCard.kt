package com.sd.nithyadharma.cards

import LocaleManager
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.nithyadharma.model.Audio_Files
import com.sd.nithyadharma.util.LocalAppLanguage
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.concurrent.TimeUnit

@Composable
fun MusicCardContent(
    paramsMap: Map<String, JsonElement>,
    textColor: Color
) {
    val context = LocalContext.current
    val currentLang = LocalAppLanguage.current

    Log.d("MusicCard", "new song it seems , paramsmap is ${paramsMap}")

    // Extract primitive audioKey first to prevent crash when clicking a different card
    val audioKey = paramsMap["audioKey"]?.jsonPrimitive?.contentOrNull

    // Extract title, audioResId, and imageResId via Triple
    val (title, audioResId, imageResId) = remember(audioKey, currentLang) {
        requireNotNull(audioKey) { "audioKey is required" }

        val audio = Audio_Files.valueOf(audioKey)
        val localKey = "audio_" + audioKey.lowercase()

        Log.d("MusicCard", "derived audio key is $localKey")

        val title = LocaleManager.getString(localKey, currentLang)
        Triple(title, audio.resId, audio.imageResId)
    }

    // -------------------------------------------------------------
    // 1. MediaPlayer & State Initialization
    // -------------------------------------------------------------
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var totalDurationMs by remember { mutableIntStateOf(1) } // Avoid divide-by-zero
    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }

    val mediaPlayer = remember {
        MediaPlayer.create(context, audioResId)?.apply {
            setOnCompletionListener {
                isPlaying = false
                seekTo(0)
                currentPositionMs = 0
            }
        }
    }

    LaunchedEffect(mediaPlayer) {
        mediaPlayer?.let {
            if (it.duration > 0) {
                totalDurationMs = it.duration
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    // Polling loop for playback progress tracking
    LaunchedEffect(isPlaying, isUserScrubbing) {
        while (isPlaying && !isUserScrubbing) {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    currentPositionMs = it.currentPosition
                }
            }
            delay(250)
        }
    }

    // -------------------------------------------------------------
    // 2. Main Horizontal Layout
    // -------------------------------------------------------------
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 0.dp, end = 6.dp, top = 4.dp, bottom = 4.dp), // Zero start padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        // -------------------------------------------------------------
        // LHS: Song Image (~15% larger width, tight left margin)
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .weight(0.35f) // Increased allocation from 0.35f to 0.40f (~15% larger)
                .fillMaxHeight()
                .padding(end = 8.dp), // Reduced spacing moves RHS closer left
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(0.9f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        }

        // -------------------------------------------------------------
        // RHS: Song Info, Controls & Slider (Moved left to use width)
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .weight(0.60f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Row 1: Song Name ---
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = textColor.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // --- Row 2: Play Button + Pulse Equalizer ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                PlayPauseButton(
                    isPlaying = isPlaying,
                    textColor = textColor,
                    onClick = {
                        mediaPlayer?.let { player ->
                            if (player.isPlaying) {
                                player.pause()
                                isPlaying = false
                            } else {
                                player.start()
                                isPlaying = true
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(10.dp))

                EnhancedRhythmicVisualizer(
                    isPlaying = isPlaying,
                    textColor = textColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp)) // Vertical gap before slider

            // --- Row 3: Isolated Slider & Timers ---
            MusicSliderSection(
                currentPositionMsProvider = { currentPositionMs },
                totalDurationMs = totalDurationMs,
                isUserScrubbing = isUserScrubbing,
                scrubProgress = scrubProgress,
                textColor = textColor,
                onScrubbingChange = { scrubbing, progress ->
                    isUserScrubbing = scrubbing
                    scrubProgress = progress
                },
                onSeekTo = { targetMs ->
                    mediaPlayer?.seekTo(targetMs)
                    currentPositionMs = targetMs
                }
            )
        }
    }
}

// -------------------------------------------------------------
// Helper Component: Compact Play/Pause Button
// -------------------------------------------------------------
@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    textColor: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PlayButtonPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ButtonPulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(46.dp)
    ) {
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.12f))
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(textColor.copy(alpha = 0.30f)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Helper Component: Isolated Slider Section (Stops Parent Recomposition)
// -------------------------------------------------------------
@Composable
private fun MusicSliderSection(
    currentPositionMsProvider: () -> Int,
    totalDurationMs: Int,
    isUserScrubbing: Boolean,
    scrubProgress: Float,
    textColor: Color,
    onScrubbingChange: (Boolean, Float) -> Unit,
    onSeekTo: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val currentPositionMs = currentPositionMsProvider()
        val effectiveProgress = if (isUserScrubbing) scrubProgress else (currentPositionMs.toFloat() / totalDurationMs)

        Slider(
            value = effectiveProgress.coerceIn(0f, 1f),
            onValueChange = { fraction ->
                onScrubbingChange(true, fraction)
            },
            onValueChangeFinished = {
                val targetMs = (scrubProgress * totalDurationMs).toInt()
                onSeekTo(targetMs)
                onScrubbingChange(false, scrubProgress)
            },
            colors = SliderDefaults.colors(
                thumbColor = textColor.copy(alpha = 0.60f),
                activeTrackColor = textColor.copy(alpha = 0.50f),
                inactiveTrackColor = textColor.copy(alpha = 0.10f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayPosition = if (isUserScrubbing) (scrubProgress * totalDurationMs).toLong() else currentPositionMs.toLong()

            Text(
                text = formatDuration(displayPosition),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.80f)
            )

            Text(
                text = formatDuration(totalDurationMs.toLong()),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.80f)
            )
        }
    }
}

// -------------------------------------------------------------
// Helper Component: Modern Waveform Equalizer (GPU Layer Scaled)
// -------------------------------------------------------------
@Composable
private fun EnhancedRhythmicVisualizer(
    isPlaying: Boolean,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val barCount = 10
    val transition = rememberInfiniteTransition(label = "WaveformTransition")

    Row(
        modifier = modifier.height(26.dp),
        horizontalArrangement = Arrangement.spacedBy(3.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            val duration = remember(index) { 300 + (index * 60) % 400 }
            val easing: Easing = remember(index) { FastOutSlowInEasing }

            val heightFraction by transition.animateFloat(
                initialValue = 0.15f,
                targetValue = if (isPlaying) 1.0f else 0.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = easing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Bar_$index"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleY = if (isPlaying) heightFraction else 0.15f
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    }
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isPlaying) textColor.copy(alpha = 0.35f)
                        else textColor.copy(alpha = 0.12f)
                    )
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}