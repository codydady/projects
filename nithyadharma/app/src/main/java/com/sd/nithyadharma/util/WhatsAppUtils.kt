package com.sd.nithyadharma.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.sd.nithyadharma.model.CustomerInfo
import com.sd.nithyadharma.model.Order
import com.sd.nithyadharma.model.Product
import com.sd.nithyadharma.util.Constants.NITHYADHARMA_BUSINESS_NUMBER
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppUtils {

    fun Context.sendWhatsAppOrderConfirmation(
        order: Order
    ) {
        val orderMsg = createCustomerMessage(order.customer, order.items, order.shippingCost)
//        val dispatcherMsg = createDispatcherMessage(order.customer, order.items)

        sendMessage(orderMsg)
//        sendMessage(dispatcherMsg)
        // Send to business the full order detail
//        sendWhatsApp(NITHYADHARMA_BUSINESS_NUMBER, orderMsg)

    }

    fun Context.sendMessage(
        message: String
    ) {
        // Send to business the full order detail
        sendWhatsApp(NITHYADHARMA_BUSINESS_NUMBER, message)
    }

//    private fun createDispatcherMessage(
//        customer: CustomerInfo,
//        products: Map<Product, Int>
//    ): String {
//
//        val indentSpace = "          "
//
//        // Build the address section of the string first
//        val deliveryAddress = StringBuilder()
//        deliveryAddress.append("${indentSpace} ${customer.name}\n")
//        deliveryAddress.append("${indentSpace} ${customer.address1}, \n")
//
//        // Only append address2 if it's not null or blank
//        if (!customer.address2.isNullOrBlank()) {
//            deliveryAddress.append("${indentSpace} ${customer.address2}, \n")
//        }
//
//        deliveryAddress.append("${indentSpace} ${customer.city}\n")
//        deliveryAddress.append("${indentSpace} ${customer.state} - ${customer.pincode}\n")
//        deliveryAddress.append("${indentSpace} phone: ${customer.phone}")
//
//        return """
//        *Delivery Address:*
//        -------------------
//${deliveryAddress.toString()}
//
//        *Items Ordered:*
//        ----------------
//${
//            products.entries.withIndex().joinToString("\n") { (index, entry) ->
//                val (product, qty) = entry
//                val number = index + 1
//                "${indentSpace} • $number. ${product.name} (Qty: $qty) - ₹${product.price * qty}"
//            }
//        }
//
//        """.trimIndent()
//    }

    private fun createCustomerMessage(
        customer: CustomerInfo,
        products: Map<Product, Int>,
        shippingCost: Int
    ): String {
        val subtotal = products.entries.sumOf { (product, qty) -> product.price * qty }
        val total = subtotal + shippingCost
        val indentSpace = "          "

        // Build the address section of the string first
        val deliveryAddress = StringBuilder()
        deliveryAddress.append("${indentSpace} ${customer.name}\n")
        deliveryAddress.append("${indentSpace} ${customer.address1}, \n")

        // Only append address2 if it's not null or blank
        if (!customer.address2.isNullOrBlank()) {
            deliveryAddress.append("${indentSpace} ${customer.address2}, \n")
        }

        deliveryAddress.append("${indentSpace} ${customer.city}\n")
        deliveryAddress.append("${indentSpace} ${customer.state} - ${customer.pincode}\n")
        deliveryAddress.append("${indentSpace} phone: ${customer.phone}")

        return """
        *🪔 Nithyadharma Order Confirmation*
        -----------------------
        Thank you 🙏for your order!
        
        *Order ID:* ${System.currentTimeMillis()}
        *Name:* ${customer.name}
        *Date:* ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())}
        
        *Delivery Address:*
        -------------------
${deliveryAddress.toString()}
        
        *Items Ordered:*
        ----------------
${
            products.entries.withIndex().joinToString("\n") { (index, entry) ->
                val (product, qty) = entry
                val number = index + 1
                "${indentSpace} • $number. ${product.name} (Qty: $qty) - ₹${product.price * qty}"
            }
        }
        
        *Subtotal:* ₹$subtotal      ,*Shipping:* ₹$shippingCost
        *Total Amount:* ₹$total
        
        *Payment Status:* Waiting
        
        - Contact us: $NITHYADHARMA_BUSINESS_NUMBER
        - Visit: www.templepages.com
            
        """.trimIndent()
    }

    private fun Context.sendWhatsApp(number: String, message: String) {
        try {
            Log.d("--whatsapp--", "phone number " + number)

            val uri = Uri.parse("https://wa.me/$number?text=${URLEncoder.encode(message, "UTF-8")}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp") // Force WhatsApp only
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            // Fallback to browser
            val webUri = Uri.parse("https://web.whatsapp.com/send?phone=$number&text=${ URLEncoder.encode(message, "UTF-8") }")
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }
}