package com.sd.nithyadharma.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sd.nithyadharma.model.languageName
import com.sd.nithyadharma.model.PanchangaAttributes

import com.sd.nithyadharma.util.PreferencesManager
import com.sd.nithyadharma.util.Constants
import kotlinx.coroutines.launch
import com.sd.nithyadharma.model.CustomerInfo
import com.sd.nithyadharma.model.NDLanguage
import java.time.LocalDateTime

import androidx.compose.runtime.remember
import com.sd.nithyadharma.model.PanchangaAttributes.Rasi
import com.sd.nithyadharma.util.Constants.dttmFormatter
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    preferencesManager: PreferencesManager,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentLang by preferencesManager.getSelectedLanguage()
        .collectAsState(initial = NDLanguage.EN)

    val earlyReminder by preferencesManager.getEarlyReminder().collectAsState(initial = Constants.DEFAULT_EARLY_REMINDER_DAYS)
    val alertInterval by preferencesManager.getAlertInterval().collectAsState(initial = Constants.DEFAULT_ALERT_INTERVAL)
    val finalCount by preferencesManager.getFinalCount().collectAsState(initial = Constants.DEFAULT_FINAL_COUNT)
    val showVisitedTemples by preferencesManager.getHideVisitedTemples().collectAsState(initial = false)
    val showOnlyMarkedTemples by preferencesManager.getShowOnlyMarkedTemples().collectAsState(initial = false)

    var saveMessage by remember { mutableStateOf("") }

    val customerInfoFlow = preferencesManager.getCustomerInfo()
    val customerInfo by customerInfoFlow.collectAsState(initial = CustomerInfo("", "", "", "", "", "", "", "", "", "", ""))

    var name by remember { mutableStateOf(customerInfo.name) }
    var email by remember { mutableStateOf(customerInfo.email) }
    var phone by remember { mutableStateOf(customerInfo.phone) }
    var address1 by remember { mutableStateOf(customerInfo.address1) }
    var address2 by remember { mutableStateOf(customerInfo.address2) }
    var city by remember { mutableStateOf(customerInfo.city) }
    var state by remember { mutableStateOf(customerInfo.state) }
    var pincode by remember { mutableStateOf(customerInfo.pincode) }

    // for horoscope to work but part of customer profile
    var dttmOfBirth by remember { mutableStateOf(customerInfo.dttmOfBirth) }
    var lat by remember { mutableStateOf(customerInfo.lat) }
    var lon by remember { mutableStateOf(customerInfo.lon) }
    // this is to convert dttmofbirth to a datetime field
    var birthDateTime by remember { mutableStateOf(LocalDateTime.now()) }

