package com.sd.nithyadharma.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
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
    var confirmFeedbackTxt by rememberSaveable { mutableStateOf("") }
    // --- END DIALOG CONTROL STATES ---

    // Form field states
    var feedbackTxt by rememberSaveable { mutableStateOf("") }

    val requestorNameDisplay = if (customerInfo.name.isNullOrBlank()) {
        LocaleManager.getString("fb_bottom", currentLang)
    } else {
        "Name: " + customerInfo.name
    }

    val nameColor = if (customerInfo.name.isNullOrBlank()) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                title = { Text(LocaleManager.getString("btn_fb", currentLang)) },
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
//                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: to alert about missing name or phone
                    Column(modifier = Modifier.weight(0.7f)) {

                        Text(
                            requestorNameDisplay,
                            color = nameColor,
                            style = MaterialTheme.typography.bodySmall

//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Column 2: for submit button to save space
                    Column(
                        modifier = Modifier.weight(0.3f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                confirmRequestorName = requestorNameDisplay // Use the displayed name
                                confirmFeedbackTxt = feedbackTxt
                                showConfirmation = true // Trigger the dialog
                            },
                            enabled = customerInfo.name.isNotBlank() && feedbackTxt.isNotBlank(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),  // ← Override default padding
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(LocaleManager.getString("cmn_submit", currentLang))
                        }
                    }
                }
            }
        },
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
//                        .background(backgroundColor) // Secondary safety layer
                        .animateContentSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.feedback),
                        contentDescription = "Ancient temple with oil lamp",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 1f) // More balanced ratio
                    )
                }

                Spacer(modifier = Modifier.height(20.dp)) // Space before form fields start

                OutlinedTextField(
                    value = feedbackTxt,
                    onValueChange = { feedbackTxt = it },
                    label = { Text("Kindly provide feedback about this app / இந்த மொபைல் ஆப்பில் என்ன முன்னேற்றங்களை காண விரும்புகிறீர்கள், பிடித்தது என்ன, குறைகள் என்ன என்று தெரியப்படுத்தவும்)") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp),
                    minLines = 12,
                    maxLines = 16,
//                colors = OutlinedTextFieldDefaults.colors(
//                        focusedTextColor = Color.Black,
//                        unfocusedTextColor = Color.Black,
//                        focusedContainerColor = Color.Transparent,
//                        unfocusedContainerColor = Color.Transparent,
//                        disabledContainerColor = Color.Transparent,
//                        cursorColor = Color.Black,
//                        focusedBorderColor = Color(0xFF6A8BAA),
//                        unfocusedBorderColor = Color(0xFFC47D5B),
//                        focusedLabelColor = Color(0xFF6A8BAA),
//                        unfocusedLabelColor = Color.DarkGray,
//                        errorBorderColor = Color.Red,
//                        errorLabelColor = Color.Red
//                    )
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,

                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,

                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
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

            onDismissRequest = {
                // Dismiss the dialog if clicked outside or by system back press
                showConfirmation = false
            },
            title = { Text("Confirm Request", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    // Customer Info
                    Spacer(modifier = Modifier.height(8.dp))

//                    Text("Temple: ${confirmTempleName}", fontWeight = FontWeight.SemiBold)
//                    Text("Location: ${confirmTempleLocation}")
                    Text("Feedback: ${confirmFeedbackTxt}")

                    Spacer(modifier = Modifier.height(8.dp))

//                    Text(
//                        text = "Nithyadharma trust verifies requests and then provides necessary items for rural temples. Request only for your temple.",
////                        color = Color.Blue,
//                        modifier = Modifier.padding(horizontal = 2.dp)
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))

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
                            🪔 NithyaDharma Feedback 🪔
                            ------------------------------
                            From: ${confirmRequestorName}
                            Feedback: ${confirmFeedbackTxt}
                        """.trimIndent()

                        // Call your WhatsApp utility to send the message
                        WhatsAppUtils.apply {
                            context.sendMessage(
                                message = whatsappMessage
                            )
                        }

                        // Clear form fields after successful submission confirmation
                        feedbackTxt = ""
//                        templeLocation = ""
//                        requirement = ""

                        showConfirmation = false // Dismiss the dialog
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .padding(start = 4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFF009688),
//                        containerColor = MaterialTheme.colorScheme.primaryContainer,
//                        contentColor = Color.White
//                    ),
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
//                    colors = ButtonDefaults.textButtonColors(
//                        containerColor = Color(0xFFCDDC39),
//                        contentColor = Color.Black // Changed to Black for better contrast
//                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Edit Details")
                }
            }
        )
    }
}
