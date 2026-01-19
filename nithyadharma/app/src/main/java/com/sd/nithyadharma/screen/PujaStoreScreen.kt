package com.sd.nithyadharma.screen

import androidx.compose.foundation.Image

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.sd.nithyadharma.model.CustomerInfo
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.model.Order
import com.sd.nithyadharma.model.Product
import com.sd.nithyadharma.util.PreferencesManager
import com.sd.nithyadharma.util.Constants.NITHYADHARMA_BUSINESS_NUMBER
import com.sd.nithyadharma.util.Constants.NITHYADHARMA_BUSINESS_UPI
import com.sd.nithyadharma.util.Constants.products
import com.sd.nithyadharma.util.WhatsAppUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PujaStoreScreen(
    preferencesManager : PreferencesManager,
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current

    val currentLang by preferencesManager.getSelectedLanguage()
        .collectAsState(initial = NDLanguage.EN)

    val requestorEmail: String by preferencesManager.getAndroidLoginEmail().collectAsState(
        initial = "Loading..."
    )
    val customerInfoFlow = preferencesManager.getCustomerInfo()
    val customerInfo by customerInfoFlow.collectAsState(initial = CustomerInfo("", "", "", "", "", "", "", ""))

    // Dialog control
    var showConfirmation by remember { mutableStateOf(false) }

    // Cart state management
    val cart = remember { mutableStateMapOf<Product, Int>() }

    val cartPrice by remember(cart) {
        derivedStateOf {
            val subtotal = cart.entries.sumOf { (product, qty) -> product.price * qty }
            val discount = subtotal * 0.10 // Calculate 10% discount
            (subtotal - discount) // Apply the discount
        }
    }

    val shippingCost = when {
        cartPrice == 0.0 -> 0 // Changed 0 to 0.0
        cartPrice < 500.0 -> 70 // Changed 500 to 500.0
        cartPrice < 1000.0 -> 110 // Changed 1000 to 1000.0
        cartPrice < 2000.0 -> 200 // Changed 2000 to 2000.0
        else -> 0 // Free shipping
    }
    // this is where the bundling of all needed items happen to be sent to whatsapp or email
    fun createOrder(): Order {
        return Order(
            customer = customerInfo,
            items = cart.toMap(), // Converts to immutable map
            shippingCost = shippingCost
        )
    }
    val requestorNameDisplay = if (customerInfo.name.isNullOrBlank()) {
        "Name must be set (Go to preferences)"
    } else {
        customerInfo.name
    }
//    val nameColor = if (customerInfo.name.isNullOrBlank()) {
//        Color.Red
//    } else {
//        Color.Black
//    }
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
    val phoneNumberColor = if (customerInfo.phone.isNullOrBlank()) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
//    val phoneNumberColor = if (customerInfo.phone.isNullOrBlank()) {
//        Color.Red
//    } else {
//        Color.Black
//    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                title = {
                    Column { // Wrap title and subtitle in a Column
                        Text(LocaleManager.getString("pp_title", currentLang))
                        Text(
                            LocaleManager.getString("pp_discount", currentLang),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }

            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: Cart Value + Shipping
                    Column(modifier = Modifier.weight(0.4f)) {
                        Text(
                            "Cart: ₹$cartPrice",
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (shippingCost == 0) "Shipping: FREE" else "Shipping: ₹$shippingCost",
                            fontSize = 14.sp
                        )
                    }

                    // Column 2: Total Amount
                    Column(
                        modifier = Modifier.weight(0.3f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Total",
                            fontSize = 14.sp
                        )
                        Text(
                            "₹${cartPrice + shippingCost}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Column 3: Proceed to Payment Button
                    Column(
                        modifier = Modifier.weight(0.3f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                showConfirmation = true
                            },
                            enabled = cartPrice > 0 && customerInfo.name.isNotBlank() && customerInfo.phone.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF009688),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),  // ← Override default padding
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = LocaleManager.getString("cmn_submit", currentLang))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Customer section
            item {
                Text(
                    LocaleManager.getString("pp_custdtls", currentLang),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp,
                        top = 8.dp, bottom = 8.dp, end = 0.dp)
                )
                Text(
                    "Name: $requestorNameDisplay",
                    color = nameColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
                Text(
                    "Phone: $requestorPhoneNumberDisplay",
                    color = phoneNumberColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
                Text(
                    "Email: $requestorEmail",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            // Products Section
            item {
                Text(
                    LocaleManager.getString("cmn_products", currentLang),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(products) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp), // Reduced vertical padding
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product Image (Added here)
                        Image(
                            painter = painterResource(id = product.imageRes),
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray), // Fallback if image missing
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold)
                            Text("₹${product.price}")
                        }

                        // Quantity controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    cart[product] = (cart[product] ?: 1) - 1
                                    if (cart[product] == 0) cart.remove(product)
                                }
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, "Decrease")
                            }

                            Text(
                                text = "${cart[product] ?: 0}",
                                modifier = Modifier.width(24.dp),
                                textAlign = TextAlign.Center,
                            )

                            IconButton(
                                onClick = { cart[product] = (cart[product] ?: 0) + 1 }
                            ) {
                                Icon(Icons.Default.Add, "Increase")
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmation) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            iconContentColor = MaterialTheme.colorScheme.onSurface,
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirm Order") },
            text = {
                Column {
                    // Customer Info
                    Text("Customer: ${customerInfo.name}")
                    Text("Phone: ${customerInfo.phone}")

                    Divider(Modifier.padding(vertical = 8.dp))

                    // Order Items
                    Text("Items:", fontWeight = FontWeight.Bold)

                    cart.forEach { (product, quantity) ->
                        Text("- ${product.name} x $quantity = ₹${product.price * quantity}")
                    }
                    Divider(Modifier.padding(vertical = 8.dp))
                    Text("Shipping:", fontWeight = FontWeight.Bold)
                    Text("- ${shippingCost} ")
                    Divider(Modifier.padding(vertical = 8.dp))

                    Text("Total: ₹${cartPrice + shippingCost} ", fontWeight = FontWeight.Bold)

                    Text("\nPlease pay to ${NITHYADHARMA_BUSINESS_UPI} from any UPI app and whatsapp " +
                            "${NITHYADHARMA_BUSINESS_NUMBER} with reference number")

                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = false

                        // make a CustomerOrderDetails object with cart , customer and total price
                        val order = createOrder()

                        // send a whatsapp order confirmation message
                        WhatsAppUtils.apply {
                            context.sendWhatsAppOrderConfirmation(
                                order = order
                            )
                        }
                        // Call your payment function or payment gateway here
//                        val order = CustomerOrderDetails(
//                            customerInfo = customerInfo,
//                            items = cart.toList(),
//                            totalAmount = totalPrice.toDouble()
//                        )
//                        initiatePayment(order)
                    },
                    enabled = !customerInfo.phone.isNullOrBlank() &&
                            customerInfo.phone.trim().length == 10 &&
                            customerInfo.phone.trim().matches(Regex("^[6-9]\\d{9}$")),
                    modifier = Modifier
                        .height(40.dp)
                        .padding(start = 4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp), // ← Internal text padding
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF009688),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Confirm & Pay")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmation = false },
                    modifier = Modifier
                        .height(40.dp)
                        .padding(start = 4.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFFB48805), // Orange
//                        contentColor = Color.White
//                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Edit Details")
                }
            }
        )// alert dialog ends
    }
}
