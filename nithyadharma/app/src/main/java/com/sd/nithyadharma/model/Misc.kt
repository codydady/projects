package com.sd.nithyadharma.model

import kotlinx.serialization.Serializable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sd.nithyadharma.util.Constants
import java.time.LocalDateTime

// for puja product order
data class Product(
    val name: String,
    val price: Int,
    val imageRes: Int // Add image resource ID
)
// for puja product order
data class Order(
    val customer: CustomerInfo,
    val items: Map<Product, Int>, // Product -> Quantity
    val shippingCost: Int
) {
    // Computed properties
    val subtotal: Int
        get() = items.entries.sumOf { (product, qty) -> product.price * qty }

    val total: Int
        get() = subtotal + shippingCost
}

// for puja product order & horoscope
data class CustomerInfo(
    val name: String,
    val email: String,
    val phone: String,
    val address1: String,
    val address2: String,
    val city: String,
    val state: String,
    val pincode: String,
    val dttmOfBirth: String,
    val lat: String,
    val lon: String
)

// for daily schedule
@Serializable
data class ScheduleItem(
    val date: String,
    val occasionEn: String,
    val occasionTa: String,
    val remarks: String
)

// for templemap - UPDATED FOR ROOM DATABASE
@Entity(tableName = Constants.TABLE_NAME) // Specify table name for Room
data class TempleItem(
    @PrimaryKey // Marks temple_id as the primary key for Room
    val temple_id: String, // Keeping the Int type for consistency with your current field
    val name: String,
    val deity: String,
    val latlong: String,
    val tags: String?,
    val place: String?,
    val weight: Int,
    var visit_dt: String?, // Made nullable for "not visited" state, as requested
    val marked: String?,
)

// Data class to hold all timings together (optional but useful)
data class BreathingTimings(
    val inhale: Float,
    val hold: Float,
    val exhale: Float,
    val pause: Float
)