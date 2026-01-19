package com.sd.nithyadharma.screen

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sd.nithyadharma.util.PreferencesManager
import com.sd.nithyadharma.model.BreathingTimings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.sin
import kotlin.math.exp
import kotlin.math.PI
import kotlinx.coroutines.isActive
import kotlinx.coroutines.CancellationException

// Define the Dark Green color globally
val DarkGreen = Color(0xFF0A6847)

// --- 1. Data Classes ---

data class Phase(
    val name: String,
    val durationSec: Float,
    val color: Color,
    val text: String
)

data class BreathingState(
    val phaseIndex: Int = 0,
    val elapsedInPhase: Float = 0f,
    val totalTimeInCycle: Float = 0f,
    val totalCycleDuration: Float = 0f,
    val isRunning: Boolean = false
)

// -----------------------------------------------------------------------------
// --- 2. Audio Synthesis Logic (Option 3: Timer-Driven Looping) ---
// -----------------------------------------------------------------------------

const val SAMPLE_RATE = 44100
const val VOLUME_SCALE = 0.3f
// 🔥 Option 3 Constants
const val LOOP_SOUND_DURATION_SEC = 3.0f
const val LOOP_SOUND_DELAY_MS = 10L // Small gap between clips for less CPU load
const val DECAY_TIME_LENGTH = 0.5

private val FREQUENCY_MAP = mapOf(
    "INHALE" to 440.0,
    "HOLD" to 554.37,
    "EXHALE" to 392.00,
    "PAUSE" to 659.25
)

// Global reference to track and stop current AudioTrack
private val audioTrackRef = AtomicReference<AudioTrack?>(null)
// 🔥 NEW: Global reference for the sound loop Job
private val audioLoopJobRef = AtomicReference<Job?>(null)

/**
 * Stops the currently playing tone immediately and releases resources.
 */
private fun stopCurrentTone() {
    audioLoopJobRef.getAndSet(null)?.cancel() // Cancel the looping job first
    audioTrackRef.getAndSet(null)?.let { track ->
        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
            track.release()
        } catch (e: Exception) {
            Log.e("Audio", "Error stopping tone", e)
        }
    }
}

/**
 * Pre-initializes audio system for lower latency (call once per app/activity).
 */
private fun preInitAudio(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.mode = AudioManager.MODE_NORMAL
}

/**
 * Generates and plays a tone using a continuous loop of a short, pre-generated buffer (Option 3).
 * This ensures instant start, regardless of phase duration.
 */
/**
 * Generates a seamless 3-second audio loop and streams it continuously (Hybrid Streaming).
 * This eliminates the annoying "beat" artifact while maintaining instant start.
 */
/**
 * Generates a seamless 3-second audio loop and streams it continuously (Hybrid Streaming).
 * This eliminates the annoying "beat" artifact while maintaining instant start and zero-crossing.
 */
fun createAndPlayDecayingTone(
    scope: CoroutineScope,
    phaseName: String,
    durationSec: Float,
    volume: Float = VOLUME_SCALE
) {
    stopCurrentTone()

    val frequency = FREQUENCY_MAP[phaseName] ?: 440.0
    // 3.0 seconds of audio buffer to hide repetition
    val numSamples = (LOOP_SOUND_DURATION_SEC * SAMPLE_RATE).toInt()

    // 1. Generate the single, seamless, reusable 3-second buffer
    val generatedSound = ShortArray(numSamples)

    // Constants for envelope calculation
    val loopDuration = LOOP_SOUND_DURATION_SEC.toDouble() // 3.0 seconds
    val startFadeTime = 0.5 // Initial decay for the 'chime' effect
    val endFadeDuration = 0.05 // Fade out over the last 50ms for seamless looping

    for (i in 0 until numSamples) {
        val t = i.toDouble() / SAMPLE_RATE
        val wave = (0.50 * sin(2 * PI * frequency * t) + 0.30 * sin(2 * PI * (frequency * 1.4) * t))

        // --- 1. Initial Decay (Chime Start) ---
        // Applies a strong decay for the first 0.5s, then holds volume at 1.0
        val startEnvelope: Double = if (t <= startFadeTime) exp(-t / (startFadeTime / 3.0)) else 1.0

        // --- 2. Final Decay (Zero-Crossing Loop Fix) ---
        // Fades out the last 50ms to ensure the waveform is at zero at the loop point.
        var endEnvelope = 1.0
        if (t > loopDuration - endFadeDuration) {
            // Calculate the ratio: 0.0 at fade start, 1.0 at fade end (loop point)
            val fadeRatio = (t - (loopDuration - endFadeDuration)) / endFadeDuration
            // We want 1.0 at fade start and 0.0 at fade end, so use (1.0 - fadeRatio)
            endEnvelope = 1.0 - fadeRatio
        }

        // Combine the initial chime envelope and the final loop-fixing envelope
        val finalEnvelope = startEnvelope * endEnvelope

        val sample = (wave * finalEnvelope * volume * Short.MAX_VALUE).toInt()
        generatedSound[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }

    // 2. Setup AudioTrack for streaming
    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    val audioFormat = AudioFormat.Builder()
        .setSampleRate(SAMPLE_RATE)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .build()

    // Use minimum buffer size for stream mode
    val minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)

    val audioTrack = AudioTrack(
        audioAttributes,
        audioFormat,
        minBufferSize,
        AudioTrack.MODE_STREAM, // Must be STREAM for continuous writing
        AudioManager.AUDIO_SESSION_ID_GENERATE
    )
    audioTrackRef.set(audioTrack)

    // 3. Launch the looping job to continuously feed the stream
    val job = scope.launch(Dispatchers.Default) {
        try {
            audioTrack.play() // Start the stream

            while (isActive) {
                // Write the full 3-second buffer to the stream. The OS manages the seamless loop.
                audioTrack.write(generatedSound, 0, numSamples)

                // Yield the CPU slightly
                delay(LOOP_SOUND_DELAY_MS)
            }
        } catch (e: CancellationException) {
            // Expected during stop
        } catch (e: Exception) {
            Log.e("Audio", "Playback error for $phaseName", e)
        }
    }
    audioLoopJobRef.set(job)
}

