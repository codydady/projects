package com.sd.nithyadharma

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sd.nithyadharma.model.MainViewModel
import com.sd.nithyadharma.screen.FloatingNavMenu
import com.sd.nithyadharma.screen.MainScreen
import com.sd.nithyadharma.screen.TempleMapScreen
import com.sd.nithyadharma.ui.theme.NithyaDharmaTheme
import com.sd.nithyadharma.util.AlarmSlotNotificationHelpers.addRasiPalanCard
import com.sd.nithyadharma.util.LocalAppLanguage
import com.sd.nithyadharma.util.PreferencesManager
import com.sd.nithyadharma.util.SlotManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    val preferencesManager by lazy { PreferencesManager(this) }

    // 🔑 Instantiate MainViewModel tied to Activity lifecycle
    // 🔑 Clean ViewModel instantiation using Kotlin delegate
    private val mainViewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(preferencesManager) as T
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val lifecycleOwner = LocalLifecycleOwner.current

            // Base 2: App Resume Observer
            // Runs when coming back from background / switching back from other apps
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        mainViewModel.purgeExpiredCards() // any cards that got cleared due to expiry
                        SlotManager.refreshSlot()         // to change colors
                    }
                }

                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            val requestPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d("Permission", "POST_NOTIFICATIONS permission granted.")
                } else {
                    Toast.makeText(this@MainActivity, "Notifications disabled. Please enable in settings.", Toast.LENGTH_LONG).show()
                }
            }

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
            }

            var activeCardColor by remember { mutableStateOf(Color.Unspecified) }
            val currentLang by mainViewModel.currentLang.collectAsStateWithLifecycle()
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()

            // 🔑 2. Provide currentlanguage to the ENTIRE Compose Tree
            CompositionLocalProvider(LocalAppLanguage provides currentLang) {
                NithyaDharmaTheme {
                    val scope = rememberCoroutineScope()

                    // Helper function for navigation (Kept intact for FloatingNavMenu)
                    fun navigateToRoute(route: String) {
                        if (route == "main") {
                            navController.popBackStack("main", inclusive = false)
                        } else {
                            navController.navigate(route) {
                                popUpTo("main") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }

                    // Root Box contains BOTH NavHost and FloatingNavMenu
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = "main",
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable("main") {

                                // logs too many times.commenting out for now
//                                FirebaseAppAnalytics.logScreenView("MainScreen")

                                // 🔑 Updated MainScreen call driven by ViewModel State
                                MainScreen(
                                    mainViewModel = mainViewModel,
                                    onSaveUserSetup = { newName, rasi, language -> mainViewModel.updateCustomerNameRasiLanguage(newName, rasi, language) }, // 👈 Saves both Name and Rasi via ViewModel
                                    onDismissCard = { cardId -> mainViewModel.dismissAndRemoveCard(cardId) },
                                    onTogglePinCard = { cardId -> mainViewModel.togglePinCard(cardId) },
                                    onSlotColorChanged = { primaryColor -> activeCardColor = primaryColor },
                                    onTempleItemClick = { navController.navigate("templeLocator") }
                                )
                            } // main screen nav ends
                            composable("templeLocator") {
                                TempleMapScreen(
                                    preferencesManager = preferencesManager,
                                    onBackClick = { navController.popBackStack() })
                            }
                            // this alone is here, rest will be handled by FloatingNavMenu action calls
                        }

                        // Layer 2: FloatingNavMenu (Floats over screen stack)
                        FloatingNavMenu(
                            onNavigate = { route -> navigateToRoute(route) },
                            onPushCard = { card -> mainViewModel.saveAndAddCard(card)  },
                            onAddRasiPalanCard = {
                                coroutineScope.launch {
                                    addRasiPalanCard(preferencesManager)
                                }
                            },
                            cardColor = if (activeCardColor != Color.Unspecified) activeCardColor else Color.Black,
                            onLanguageSelected = { newLang -> mainViewModel.setLanguage(newLang) } // 🟢 Pure MVVM!
                        )
                    }
                } // nithyadharma theme ends
            } // CompositionLocalProvider ends
        } // setContent ends

    } // onCreate ends

    /* important - may be remove for testing only to clear the customer object
        tied to mainviewmodel.debugResetCustomerInfo , so if one goes,
        other goes as well
    */
//    override fun onResume() {
//        super.onResume()
//        // 🧪 DEBUG: Reset customer name on every app resume to test setup dialog
//        mainViewModel.debugResetCustomerInfo()
//    }

} // class ends