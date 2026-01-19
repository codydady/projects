package com.sd.nithyadharma.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.nithyadharma.R
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.PreferencesManager
import com.sd.nithyadharma.util.Constants
import com.sd.nithyadharma.util.SoundManager
import com.sd.nithyadharma.util.TTSManager // Make sure this import is correct for your TTSManager
import kotlinx.coroutines.flow.first
import com.sd.nithyadharma.util.FirebaseAppAnalytics // Ensure this import is correct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(
    preferencesManager : PreferencesManager,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
//    val preferencesManager = remember { PreferencesManager(context) }
    val soundManager = remember { SoundManager.getInstance(context) }

    var count by rememberSaveable { mutableStateOf(0) }
    var alertInterval by remember { mutableStateOf(Constants.DEFAULT_ALERT_INTERVAL) }
    var finalCount by remember { mutableStateOf(Constants.DEFAULT_FINAL_COUNT) }
    var isLoading by remember { mutableStateOf(true) }

    val currentLang by preferencesManager.getSelectedLanguage()
        .collectAsState(initial = NDLanguage.EN)

    DisposableEffect(Unit) {
        // --- Cleanup block for DisposableEffect ---
        // This runs when the Composable leaves the composition (e.g., navigated away from this screen)
        onDispose {
            // Release SoundManager resources
            soundManager.release()
//            Log.d(TAG, "CounterScreen disposed")
        }
    }

    // Load preferences when the Composable is first launched
    LaunchedEffect(Unit) {
        alertInterval = preferencesManager.getAlertInterval().first()
        finalCount = preferencesManager.getFinalCount().first()
        count = preferencesManager.getCounterValue().first()
        isLoading = false
//        Log.d(TAG, "CounterScreen preferences loaded.")
    }

    // Save count whenever it changes
    LaunchedEffect(count) {
        preferencesManager.saveCounterValue(count)
//        Log.d(TAG, "Count saved: $count")
    }

    // Handle back press
    BackHandler { onBackClick() }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    title = { Text( LocaleManager.getString("btn_mc", currentLang) ) },
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
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            LocaleManager.getString("ct_finalct", currentLang) +":"+ finalCount,
                            modifier = Modifier.weight(0.6f)
                        )
//                        Spacer(modifier = Modifier.weight(0.1f))
                        TextButton(
                            onClick = {
                                count = 0
                                Log.d(TAG, "Counter reset to 0.")
                            },
                            modifier = Modifier.weight(0.4f)
                        ) {
                            Text(LocaleManager.getString("ct_reset", currentLang))
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .clickable {
                        count++
                        soundManager.playKuduk()

                        if (count % alertInterval == 0) {
                            TTSManager.speakNumber(count)
                        } else if (count == finalCount) {
                            TTSManager.speak("You have completed $count")
                            FirebaseAppAnalytics.logCounterMilestone(count)
                            Log.d(TAG, "Final count reached: $count")
                        } else {
//                            Log.d(TAG, "Playing kuduk sound.")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { // Arrange vertically, center horizontally
                    Text(LocaleManager.getString("ct_motto", currentLang),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center )
                    Spacer(modifier = Modifier.height(24.dp)) // Space before form fields start

                    Image(
                        painter = painterResource(id = R.drawable.munivar),
                        contentDescription = "munivar",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(180.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp)) // Space before form fields start

                    Text("$count", fontSize = 140.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center )
                }
            }
        }
    }
}

private const val TAG = "CounterScreen"