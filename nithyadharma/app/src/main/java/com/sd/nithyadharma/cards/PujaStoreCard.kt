package com.sd.nithyadharma.cards

import LocaleManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.delay
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.nithyadharma.model.CustomerInfo
import com.sd.nithyadharma.model.Order
import com.sd.nithyadharma.model.Product
import com.sd.nithyadharma.util.Constants.NITHYADHARMA_BUSINESS_NUMBER
import com.sd.nithyadharma.util.Constants.NITHYADHARMA_BUSINESS_UPI
import com.sd.nithyadharma.util.Constants.products
import com.sd.nithyadharma.util.LocalAppLanguage
import com.sd.nithyadharma.util.WhatsAppUtils
import kotlinx.coroutines.launch

// --- 2. CARD CONTENT COMPOSABLE ---
@Composable
fun PujaStoreCardContent(
    textColor: Color,
    customerInfo: CustomerInfo?,
    onSaveCustomerInfo: (CustomerInfo) -> Unit
) {
    val currentLang = LocalAppLanguage.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val coroutineScope = rememberCoroutineScope()

    // Form state variables pre-filled from DataStore
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address1 by remember { mutableStateOf("") }
    var address2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }

    // --- 2. Focus State (Where the cursor goes) ---
    val nameFocus = remember { FocusRequester() }
    val phoneFocus = remember { FocusRequester() }
    val address1Focus = remember { FocusRequester() }
    val cityFocus = remember { FocusRequester() }
    val stateFocus = remember { FocusRequester() }
    val pincodeFocus = remember { FocusRequester() }

    // Flag to ensure we only pre-fill once from DataStore load
    var isInitialDataLoaded by remember { mutableStateOf(false) }

    // Populate state once customerInfo is loaded from DataStore
    LaunchedEffect(customerInfo) {
        if (customerInfo != null && !isInitialDataLoaded) {
            name = customerInfo.name
            phone = customerInfo.phone
            address1 = customerInfo.address1
            address2 = customerInfo.address2
            city = customerInfo.city
            state = customerInfo.state
            pincode = customerInfo.pincode
            isInitialDataLoaded = true
        }
    }

    // Validation state
    var showErrors by remember { mutableStateOf(false) }

    // Cart state
    val cart = remember { mutableStateMapOf<Product, Int>() }
    var showConfirmation by remember { mutableStateOf(false) }

    // Cart calculations
    val cartPrice by remember(cart) {
        derivedStateOf {
            val subtotal = cart.entries.sumOf { (product, qty) -> product.price * qty }
            val discount = subtotal * 0.10 // 10% discount
            (subtotal - discount)
        }
    }

    val shippingCost = when {
        cartPrice == 0.0 -> 0
        cartPrice < 500.0 -> 70
        cartPrice < 1000.0 -> 110
        cartPrice < 2000.0 -> 200
        else -> 0 // Free shipping
    }

    // Helper to build updated CustomerInfo object
    fun buildCurrentCustomerInfo(): CustomerInfo {
        return CustomerInfo(
            name = name.trim(),
            email = customerInfo?.email ?: "",
            phone = phone.trim(),
            address1 = address1.trim(),
            address2 = address2.trim(), // Optional field
            city = city.trim(),
            state = state.trim(),
            pincode = pincode.trim(),
            dttmOfBirth = customerInfo?.dttmOfBirth ?: "",
            lat = customerInfo?.lat ?: "",
            lon = customerInfo?.lon ?: ""
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- Products List ---
        products.forEach { product ->
            val quantity = cart[product] ?: 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(textColor.copy(alpha = 0.08f))
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = product.imageRes),
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.DarkGray),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                        Text(
                            text = "₹${product.price}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = textColor.copy(alpha = 0.75f)
                            )
                        )
                    }

                    // Stepper Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.15f))
                                .clickable {
                                    if (quantity > 0) {
                                        cart[product] = quantity - 1
                                        if (cart[product] == 0) cart.remove(product)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = textColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            ),
                            modifier = Modifier.width(20.dp),
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.15f))
                                .clickable {
                                    cart[product] = quantity + 1
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = textColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- DELIMITER ---
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = textColor.copy(alpha = 0.20f)
        )

        // --- DELIVERY DETAILS SECTION ---
        Text(
            text = "Delivery Details",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )

        // Helper to keep textfields transparent with explicit text color
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            // Normal State
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = textColor.copy(alpha = 0.8f),
            unfocusedBorderColor = textColor.copy(alpha = 0.3f),
            focusedLabelColor = textColor,
            unfocusedLabelColor = textColor.copy(alpha = 0.7f),
            cursorColor = textColor,

            // Error State (Ensures text/cursor stays white/textColor instead of turning dark)
            errorTextColor = textColor,
            errorContainerColor = Color.Transparent,
            errorCursorColor = textColor,
            errorBorderColor = Color.Yellow, // was MaterialTheme.colorScheme.error,
            errorLabelColor = Color.Yellow // was MaterialTheme.colorScheme.error
        )
        // Phone validation logic (Strips spaces/hyphens and checks for valid 10-digit Indian mobile)
        val cleanPhone = phone.filter { it.isDigit() }
        val isPhoneValid = cleanPhone.length == 10 && cleanPhone.matches(Regex("^[6-9]\\d{9}$"))

        // Complete form validation check
        val isFormValid = name.isNotBlank() &&
                isPhoneValid &&
                address1.isNotBlank() &&
                city.isNotBlank() &&
                state.isNotBlank() &&
                pincode.isNotBlank()

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name *") },
            isError = showErrors && name.isBlank(),
            colors = textFieldColors,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(nameFocus) // 👈 Connects focus requester
        )

        // Phone (Fixed dark background & clean 10-digit handling)
        OutlinedTextField(
            value = phone,
            onValueChange = { input ->
                // Keep only digits and restrict to maximum 10 digits
                if (input.filter { it.isDigit() }.length <= 10) {
                    phone = input.filter { it.isDigit() }
                }
            },
            label = { Text("WhatsApp Phone (10 digits) *") },
            isError = showErrors && !isPhoneValid,
            colors = textFieldColors,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().focusRequester(phoneFocus) // 👈 Connects focus requester
        )

        // Address Line 1
        OutlinedTextField(
            value = address1,
            onValueChange = { address1 = it },
            label = { Text("Address Line 1 *") },
            isError = showErrors && address1.isBlank(),
            colors = textFieldColors,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(address1Focus) // 👈 Connects focus requester
        )

        // Address Line 2 (Optional)
        OutlinedTextField(
            value = address2,
            onValueChange = { address2 = it },
            label = { Text("Address Line 2 (Optional)") },
            colors = textFieldColors,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // City
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City *") },
            isError = showErrors && city.isBlank(),
            colors = textFieldColors,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(cityFocus) // 👈 Connects focus requester
        )

        // State
        OutlinedTextField(
            value = state,
            onValueChange = { state = it },
            label = { Text("State *") },
            isError = showErrors && state.isBlank(),
            colors = textFieldColors,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(stateFocus) // 👈 Connects focus requester
        )

        // Pincode
        OutlinedTextField(
            value = pincode,
            onValueChange = { pincode = it },
            label = { Text("Pincode *") },
            isError = showErrors && pincode.isBlank(),
            colors = textFieldColors,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().focusRequester(pincodeFocus) // 👈 Connects focus requester
        )

        // --- BOTTOM SUMMARY & SUBMIT ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.65f)) {
                Text(
                    text = "Cart: ₹${cartPrice.toInt()} | ${if (shippingCost == 0) "Free Delivery" else "Ship: ₹$shippingCost"}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = textColor.copy(alpha = 0.85f)
                    )
                )
                Text(
                    text = "Total: ₹${(cartPrice + shippingCost).toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        showErrors = false
                        val updatedInfo = buildCurrentCustomerInfo()
                        onSaveCustomerInfo(updatedInfo)
                        showConfirmation = true
                    } else {
                        showErrors = true

                        // 🟢 Clear focus and delay briefly so Compose finishes drawing error state
                        coroutineScope.launch {
                            focusManager.clearFocus() // Drops current active focus
                            delay(100) // Gives Compose ~6 frames to settle layout & error borders

                            when {
                                name.isBlank() -> nameFocus.requestFocus()
                                !isPhoneValid -> phoneFocus.requestFocus()
                                address1.isBlank() -> address1Focus.requestFocus()
                                city.isBlank() -> cityFocus.requestFocus()
                                state.isBlank() -> stateFocus.requestFocus()
                                pincode.isBlank() -> pincodeFocus.requestFocus()
                            }
                        }
                    }
                },
                enabled = cartPrice > 0,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = LocaleManager.getString("cmn_submit", currentLang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

//            Button(
//                onClick = {
//                    if (isFormValid) {
//                        showErrors = false
//                        val updatedInfo = buildCurrentCustomerInfo()
//
//                        // 🟢 Delegate save to ViewModel via callback
//                        onSaveCustomerInfo(updatedInfo)
//
//                        showConfirmation = true
//                    } else {
//                        showErrors = true
//                        // 🟢 Defer focus request until AFTER recomposition finishes drawing error borders
//                        coroutineScope.launch {
//                            kotlinx.coroutines.yield() // Waits 1 frame for UI recomposition
//
//                            when {
//                                name.isBlank() -> nameFocus.requestFocus()
//                                !isPhoneValid -> phoneFocus.requestFocus()
//                                address1.isBlank() -> address1Focus.requestFocus()
//                                city.isBlank() -> cityFocus.requestFocus()
//                                state.isBlank() -> stateFocus.requestFocus()
//                                pincode.isBlank() -> pincodeFocus.requestFocus()
//                            }
//                        }
//                    }
//                },
//                enabled = cartPrice > 0,
//                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//                shape = RoundedCornerShape(10.dp)
//            ) {
//                Text(
//                    text = LocaleManager.getString("cmn_submit", currentLang),
//                    fontSize = 13.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
        }
    }

    // --- CONFIRMATION DIALOG ---
    if (showConfirmation) {
        val currentInfo = buildCurrentCustomerInfo()

        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirm Order", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Customer Details - upgraded to bodyMedium & bodyLarge
                    Text(
                        text = "Customer: ${currentInfo.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Phone: ${currentInfo.phone}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Address: ${currentInfo.address1}${if (currentInfo.address2.isNotBlank()) ", ${currentInfo.address2}" else ""}, ${currentInfo.city}, ${currentInfo.state} - ${currentInfo.pincode}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Replaced deprecated Divider with HorizontalDivider
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Items:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    cart.forEach { (product, quantity) ->
                        Text(
                            text = "• ${product.name} x $quantity = ₹${(product.price * quantity).toInt()}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "10% automatic app discount applied on cart total",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Shipping: ${if (shippingCost == 0) "FREE" else "₹$shippingCost"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Total: ₹${(cartPrice + shippingCost).toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Please pay to ${NITHYADHARMA_BUSINESS_UPI} from any UPI app and WhatsApp ${NITHYADHARMA_BUSINESS_NUMBER} with reference number.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = false
                        val order = Order(
                            customer = currentInfo,
                            items = cart.toMap(),
                            shippingCost = shippingCost
                        )
                        WhatsAppUtils.apply {
                            context.sendWhatsAppOrderConfirmation(order = order)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009688),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm & Pay")
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