// -----------------------------------------------------------------------------
// --- 4. Keep Screen On Composable (FIXED: Using Window Flag) ---
// -----------------------------------------------------------------------------

/**
 * Manages the Window Flag to reliably keep the screen on.
 */
@Composable
fun KeepScreenOn(keepOn: Boolean) {
    val context = LocalContext.current

    // Helper to find the Activity's Window
    val window = remember(context) {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return@remember currentContext.window
            }
            currentContext = currentContext.baseContext
        }
        null
    }

    DisposableEffect(keepOn, window) {
        if (window != null) {
            if (keepOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        onDispose {
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// --- 3. The Meditate Screen Composable (Fully Integrated) ---
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditateScreen(
    preferencesManager : PreferencesManager,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Pre-init audio once on screen entry
    LaunchedEffect(Unit) {
        preInitAudio(context)
    }

    val preferencesManager = remember { PreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val timings: State<BreathingTimings> = preferencesManager.getBreathingTimings().collectAsState(
        initial = BreathingTimings(6f, 10.5f, 12f, 0.01f)
    )

    val totalCycleDuration: Float = remember(timings.value) {
        timings.value.inhale + timings.value.hold + timings.value.exhale + timings.value.pause
    }

    val phases = remember(timings.value) {
        val t = timings.value
        listOf(
            Phase("INHALE", t.inhale, Color.Blue, "Inhale"),
            Phase("HOLD", t.hold, DarkGreen, "Hold (Kumbhaka)"),
            Phase("EXHALE", t.exhale, Color.Red, "Exhale"),
            Phase("PAUSE", t.pause, Color.Gray, "Hold (Rechaka)")
        )
    }

    var state by remember {
        mutableStateOf(BreathingState(totalCycleDuration = totalCycleDuration, isRunning = false))
    }

    var lastPhaseIndexForSound by remember { mutableIntStateOf(-1) }

    // Keep screen on during run
    KeepScreenOn(keepOn = state.isRunning)

    // Main timer & phase logic with audio triggers
    LaunchedEffect(state.isRunning, phases) {
        if (!state.isRunning) {
            stopCurrentTone()
            lastPhaseIndexForSound = -1
            return@LaunchedEffect
        }

        // Trigger initial sound if new start/resume
        if (state.phaseIndex != lastPhaseIndexForSound) {
            createAndPlayDecayingTone(
                scope = coroutineScope, // Correct call signature
                phaseName = phases[state.phaseIndex].name,
                durationSec = phases[state.phaseIndex].durationSec
            )
            lastPhaseIndexForSound = state.phaseIndex
        }

        var lastUpdateTime = System.currentTimeMillis()

        try {
            while (state.isRunning) {
                val now = System.currentTimeMillis()
                val deltaTime = (now - lastUpdateTime) / 1000f
                lastUpdateTime = now

                val currentPhase = phases[state.phaseIndex]
                var newElapsedInPhase = state.elapsedInPhase + deltaTime

                // Phase transition check
                if (newElapsedInPhase >= currentPhase.durationSec) {
                    val timeOvershoot = newElapsedInPhase - currentPhase.durationSec
                    val nextIndex = (state.phaseIndex + 1) % phases.size

                    // Immediate sound for next phase
                    createAndPlayDecayingTone(
                        scope = coroutineScope, // Correct call signature
                        phaseName = phases[nextIndex].name,
                        durationSec = phases[nextIndex].durationSec
                    )
                    lastPhaseIndexForSound = nextIndex

                    state = state.copy(
                        phaseIndex = nextIndex,
                        elapsedInPhase = timeOvershoot,
                        totalTimeInCycle = (state.totalTimeInCycle + currentPhase.durationSec) % totalCycleDuration
                    )
                } else {
                    state = state.copy(
                        elapsedInPhase = newElapsedInPhase,
                        totalTimeInCycle = (state.totalTimeInCycle + deltaTime) % totalCycleDuration
                    )
                }

                delay(1000L / 60L)
            }
        } catch (e: Exception) {
            Log.e("Timer", "Timer loop error", e)
        }
    }

    // Cleanup on screen exit
    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.cancel()
            stopCurrentTone()
        }
    }

    // UI Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                title = { Text("Breathing Meditation") },
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Cycle time: ${"%.1f".format(totalCycleDuration)}s",
                        modifier = Modifier.weight(7f)
                    )

                    TextButton(
                        onClick = {
                            val isStarting = !state.isRunning
                            if (isStarting) {
                                lastPhaseIndexForSound = -1
                            }
                            state = state.copy(isRunning = isStarting)
                        },
                        modifier = Modifier.weight(3f)
                    ) {
                        Text(
                            if (state.isRunning) "Stop" else "Start",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        },
//        containerColor = Color(0xFFF5F5DC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val currentPhase = phases[state.phaseIndex]
            val remainingTime = (currentPhase.durationSec - state.elapsedInPhase).coerceAtLeast(0f)

            Text(
                text = "${currentPhase.text}: ${"%.1f".format(remainingTime)}s",
                color = currentPhase.color,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val numPhases = phases.size
                val barSpacing = 4.dp.toPx()
                val totalSpacing = barSpacing * (numPhases - 1)
                val uniformBarWidth = (canvasWidth - totalSpacing) / numPhases

                val minBarHeight = 10.dp.toPx()
                val maxBarHeight = canvasHeight * 0.8f

                var currentX = 0f

                phases.forEachIndexed { index, phase ->
                    val maxDuration = phases.maxOf { it.durationSec }
                    val targetHeightRatio = phase.durationSec / maxDuration
                    val targetBarHeight = minBarHeight.coerceAtLeast(maxBarHeight * targetHeightRatio)

                    var progressFillHeight = targetBarHeight
                    var progressFillWidth = uniformBarWidth

                    if (index == state.phaseIndex) {
                        val progressRatio = state.elapsedInPhase / phase.durationSec

                        progressFillHeight = when (phase.name) {
                            "INHALE" -> targetBarHeight * progressRatio
                            "HOLD" -> targetBarHeight
                            "EXHALE" -> targetBarHeight * (1f - progressRatio)
                            "PAUSE" -> minBarHeight * 0.1f
                            else -> 0f
                        }
                        progressFillWidth = when (phase.name) {
                            "HOLD" -> uniformBarWidth * progressRatio
                            "PAUSE" -> uniformBarWidth * progressRatio
                            else -> uniformBarWidth
                        }
                    }

                    val progressColor = phase.color.copy(alpha = 0.2f)

                    // Background bar
                    drawRect(
                        color = progressColor,
                        topLeft = Offset(currentX, canvasHeight - targetBarHeight),
                        size = Size(uniformBarWidth, targetBarHeight)
                    )

                    // Progress overlay
                    if (progressFillHeight > 0f || progressFillWidth > 0f) {
                        if (phase.name == "HOLD" || phase.name == "PAUSE") {
                            // Horizontal fill for holds
                            drawRect(
                                color = progressColor,
                                topLeft = Offset(currentX, canvasHeight - targetBarHeight),
                                size = Size(progressFillWidth, targetBarHeight)
                            )
                        } else {
                            // Vertical fill for inhale/exhale
                            drawRect(
                                color = progressColor,
                                topLeft = Offset(currentX, canvasHeight - progressFillHeight),
                                size = Size(uniformBarWidth, progressFillHeight)
                            )
                        }
                    }

                    // Progress cursor for current phase
                    if (index == state.phaseIndex) {
                        val progressRatio = state.elapsedInPhase / phase.durationSec
                        val cursorX = currentX + uniformBarWidth * progressRatio
                        val cursorY = canvasHeight - progressFillHeight

                        drawCircle(
                            color = Color.Yellow,
                            center = Offset(cursorX, cursorY.coerceAtLeast(0f)),
                            radius = 4.dp.toPx()
                        )
                    }

                    currentX += uniformBarWidth + barSpacing
                }
            }
        }
    }
}