//    var sliderValue by remember { mutableStateOf(112f) } // Initialize with the starting value
    val stepValues = listOf(112f, 224f, 336f, 448f, 560f, 672f, 784f, 896f, 1008f)
    val steps = stepValues.size - 2 // 8 steps for 9 values

    // --- Breathing Timings --- along the lines of 4-7-8 ratio
    // These collectAsState are essential for getting the initial and updated values
    val inhaleTime by preferencesManager.getInhaleTime().collectAsState(initial = 6f)
    val holdTime by preferencesManager.getHoldTime().collectAsState(initial = 12f)
    val exhaleTime by preferencesManager.getExhaleTime().collectAsState(initial = 16f)
    val pauseTime by preferencesManager.getPauseTime().collectAsState(initial = 4f)

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm")

    LaunchedEffect(customerInfo) {
        Log.d("--Preferencesscren--", "LaunchedEffect(customerInfo)" )

        name = customerInfo.name
        email = customerInfo.email
        phone = customerInfo.phone
        address1 = customerInfo.address1
        address2 = customerInfo.address2
        city = customerInfo.city
        state = customerInfo.state
        pincode = customerInfo.pincode
        dttmOfBirth = customerInfo.dttmOfBirth
        lat = customerInfo.lat
        lon = customerInfo.lon

        if (customerInfo.dttmOfBirth.isNotBlank()) {
            try {
                birthDateTime =
                    LocalDateTime.parse(customerInfo.dttmOfBirth, formatter)
            } catch (e: Exception) {
                birthDateTime = LocalDateTime.now()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                title = { Text(LocaleManager.getString("btn_pf", currentLang)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // language selection
            SectionCard(background = Color(0xFFDAD5C3)) {
                Text(LocaleManager.getString("pf_lang", currentLang), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))

                // Much cleaner: no .map {} or try-catch needed here anymore
                val selectedLang by preferencesManager.getSelectedLanguage()
                    .collectAsState(initial = NDLanguage.EN)

                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    TextField(
                        value = languageName(selectedLang),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Language") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC),
                        ),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .shadow(elevation = 0.dp) // Remove shadow
                    ) {
                        NDLanguage.entries.forEach { language ->
                            DropdownMenuItem(
                                text = { Text(languageName(language)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFFFF0)), // Ivory background for the item
                                onClick = {
                                    expanded = false
                                    scope.launch {
                                        preferencesManager.saveSelectedLanguage(language)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            SectionCard(background = Color(0xFFe2f4c7)) {
                Text(LocaleManager.getString("pf_rasi", currentLang), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))

                // 1. Cleaner Collection: The PreferencesManager now handles the Enum conversion logic
                val currentLang by preferencesManager.getSelectedLanguage().collectAsState(initial = NDLanguage.EN)
                val selectedRasi by preferencesManager.getSelectedRasi().collectAsState(initial = Rasi.MESHA)

                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    TextField(
                        value = PanchangaAttributes.rasiName(selectedRasi, currentLang),

                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Rasi") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC),
                        ),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.shadow(elevation = 0.dp)
                    ) {
                        Rasi.entries.forEach { rasi ->
                            DropdownMenuItem(
                                text = { Text(PanchangaAttributes.rasiName(rasi, currentLang)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFFFF0)),
                                onClick = {
                                    expanded = false
                                    scope.launch {
                                        preferencesManager.saveSelectedRasi(rasi)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // -- for breathing exercise and inhale/hold/exhale/hold times
            // --- NEW: Breathing Timings Section ---
            // 1. Calculate the total duration for display
            if ( Constants.PAYING_CUSTOMER) {

                val totalCycleDuration = remember(inhaleTime, holdTime, exhaleTime, pauseTime) {
                    inhaleTime + holdTime + exhaleTime + pauseTime
                }
                SectionCard(background = Color(0xFFD9ECF1)) { // Light Blue background
                    Text(
                        "Breathing Timings (seconds)",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(Modifier.height(8.dp))

                    // Helper function to create the input field
                    @Composable
                    fun TimeInput(label: String, currentValue: Float, phaseName: String) {
                        // Use a temporary mutable state to hold the string value currently typed by the user
                        var textValue by remember { mutableStateOf(currentValue.toString()) }

                        // Reset the temporary state when the collected preference value changes (e.g., after initial load or successful save)
                        LaunchedEffect(currentValue) {
                            textValue = currentValue.toString()
                        }

                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { newValue ->
                                textValue =
                                    newValue // Update the temporary string state immediately

                                // Try to convert to Float
                                val floatValue = newValue.toFloatOrNull()

                                if (floatValue != null) {
                                    // Basic validation: Ensure the value is positive
                                    val validatedValue = floatValue.coerceAtLeast(0.01f)

                                    // Save ALL timings whenever any one field successfully validates
                                    scope.launch {
                                        preferencesManager.saveBreathingTimings(
                                            inhale = if (phaseName == "Inhale") validatedValue else inhaleTime,
                                            hold = if (phaseName == "Hold") validatedValue else holdTime,
                                            exhale = if (phaseName == "Exhale") validatedValue else exhaleTime,
                                            pause = if (phaseName == "Pause") validatedValue else pauseTime
                                        )
                                    }
                                }
                            },
                            label = { Text(label) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            textStyle = LocalTextStyle.current.copy(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F5DC),
                                unfocusedContainerColor = Color(0xFFF5F5DC),
                            )
                        )
                    }

                    // Input fields for each phase - Note: onValueChange lambda is no longer needed
                    TimeInput("Inhale", inhaleTime, "Inhale")
                    TimeInput("Hold (Kumbhaka)", holdTime, "Hold")
                    TimeInput("Exhale", exhaleTime, "Exhale")
                    TimeInput("Pause (Bahir Kumbhaka)", pauseTime, "Pause")

                    Text(
                        "Current Total Cycle: ${"%.2f".format(totalCycleDuration)}s",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                // --- Schedule Preferences Section (Unchanged) ---
                SectionCard(background = Color(0xFFEBE8F1)) {
                    Text("Dharmic Calendar", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))

                    Text("Early Reminder (days before): $earlyReminder")
                    Slider(
                        value = earlyReminder.toFloat(),
                        onValueChange = { scope.launch { preferencesManager.saveEarlyReminder(it.toInt()) } },
                        valueRange = 2f..4f,
                        steps = 0,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }

                // --- Temple Map Preferences Section (Unchanged) ---
                SectionCard(background = Color(0xFFEEB5A3)) {
                    Text("Temple Map", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showVisitedTemples,
                            onCheckedChange = {
                                scope.launch {
                                    preferencesManager.saveHideVisitedTemples(
                                        it
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hide visited temples (less clutter - premium)")
                    }
                    // only for sriram for me now as of nov 4,25
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showOnlyMarkedTemples,
                            onCheckedChange = {
                                scope.launch {
                                    preferencesManager.saveShowOnlyMarkedTemples(
                                        it
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show only marked temples (less clutter - premium)")
                    }
                }

            } // end of super user check

            // --- Counter Preferences Section (Unchanged) ---
            SectionCard(background = Color(0xFFD9E4DD)) {
                Text(LocaleManager.getString("btn_mc", currentLang), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))

                Text(LocaleManager.getString("pf_intercnt", currentLang, alertInterval))
                Slider(
                    value = alertInterval.toFloat(),
                    onValueChange = { scope.launch { preferencesManager.saveAlertInterval(it.toInt()) } },
                    valueRange = 18f..36f,
                    steps = 1,
                    modifier = Modifier.fillMaxWidth(0.7f),
                )

                Spacer(Modifier.height(8.dp))

                Text(LocaleManager.getString("pf_finalcnt", currentLang, finalCount))
                Slider(
                    value = finalCount.toFloat(),
                    onValueChange = { scope.launch { preferencesManager.saveFinalCount(it.toInt()) } },
                    valueRange = 112f..1008f,
                    steps = steps,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }

            SectionCard(background = Color(0xFFEDE4D2)) {
                Text(LocaleManager.getString("pp_custdtls", currentLang), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))

                fun save() {
                    scope.launch {
                        preferencesManager.saveCustomerInfo(
                            CustomerInfo(name, email, phone, address1, address2, city, state, pincode, dttmOfBirth, lat, lon)
                        )
                    }
                }

                @Composable
                fun input(label: String, value: String, onValueChange: (String) -> Unit) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue -> // Use newValue here
                            onValueChange(newValue.trim()) // <--- Apply .trim() here!
                            save()
                        },
                        label = { Text(label) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textStyle = LocalTextStyle.current.copy(color = Color.Black)
                    )
                }

                input("Name", name) { name = it }
                input("Email", email) { email = it }
                input("Phone ( whatsapp )", phone) { phone = it }
                input("Address Line 1", address1) { address1 = it }
                input("Address Line 2", address2) { address2 = it }
                input("City", city) { city = it }
                input("State", state) { state = it }
                input("Pincode", pincode) { pincode = it }
                // adding the horoscope related details as weall - 2nd feb 2026
                input("Latitude", lat) { lat = it }
                input("Longitude", lon) { lon = it }

//                birthDateTime = parseDob(customerInfo.dttmOfBirth, formatter)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Date and Time of Birth",
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )

                    DateTimePicker(birthDateTime) {
                        birthDateTime = it
                        dttmOfBirth = it.format(formatter)
                        save()
                    }
                }
            }

            if (saveMessage.isNotEmpty()) {
                Text(saveMessage, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    value: LocalDateTime,
    onChange: (LocalDateTime) -> Unit
) {
    val context = LocalContext.current

    OutlinedButton(onClick = {
        val now = value

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->

                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onChange(
                            now.withYear(year)
                                .withMonth(month + 1)
                                .withDayOfMonth(dayOfMonth)
                                .withHour(hour)
                                .withMinute(minute)
                        )
                    },
                    now.hour,
                    now.minute,
                    true
                ).show()

            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        ).show()
    }) {
        Text(value.format(dttmFormatter))
    }
}

//fun parseDob(value: String, formatter: DateTimeFormatter): LocalDateTime =
//    try {
//        LocalDateTime.parse(value, formatter)
//    } catch (e: Exception) {
//        LocalDateTime.now()
//    }


// --- Helper Card Composable for section styling (Unchanged) ---
@Composable
fun SectionCard(background: Color, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = background,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
