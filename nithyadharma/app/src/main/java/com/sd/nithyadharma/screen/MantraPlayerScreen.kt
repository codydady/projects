package com.sd.nithyadharma.screen

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log // Added for robust logging
import android.view.WindowManager

import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.sd.nithyadharma.R
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.PreferencesManager


// --- Data Class for Media Buttons ---
data class MediaButtonItem(
    val text: String,
    val color: Color,
    @RawRes val musicResId: Int? = null,
    val icon: ImageVector? = null
)

// --- MantraPlayerScreen Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantraPlayerScreen(
    preferencesManager : PreferencesManager,
    onBackClick: () -> Unit) {
    val context = LocalContext.current

    val currentLang by preferencesManager.getSelectedLanguage()
        .collectAsState(initial = NDLanguage.EN)

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    // Use a single mutable state for the MediaPlayer instance, initialized to null
    var mediaPlayerInstance by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentPlayingMusicResId by rememberSaveable { mutableStateOf<Int?>(null) }
    var isPlaying by remember { mutableStateOf(false) } // Reflects if media is actively playing/resumable

    // Separate state to track audio focus granted to THIS Composable for THIS playback
    var hasAudioFocus by remember { mutableStateOf(false) }

    // --- Audio Focus Change Listener ---
    // This listener should only operate on the mediaPlayerInstance
    val audioFocusChangeListener = remember {
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // Permanent or transient loss of focus, stop playback and clean up
                    Log.d("MantraPlayerScreen", "Audio Focus LOST. Stopping playback.")
                    mediaPlayerInstance?.apply {
                        if (isPlaying) { // Only stop if it's currently playing or resumable
                            stop() // Stop playback
                        }
                        // Important: Don't release here immediately on transient loss if you plan to resume.
                        // However, for permanent loss (like calls), releasing is appropriate.
                        // Given the context of "call came thru," assume this is a complete stop/release.
                        release() // Release resources
                    }
                    mediaPlayerInstance = null // Clear the instance
                    isPlaying = false // Update UI state
                    currentPlayingMusicResId = null // Clear selected mantra as playback ended/stopped
                    hasAudioFocus = false // Update focus state
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    // Lower volume if possible, but keep playing
                    mediaPlayerInstance?.setVolume(0.1f, 0.1f)
                    Log.d("MantraPlayerScreen", "Audio Focus DUCK. Volume lowered.")
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // Gained focus, resume playback at full volume
                    mediaPlayerInstance?.setVolume(1.0f, 1.0f)
                    if (currentPlayingMusicResId != null && mediaPlayerInstance == null) {
                        // If we lost focus and released, but then gained focus again,
                        // and a mantra was selected, we need to restart it completely.
                        // Setting currentPlayingMusicResId will trigger the DisposableEffect.
                        Log.d("MantraPlayerScreen", "Audio Focus GAINED. Attempting to resume full playback by re-triggering effect.")
                        // The DisposableEffect will re-create and start if currentPlayingMusicResId is not null.
                        // No direct start() here; let the effect handle it.
                    } else if (mediaPlayerInstance != null && !isPlaying) {
                        // If it was just paused (not released) and gained focus, resume.
                        mediaPlayerInstance?.start()
                        isPlaying = true
                        Log.d("MantraPlayerScreen", "Audio Focus GAINED. Volume restored and playback resumed.")
                    }
                    hasAudioFocus = true // Update focus state
                }
            }
        }
    }

    // --- Media Player & Audio Focus Lifecycle Management with DisposableEffect ---
    // This DisposableEffect drives the creation/destruction of MediaPlayer
    DisposableEffect(currentPlayingMusicResId) {
        val selectedMusicResId = currentPlayingMusicResId
        var currentEffectMediaPlayer: MediaPlayer? = null // Local var for this specific effect run

        // Cleanup before new setup if key changes
        if (selectedMusicResId == null) {
            // This path is taken when currentPlayingMusicResId becomes null (e.g., stop button clicked)
            // The onDispose below handles the cleanup for the *previous* MediaPlayer.
            // Ensure isPlaying is false if no music is selected.
            isPlaying = false
            Log.d("MantraPlayerScreen", "DisposableEffect triggered by currentPlayingMusicResId == null. Cleanup will run.")
        } else {
            // New music selected or re-triggered for same music (e.g. after focus gain)
//            Log.d("MantraPlayerScreen", "DisposableEffect triggered for ID: $selectedMusicResId. Setting up MediaPlayer.")

            // Abandon focus for any previous player if it was active
            if (hasAudioFocus) {
                audioManager.abandonAudioFocus(audioFocusChangeListener)
                hasAudioFocus = false
            }

            // Request Audio Focus before playing
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN // Request permanent focus for continuous playback
            )

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                hasAudioFocus = true // Update screen-level focus state
                try {
                    // Create a new MediaPlayer instance
                    val newPlayer = MediaPlayer.create(context, selectedMusicResId).apply {
                        isLooping = true // Repeat indefinitely
                        setOnPreparedListener { mp ->
//                            Log.d("MantraPlayerScreen", "MediaPlayer prepared. Starting playback for ID: $selectedMusicResId")
                            mp.start() // Start playback immediately after preparation
                            isPlaying = true // Update UI state
                            mediaPlayerInstance = mp // Assign to the shared mutable state
                        }
                        setOnCompletionListener { mp ->
//                            Log.d("MantraPlayerScreen", "MediaPlayer completed for ID: $selectedMusicResId")
                            isPlaying = false // Update UI state
                            // If looping, this won't be called. If not looping, it finishes.
                            // For a single play, you might reset() or release() here.
                            // With looping, this listener won't fire until looping is disabled.
                        }
                        setOnErrorListener { mp, what, extra ->
                            Log.e("MantraPlayerScreen", "MediaPlayer error: what=$what, extra=$extra")
                            isPlaying = false // Update UI state
                            currentPlayingMusicResId = null // Clear selected mantra on error
                            hasAudioFocus = false // Assume focus lost on error
                            mp.reset() // Attempt to reset to recover from error state
                            mp.release() // Release after reset on error to be safe
                            mediaPlayerInstance = null // Clear the instance
                            audioManager.abandonAudioFocus(audioFocusChangeListener) // Abandon focus on error
                            true // Indicate that the error was handled
                        }
                    }
                    currentEffectMediaPlayer = newPlayer // Local reference for this effect's cleanup
                    mediaPlayerInstance = newPlayer // Assign to the shared state
                    Log.d("MantraPlayerScreen", "MediaPlayer instance created for ID: $selectedMusicResId")

                    // Initial start if MediaPlayer.create didn't already start (some versions/devices might)
                    if (!newPlayer.isPlaying) {
                        newPlayer.start()
                        isPlaying = true
                    }
                } catch (e: Exception) {
                    Log.e("MantraPlayerScreen", "Error creating/playing music for ID: $selectedMusicResId: ${e.message}", e)
                    audioManager.abandonAudioFocus(audioFocusChangeListener)
                    hasAudioFocus = false
                    isPlaying = false
                    currentPlayingMusicResId = null // Reset state if failed to play
                    currentEffectMediaPlayer?.release() // Ensure local player is released on setup error
                    mediaPlayerInstance = null
                }
            } else {
                Log.w("MantraPlayerScreen", "Audio Focus NOT granted. Cannot play music for ID: $selectedMusicResId.")
                currentPlayingMusicResId = null // Cannot play, so reset the state
                isPlaying = false
                hasAudioFocus = false
            }
        }

        // --- Cleanup block: Runs when currentPlayingMusicResId changes OR Composable leaves composition ---
        onDispose {
            Log.d("MantraPlayerScreen", "DisposableEffect cleanup for currentPlayingMusicResId: $selectedMusicResId")

            // IMPORTANT: Safely stop and release the MediaPlayer
            // Use the instance that was created by *this specific effect run* (`currentEffectMediaPlayer`)
            // OR the shared `mediaPlayerInstance` if it's the one we expect to clean up.
            // The `mediaPlayerInstance` is generally better as it's the single source of truth.
            mediaPlayerInstance?.apply {
                try {
                    // Check if player is not null and is in a state where stopping is valid
                    // isPlaying covers Started. isLooping means it's still active.
                    // This is the core fix for IllegalStateException on stop()
                    if (isPlaying) { // If it's playing, stop it
                        stop()
                        Log.d("MantraPlayerScreen", "MediaPlayer stopped during dispose.")
                    } else {
                        // If not playing, but still exists (e.g., paused, prepared), ensure it's in a stoppable state or handle its state
                        // This check is mainly for robust logging/understanding.
                        // The primary goal is to release it, but calling stop() first is good practice if it's active.
                        Log.d("MantraPlayerScreen", "MediaPlayer not playing, but exists. Proceeding to release.")
                    }
                } catch (e: IllegalStateException) {
                    Log.e("MantraPlayerScreen", "IllegalStateException during MediaPlayer stop() in dispose: ${e.message}", e)
                    // This block catches the exact error. It means the player was already in an invalid state.
                } catch (e: Exception) {
                    Log.e("MantraPlayerScreen", "Unexpected error during MediaPlayer stop() in dispose: ${e.message}", e)
                } finally {
                    try {
                        release() // Always release to free native resources
                        Log.d("MantraPlayerScreen", "MediaPlayer released during dispose.")
                    } catch (e: Exception) {
                        Log.e("MantraPlayerScreen", "Error releasing MediaPlayer in dispose: ${e.message}", e)
                    }
                }
            }
            mediaPlayerInstance = null // Ensure the shared state is cleared after release
            isPlaying = false // Update UI state
            hasAudioFocus = false // Clear focus state

            // Abandon audio focus if it was granted by this Composable.
            // This is crucial to release focus when playback stops or screen is disposed.
            audioManager.abandonAudioFocus(audioFocusChangeListener)
            Log.d("MantraPlayerScreen", "Audio Focus abandoned during dispose.")
        }
    }

    // --- Lifecycle Observer to handle app background/foreground transitions ---
    // This is separate from the MediaPlayer DisposableEffect to handle broader app lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // App is going to background or another activity comes to foreground
                    if (mediaPlayerInstance?.isPlaying == true) {
                        Log.d("MantraPlayerScreen", "Lifecycle ON_PAUSE: Pausing music.")
                        mediaPlayerInstance?.pause() // Pause instead of full stop
                        isPlaying = false // Update UI state
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    // App is coming to foreground
                    Log.d("MantraPlayerScreen", "Lifecycle ON_RESUME.")
                    // If music was playing before pause, and we still have focus, resume it
                    // This logic is tricky. If audio focus was lost (e.g., call), you might not want to auto-resume.
                    // The audioFocusChangeListener's AUDIOFOCUS_GAIN would handle auto-resume more reliably.
                }
                Lifecycle.Event.ON_DESTROY -> {
                    // App being destroyed (not just backgrounded)
                    Log.d("MantraPlayerScreen", "Lifecycle ON_DESTROY: Performing full cleanup.")
                    // The main DisposableEffect's onDispose will handle MediaPlayer release and focus abandon.
                    // Just ensure state is reset here if needed for robustness.
                    isPlaying = false
                    currentPlayingMusicResId = null
                    mediaPlayerInstance = null
                    hasAudioFocus = false
                }
                else -> { /* Do nothing for other events */ }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // --- Screen Visibility (Keep Screen On) Logic ---
    val window = (LocalView.current.context as? Activity)?.window
    DisposableEffect(isPlaying) { // React to the 'isPlaying' state
        if (isPlaying) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            Log.d("MantraPlayerScreen", "FLAG_KEEP_SCREEN_ON added.")
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            Log.d("MantraPlayerScreen", "FLAG_KEEP_SCREEN_ON cleared.")
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            Log.d("MantraPlayerScreen", "FLAG_KEEP_SCREEN_ON cleared on dispose for screen visibility effect.")
        }
    }

    // old colors
