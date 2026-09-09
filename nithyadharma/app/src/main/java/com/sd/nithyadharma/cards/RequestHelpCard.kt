package com.sd.nithyadharma.cards

import LocaleManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.nithyadharma.model.CustomerInfo
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.CommonFunctions.generateNDRId
import com.sd.nithyadharma.util.Constants.NITHYADHARMA_BUSINESS_NUMBER
import com.sd.nithyadharma.util.LocalAppLanguage
import com.sd.nithyadharma.util.WhatsAppUtils

@Composable
fun RequestHelpCardContent(
    textColor: Color,
    customerInfo: CustomerInfo?
) {
    val currentLang = LocalAppLanguage.current
    val context = LocalContext.current

    // Form field states
    var templeName by rememberSaveable { mutableStateOf("") }
    var templeLocation by rememberSaveable { mutableStateOf("") }
    var requirement by rememberSaveable { mutableStateOf("") }

    // Dialog & Validation Error states
    var showConfirmation by remember { mutableStateOf(false) }
    var validationErrorKey by remember { mutableStateOf<String?>(null) }

    val isNameMissing = customerInfo?.name.isNullOrBlank()

    fun getMissingFieldErrorKey(): String? {
        return when {
            templeName.isBlank() -> "err_enter_temple_name"
            templeLocation.isBlank() -> "err_enter_location"
            requirement.isBlank() -> "err_enter_requirement"
            else -> null
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = textColor,
        unfocusedTextColor = textColor,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = textColor.copy(alpha = 0.30f),
        cursorColor = textColor,
        focusedBorderColor = textColor,
        unfocusedBorderColor = textColor.copy(alpha = 0.5f),
        focusedLabelColor = textColor,
        unfocusedLabelColor = textColor.copy(alpha = 0.7f),
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error
    )

    val compactInputStyle = LocalTextStyle.current.copy(
        fontSize = 11.sp,
        lineHeight = 14.sp
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // --- Field 1: Temple Name ---
        Column(modifier = Modifier.fillMaxWidth()) {
            FieldTooltipError(
                visible = validationErrorKey == "err_enter_temple_name",
                errorKey = "err_enter_temple_name",
                currentLang = currentLang
            )
            OutlinedTextField(
                value = templeName,
                onValueChange = {
                    templeName = it
                    if (validationErrorKey != null) validationErrorKey = getMissingFieldErrorKey()
                },
                label = { Text(LocaleManager.getString("err_enter_temple_name", currentLang), fontSize = 11.sp) },
                textStyle = compactInputStyle,
                isError = validationErrorKey == "err_enter_temple_name",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )
        }

        // --- Field 2: Temple Location ---
        Column(modifier = Modifier.fillMaxWidth()) {
            FieldTooltipError(
                visible = validationErrorKey == "err_enter_location",
                errorKey = "err_enter_location",
                currentLang = currentLang
            )
            OutlinedTextField(
                value = templeLocation,
                onValueChange = {
                    templeLocation = it
                    if (validationErrorKey != null) validationErrorKey = getMissingFieldErrorKey()
                },
                label = { Text(LocaleManager.getString("err_enter_location", currentLang), fontSize = 11.sp) },
                textStyle = compactInputStyle,
                isError = validationErrorKey == "err_enter_location",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )
        }

        // --- Field 3: Requirement ---
        Column(modifier = Modifier.fillMaxWidth()) {
            FieldTooltipError(
                visible = validationErrorKey == "err_enter_requirement",
                errorKey = "err_enter_requirement",
                currentLang = currentLang
            )
            OutlinedTextField(
                value = requirement,
                onValueChange = {
                    requirement = it
                    if (validationErrorKey != null) validationErrorKey = getMissingFieldErrorKey()
                },
                label = { Text(LocaleManager.getString("err_enter_requirement", currentLang), fontSize = 11.sp) },
                textStyle = compactInputStyle,
                isError = validationErrorKey == "err_enter_requirement",
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                colors = textFieldColors
            )
        }

        // --- Bottom Action Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isNameMissing) {
                    LocaleManager.getString("rh_bottom", currentLang)
                } else {
                    "Name: ${customerInfo.name}"
                },
                color = if (isNameMissing) MaterialTheme.colorScheme.error else textColor.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 11.sp,
                modifier = Modifier.weight(0.65f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val missingError = getMissingFieldErrorKey()
                    if (missingError != null) {
                        validationErrorKey = missingError
                    } else {
                        validationErrorKey = null
                        showConfirmation = true
                    }
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = LocaleManager.getString("cmn_submit", currentLang),
                    fontSize = 11.sp
                )
            }
        }
    }

    // --- Confirmation Dialog ---
    if (showConfirmation) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirm Request", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Temple: $templeName", fontWeight = FontWeight.SemiBold)
                    Text("Location: $templeLocation")
                    Text("Requirement: $requirement")

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Nithyadharma trust verifies requests and then provides necessary items for rural temples. Request only for your temple.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Confirm opens WhatsApp to ${NITHYADHARMA_BUSINESS_NUMBER}. Click 'Send' in WhatsApp to complete the message.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val whatsappMessage = """
                            🪔 NithyaDharma New Request: 🪔
                            ----------------------
                            *Request Id*: ${generateNDRId()}
                            *Temple*: $templeName
                            *Location*: $templeLocation
                            *Requirement*: $requirement
                            ---------------
                            From: ${customerInfo?.name}
                            Phone: ${customerInfo?.phone}
                        """.trimIndent()

                        WhatsAppUtils.apply {
                            context.sendMessage(
                                message = whatsappMessage
                            )
                        }
                        templeName = ""
                        templeLocation = ""
                        requirement = ""
                        validationErrorKey = null
                        showConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009688),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmation = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Edit Details")
                }
            }
        )
    }
}

// Tooltip banner displayed directly ABOVE the field box
@Composable
private fun FieldTooltipError(
    visible: Boolean,
    errorKey: String,
    currentLang: NDLanguage
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
            modifier = Modifier.padding(start = 6.dp, bottom = 1.dp)
        ) {
            Text(
                text = "⚠️ ${LocaleManager.getString(errorKey, currentLang)}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}