package com.sd.nithyadharma.util

import com.sd.nithyadharma.model.NDLanguage
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale
import java.util.UUID

object CommonFunctions {

    // directly refers to the variable in app class and not duplicate
    private lateinit var preferencesManager: PreferencesManager

    /**
     * Call this once in Application.onCreate() or MainActivity.onCreate()
     */
    fun init(prefManager: PreferencesManager) {
        preferencesManager = prefManager
    }

    private fun checkInitialized() {
        check(::preferencesManager.isInitialized) {
            "CommonFunctions is not initialized! Call CommonFunctions.init(preferencesManager) in your Application class."
        }
    }

    // 1. Suspend function for coroutines / background services
    suspend fun getCurrentLanguage(): NDLanguage {
        checkInitialized()
        return preferencesManager.getSelectedLanguage().first()
    }

    suspend fun getLocaleAwareString(key: String): String {
        return LocaleManager.getString(key, getCurrentLanguage() )
    }

    fun getCurrentTime(): LocalDateTime {
        return LocalDateTime.now(Constants.INDIA_ZONE)
    }

    fun getCurrentDate(): LocalDate {
        return LocalDate.now(Constants.INDIA_ZONE)
    }

    /**
     * Formats timestamp to "dd MMM, hh:mm a" (e.g., "26 Mar, 11:30 PM")
     */
    fun formatCardTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd MMM yy, hh:mm a", Locale.ENGLISH)
        return formatter.format(Date(timestamp))
    }

    // for uniq whatsapp id to see if they are doin it on the day they say they are
    // doin it
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

    fun calculateShippingCost(cartPrice: Double, pincode: String): Int {
        // Safely parse pincode string to Int (handles empty or invalid strings gracefully)
        val pinInt = pincode.trim().toIntOrNull()

        // Check if pincode falls within the local region range (600001 to 643253)
        val isLocalRegion = pinInt != null && pinInt in 600001..643253

        return if (isLocalRegion) {
            // Local Region Slabs (Tamil Nadu)
            when {
                cartPrice <= 0.0 -> 0
                cartPrice < 500.0 -> 70
                cartPrice < 1000.0 -> 160
                cartPrice < 2000.0 -> 220
                else -> 0 // Free shipping
            }
        } else {
            // National / Rest of Region Slabs (Adjust rates as needed)
            when {
                cartPrice <= 0.0 -> 0
                cartPrice < 500.0 -> 120
                cartPrice < 1000.0 -> 250
                cartPrice < 2000.0 -> 320
                else -> 0 // Free shipping
            }
        }
    }
    // todo should swissephemeris be initialised in once place may be here in init
}