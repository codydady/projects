package com.sd.nithyadharma

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.screen.AboutScreen
import com.sd.nithyadharma.screen.CounterScreen
import com.sd.nithyadharma.screen.FeedbackScreen
import com.sd.nithyadharma.screen.GaneshaSplashScreen
import com.sd.nithyadharma.screen.HinduCalendarScreen
import com.sd.nithyadharma.screen.LightALampScreen
import com.sd.nithyadharma.screen.MainScreen
import com.sd.nithyadharma.screen.MantraPlayerScreen
import com.sd.nithyadharma.screen.MeditateScreen
import com.sd.nithyadharma.screen.PreferencesScreen
import com.sd.nithyadharma.screen.PujaStoreScreen
import com.sd.nithyadharma.screen.RequestHelpScreen
import com.sd.nithyadharma.screen.TempleMapScreen
import com.sd.nithyadharma.ui.theme.NithyaDharmaTheme
import com.sd.nithyadharma.util.FirebaseAppAnalytics
import com.sd.nithyadharma.util.PreferencesManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {

    val preferencesManager by lazy { PreferencesManager(this) }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // NOTE: Google Sign-In and setUserId(email) removed for privacy and simplicity.
        // Firebase Analytics still works automatically behind the scenes.

        setContent {
            // 1. Create a state to track the splash visibility
            var showSplash by remember { mutableStateOf(true) }

            val requestPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                isGranted: Boolean ->
                if (isGranted) {
                    Log.d("Permission", "POST_NOTIFICATIONS permission granted.")
                } else {
                    Log.w("Permission", "POST_NOTIFICATIONS permission denied. Notifications may not appear.")
                    Toast.makeText(this@MainActivity, "Notifications disabled. Please enable in settings.", Toast.LENGTH_LONG).show()
                }
            }

            val navController = rememberNavController()

            // Replacing above with this to check - nov 18
            LaunchedEffect(Unit) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                // Only handle notification deep-link if NOT launched from recent-apps history
                val isFromHistory = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
                val navigateTo = intent.getStringExtra("navigateTo")

                if (!isFromHistory && navigateTo == "hinduCalendar") {
                    navController.navigate("hinduCalendar") {
                        // Optional: clear the back stack so multiple notification taps don't stack screens
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                    // 🔒 CONSUME the extra
                    intent.removeExtra("navigateTo")
                }
            }

            val currentLang by preferencesManager.getSelectedLanguage()
                .collectAsState(initial = NDLanguage.EN)

            NithyaDharmaTheme(currentLang) {

                if (showSplash) {
                    GaneshaSplashScreen(
                        onSplashComplete = {
                            showSplash = false // Transitions to NavHost when animation ends
                        }
                    )
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier.fillMaxSize()
                    ) {

                        composable("main") {
                            FirebaseAppAnalytics.logScreenView("MainScreen", "MainActivity")
                            MainScreen(
                                preferencesManager = preferencesManager,
                                onNavigate = { screenId ->
                                    when (screenId) {
                                        "hc" -> navController.navigate("hinduCalendar")
                                        "mc" -> navController.navigate("counter")
                                        "tm" -> navController.navigate("templeMap")
                                        "pp" -> navController.navigate("pujaProducts")
                                        "mp" -> navController.navigate("manthraPlayer")
                                        "ll" -> navController.navigate("lightALamp")
                                        "md" -> navController.navigate("meditate")
                                    }
                                },
                                onPreferencesClick = { navController.navigate("preferences") },
                                onRALClick = { navController.navigate("requestHelp") },
                                onFeedbackClick = { navController.navigate("feedback") },
                                onAboutClick = { navController.navigate("about") }
                            )
                        }
                        composable("hinduCalendar") {
                            FirebaseAppAnalytics.logScreenView("HinduCalendarScreen", "MainActivity")
                            HinduCalendarScreen(
                                preferencesManager = preferencesManager,
                                onBackClick = { navController.popBackStack() })
                        }
                        composable("counter") {
                            FirebaseAppAnalytics.logScreenView("CounterScreen", "MainActivity")
                            CounterScreen(
                                preferencesManager = preferencesManager,
                                onBackClick = { navController.popBackStack() })
                        }
                        composable("templeMap") {
                            FirebaseAppAnalytics.logScreenView("TempleMapScreen", "MainActivity")
                            TempleMapScreen(
                                preferencesManager = preferencesManager,
                                onBackClick = { navController.popBackStack() })
                        }
                        composable("pujaProducts") {
                            FirebaseAppAnalytics.logScreenView("PujaStoreScreen", "MainActivity")
                            PujaStoreScreen(
                                preferencesManager = preferencesManager,
                                onBackClick = { navController.popBackStack() })
                        }
                        composable("manthraPlayer") {
                            FirebaseAppAnalytics.logScreenView("MantraPlayerScreen", "MainActivity")
                            MantraPlayerScreen(
                                preferencesManager = preferencesManager,
                                onBackClick = { navController.popBackStack() })
                        }
                        composable("lightALamp") {
                            FirebaseAppAnalytics.logScreenView("LightALampScreen", "MainActivity")
                            LightALampScreen(
                                preferencesManager = preferencesManager,
                                onBackClick = { navController.popBackStack() })
                        }
                        composable("preferences") {
                            FirebaseAppAnalytics.logScreenView("PreferencesScreen", "MainActivity")
                            PreferencesScreen(
                                preferencesManager = preferencesManager,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable("feedback") {
                            FirebaseAppAnalytics.logScreenView("FeedbackScreen", "MainActivity")
                            FeedbackScreen(
                                preferencesManager = preferencesManager,
                                onBackClick = { navController.popBackStack() })
                        }
                        composable("meditate") {
                            FirebaseAppAnalytics.logScreenView("MeditateScreen", "MainActivity")
                            MeditateScreen(
                                preferencesManager = preferencesManager,
                                onBackClick = { navController.popBackStack() })
                        }
                        composable("requestHelp") {
                            FirebaseAppAnalytics.logScreenView("RequestHelpScreen", "MainActivity")
                            RequestHelpScreen(
                                preferencesManager = preferencesManager,
                                onBackClick = { navController.popBackStack() })
                        }
                        composable("about") {
                            FirebaseAppAnalytics.logScreenView("AboutScreen", "MainActivity")
                            AboutScreen(
                                preferencesManager,
                                onBackClick = { navController.popBackStack() })
                        }
                    }
                }// for splash screen end
            }
        } // setcontent ends

    } // oncreate ends

} //class ends