//    val mantraButtons = listOf(
//        MediaButtonItem("Ganesha Mantra", Color(0xFF837E50), musicResId = R.raw.ganesha, icon = Icons.Filled.PlayArrow),
//        MediaButtonItem("Shanmukha Mantra", Color(0xFF8D825C), musicResId = R.raw.muruga, icon = Icons.Filled.PlayArrow),
//        MediaButtonItem("Vishnu Mantra", Color(0xFF8D7B5E), musicResId = R.raw.vishnu, icon = Icons.Filled.PlayArrow),
//        MediaButtonItem("Dhathatreya Mantra", Color(0xFF9D9765), musicResId = R.raw.dhathatreya, icon = Icons.Filled.PlayArrow),
//        MediaButtonItem("Shiva Mantra", Color(0xFF937C59), musicResId = R.raw.shiva, icon = Icons.Filled.PlayArrow),
//        MediaButtonItem("Devi Mantra", Color(0xFF988753), musicResId = R.raw.shakthi, icon = Icons.Filled.PlayArrow),
//        MediaButtonItem("Tara Mantra", Color(0xFF917E64), musicResId = R.raw.tara, icon = Icons.Filled.PlayArrow),
//    )

    // new colors to suit the underlying background
    val mantraButtons = listOf(
        MediaButtonItem(LocaleManager.getString("mp_ganesha", currentLang), Color(0xFFB79B82), musicResId = R.raw.ganesha, icon = Icons.Filled.PlayArrow),
        MediaButtonItem(LocaleManager.getString("mp_murugan", currentLang), Color(0xFF8C9B8B), musicResId = R.raw.muruga, icon = Icons.Filled.PlayArrow),
        MediaButtonItem(LocaleManager.getString("mp_vishnu", currentLang), Color(0xFFC29962), musicResId = R.raw.vishnu, icon = Icons.Filled.PlayArrow),
        MediaButtonItem(LocaleManager.getString("mp_ddtrya", currentLang), Color(0xFFA87B6E), musicResId = R.raw.dhathatreya, icon = Icons.Filled.PlayArrow),
        MediaButtonItem(LocaleManager.getString("mp_shiva", currentLang), Color(0xFF808362), musicResId = R.raw.shiva, icon = Icons.Filled.PlayArrow),
        MediaButtonItem(LocaleManager.getString("mp_devi", currentLang), Color(0xFF968777), musicResId = R.raw.shakthi, icon = Icons.Filled.PlayArrow),
        MediaButtonItem(LocaleManager.getString("mp_tara", currentLang), Color(0xFF917E64), musicResId = R.raw.tara, icon = Icons.Filled.PlayArrow),
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    title = { Text( LocaleManager.getString("btn_mp", currentLang) ) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                            )
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            LocaleManager.getString("mp_bottom", currentLang),
//                            fontSize = 14.sp,
                            modifier = Modifier.weight(0.6f)
                        )
                        Spacer(modifier = Modifier.weight(0.1f))
                        if (currentPlayingMusicResId != null) { // Only show Stop button if a mantra is selected
                            TextButton(
                                onClick = {
                                    // Stop button clicked: clear selected mantra and update isPlaying
                                    mediaPlayerInstance?.apply {
                                        try {
                                            if (isPlaying) {
                                                stop() // Stop playback
                                            }
                                            reset() // Reset for next use
                                        } catch (e: IllegalStateException) {
                                            Log.e("MantraPlayerScreen", "IllegalStateException on Stop button: ${e.message}", e)
                                        } finally {
                                            // Always ensure release on manual stop to free native resources
                                            release()
                                            mediaPlayerInstance = null // Clear the instance
                                        }
                                    }
                                    currentPlayingMusicResId = null // Clear selected mantra
                                    isPlaying = false // Crucial: Update isPlaying immediately
                                    audioManager.abandonAudioFocus(audioFocusChangeListener) // Abandon focus manually
                                },
                                modifier = Modifier.weight(0.3f)
                            ) {
                                Text( LocaleManager.getString("mp_stop", currentLang) )
                            }
                        }
                    }
                }
            }
        ) // for scaffold
        { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
//                    .background(Color(0xFFF5F5DC))
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dakshinamurthy),
                    contentDescription = "veena dakshinamurthy",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(180.dp)
                )
                mantraButtons.forEach { item ->
                    // Determine if this specific mantra is the one currently playing or selected
                    val isPlayingThisMantra = currentPlayingMusicResId == item.musicResId && isPlaying

                    Button(
                        onClick = {
                            if (item.musicResId != null) {
                                if (isPlayingThisMantra) {
                                    // If this mantra is playing, pause it
                                    mediaPlayerInstance?.pause()
                                    isPlaying = false
                                    Log.d("MantraPlayerScreen", "Mantra paused: ${item.text}")
                                } else {
                                    // If it's a different mantra or currently paused, start/resume
                                    // Setting currentPlayingMusicResId triggers the main DisposableEffect
                                    if (currentPlayingMusicResId != item.musicResId) {
                                        // If selecting a *new* mantra, immediately stop/release old one and set new ID
                                        // This ensures the DisposableEffect gets a clean state change.
                                        mediaPlayerInstance?.apply {
                                            if(isPlaying) stop()
                                            release()
                                        }
                                        mediaPlayerInstance = null
                                        isPlaying = false
                                    }
                                    currentPlayingMusicResId = item.musicResId
                                    Log.d("MantraPlayerScreen", "Mantra selected/resumed: ${item.text}. Triggering DisposableEffect.")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = item.color,
                        ),
                        contentPadding = PaddingValues(8.dp)

                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 1.dp) // Adjust this value (e.g., 8.dp, 12.dp, 16.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    clip = true,
//                                    ambientColor = MaterialTheme.colorScheme.outlineVariant,
//                                    spotColor = MaterialTheme.colorScheme.outline
                                )
                        ) {
                            item.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(end = 8.dp)
                                )
                            }
                            Text(item.text, fontSize = 14.sp)
                            if (item.musicResId != null && isPlayingThisMantra) {
                                Text(" ( Playing )",  fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
