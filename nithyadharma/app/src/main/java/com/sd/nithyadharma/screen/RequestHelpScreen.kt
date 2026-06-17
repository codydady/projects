package com.sd.nithyadharma.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import java.util.UUID // Import the UUID class

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.nithyadharma.R
import com.sd.nithyadharma.model.CustomerInfo
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.Constants.NITHYADHARMA_BUSINESS_NUMBER
import com.sd.nithyadharma.util.PreferencesManager
import com.sd.nithyadharma.util.WhatsAppUtils // Assuming this exists
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestHelpScreen(
    preferencesManager: PreferencesManager,
    onBackClick: () -> Unit) {

    val scrollState = rememberScrollState()

    val currentLang by preferencesManager.getSelectedLanguage()
        .collectAsState(initial = NDLanguage.EN)

    val imageShape = RoundedCornerShape(16.dp) // Define rounded corners
    val context = LocalContext.current // Get context in Composable scope
    val preferencesManager = remember { PreferencesManager(context) }
    val customerInfoFlow = preferencesManager.getCustomerInfo()
    val customerInfo by customerInfoFlow.collectAsState(initial = CustomerInfo("", "", "", "", "", "", "", "", "", "", ""))

    // --- DIALOG CONTROL STATES ---
    var showConfirmation by remember { mutableStateOf(false) } // Controls AlertDialog visibility

    // States to hold the data to display in the confirmation dialog
    var confirmRequestorName by rememberSaveable { mutableStateOf("") }
    var confirmRequestorPhone by rememberSaveable { mutableStateOf("") }
    var confirmRequestorEmail by rememberSaveable { mutableStateOf("") }
    var confirmTempleName by rememberSaveable { mutableStateOf("") }
    var confirmTempleLocation by rememberSaveable { mutableStateOf("") }
    var confirmRequirement by rememberSaveable { mutableStateOf("") }
    // --- END DIALOG CONTROL STATES ---

    // Form field states
    var templeName by rememberSaveable { mutableStateOf("") }
    var templeLocation by rememberSaveable { mutableStateOf("") }
    var requirement by rememberSaveable { mutableStateOf("") }

    val requestorNameDisplay = if (customerInfo.name.isNullOrBlank()) {
        LocaleManager.getString("rh_bottom", currentLang)
    } else {
        "Name: ${customerInfo.name}"
    }

    val nameColor = if (customerInfo.name.isNullOrBlank()) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val requestorPhoneNumberDisplay = if (customerInfo.phone.isNullOrBlank()) {
        "Must be a whatsapp number. (Go to preferences)"
    } else {
        customerInfo.phone
    }
//    val phoneNumberColor = if (customerInfo.phone.isNullOrBlank()) {
//        Color.Red
//    } else {
//        Color.Black
//    }
    // email is hidden for 2 reasons. one is space and other is for them to not know it is being sent
    val requestorEmail: String by preferencesManager.getAndroidLoginEmail().collectAsState(
        initial = "Loading..."
    )
    Scaffold(
       // modifier = Modifier.background(backgroundColor), // Set scaffold background
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF1E1B4)),
                title = { Text(LocaleManager.getString("btn_rh", currentLang)) },
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
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: to alert about missing name or phone
                    Column(modifier = Modifier.weight(0.7f)) {

                        Text(
                            text = requestorNameDisplay,
                            color = nameColor,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
//                        Text(
//                            "Phone: $requestorPhoneNumberDisplay",
//                            color = phoneNumberColor,
//                            modifier = Modifier.fillMaxWidth()
//                        )
                    }

                    // Column 2: for submit button to save space
                    Column(
                        modifier = Modifier.weight(0.3f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                // --- Capture data for the dialog and show it ---
                                confirmRequestorName = requestorNameDisplay // Use the displayed name
                                confirmRequestorPhone = requestorPhoneNumberDisplay
                                confirmRequestorEmail = requestorEmail // Use actual data
                                confirmTempleName = templeName
                                confirmTempleLocation = templeLocation
                                confirmRequirement = requirement
                                showConfirmation = true // Trigger the dialog
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                            enabled = customerInfo.name.isNotBlank() && templeName.isNotBlank()
                                    && templeLocation.isNotBlank() && requirement.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = LocaleManager.getString("cmn_submit", currentLang)
                            )
                        }
                    }
                }
            }
        },
       // containerColor = backgroundColor // Ensure scaffold container matches
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally // This centers the 80% width inner Column
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.9f) // Use fillMaxWidth on the inner Column
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .clip(imageShape) // Apply rounded corners
                        .animateContentSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.requestoil),
                        contentDescription = "Ancient temple with oil lamp",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 1f) // More balanced ratio
                    )
                }

                Spacer(modifier = Modifier.height(8.dp)) // Space before form fields start

                OutlinedTextField(
                    value = templeName,
                    onValueChange = { templeName = it },
                    label = { Text("Temple Name (கோவில் பெயர்)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedBorderColor = Color(0xFF6A8BAA),
                        unfocusedBorderColor = Color(0xFFC47D5B),
                        focusedLabelColor = Color(0xFF6A8BAA),
                        unfocusedLabelColor = Color.DarkGray,
                        errorBorderColor = Color.Red,
                        errorLabelColor = Color.Red
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = templeLocation,
                    onValueChange = { templeLocation = it },
                    label = { Text("Place ( கோவில் உள்ள ஊரின் பெயர்)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedBorderColor = Color(0xFF6A8BAA),
                        unfocusedBorderColor = Color(0xFFC47D5B),
                        focusedLabelColor = Color(0xFF6A8BAA),
                        unfocusedLabelColor = Color.DarkGray,
                        errorBorderColor = Color.Red,
                        errorLabelColor = Color.Red
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = requirement,
                    onValueChange = { requirement = it },
                    label = { Text("Requirement (Oil, Vibhuthi, Puja help for Pradosham, Sashti, Chathuthi etc / எண்ணெய், விபூதி, பிரதோஷம், சதுர்த்தி, சஷ்டி நடக்க உதவி; இன்ன பிற)") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp),
                    minLines = 7,
                    maxLines = 10,
                colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedBorderColor = Color(0xFF6A8BAA),
                        unfocusedBorderColor = Color(0xFFC47D5B),
                        focusedLabelColor = Color(0xFF6A8BAA),
                        unfocusedLabelColor = Color.DarkGray,
                        errorBorderColor = Color.Red,
                        errorLabelColor = Color.Red
                    )
                )
//                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    // --- CONFIRMATION ALERT DIALOG (Now correctly placed) ---
    if (showConfirmation) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            iconContentColor = MaterialTheme.colorScheme.onSurface,
            onDismissRequest = {
                // Dismiss the dialog if clicked outside or by system back press
                showConfirmation = false
            },
            title = { Text("Confirm Request", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    // Customer Info
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Temple: ${confirmTempleName}", fontWeight = FontWeight.SemiBold)
                    Text("Location: ${confirmTempleLocation}")
                    Text("Requirement: ${confirmRequirement}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Nithyadharma trust verifies requests and then provides necessary items for rural temples. Request only for your temple.",
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Confirm opens WhatsApp to ${NITHYADHARMA_BUSINESS_NUMBER}. Click 'Send' in WhatsApp to complete the message.",
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Create the WhatsApp message
                        val whatsappMessage = """
                            🪔 NithyaDharma New Request: 🪔
                            ----------------------
                            *Request Id*: ${generateNDRId()}
                            *Temple*: ${confirmTempleName}
                            *Location*: ${confirmTempleLocation}
                            *Requirement*: ${confirmRequirement}
                            ---------------
                            From: ${confirmRequestorName}
                            Phone: ${confirmRequestorPhone}
                            Email: ${confirmRequestorEmail}
                        """.trimIndent()

                        // Call your WhatsApp utility to send the message
                        WhatsAppUtils.apply {
                            context.sendMessage(
                                message = whatsappMessage
                            )
                        }

                        // Clear form fields after successful submission confirmation
                        templeName = ""
                        templeLocation = ""
                        requirement = ""

                        showConfirmation = false // Dismiss the dialog
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .padding(start = 4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009688),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmation = false }, // Just dismiss the dialog
                    modifier = Modifier
                        .height(40.dp)
                        .padding(start = 4.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Edit Details")
                }
            }
        )
    }
}

fun generateNDRId(): String {
    // 1. Get current day of week (3-letter uppercase abbreviation)
    val dayOfWeek = LocalDate.now()
        .dayOfWeek
        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        .lowercase()  // "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"

    // 2. Generate UUID and take first 7 chars
    val uuid = UUID.randomUUID().toString()
    val uuidPart = uuid.take(9)  // e.g. "a1b2c3d"

    // 3. Interleave day-of-week chars into uuidPart at fixed (but not obvious) positions
    // Positions: 0, 3, 6 (spread out, not consecutive)
    val sb = StringBuilder(uuidPart)
    sb.setCharAt(1, dayOfWeek[0])   // 1st char → day[0] e.g. 'T' for Thu
    sb.setCharAt(3, dayOfWeek[1])   // 4th char → day[1] e.g. 'h'
    sb.setCharAt(6, dayOfWeek[2])   // 7th char → day[2] e.g. 'u'

    val finalId = sb.toString()

    println("Generated ID: $finalId (Day embedded: $dayOfWeek)")
    return finalId
}
