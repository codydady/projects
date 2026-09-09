package com.sd.nithyadharma.util

import androidx.compose.ui.graphics.Color
import com.sd.nithyadharma.R
import com.sd.nithyadharma.model.Product
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Constants {
//    const val SUPER_USER = "sriram" // more beta features
//    const val OTHER_USER = "others" // or it is others
//    const val CURRENT_USER = SUPER_USER // more beta features
    const val PAYING_CUSTOMER = false // stable version

    const val APP_VERSION = 14

    val INDIA_ZONE = ZoneId.of("Asia/Kolkata")
    val UTC_ZONE = ZoneOffset.UTC

    /* TODO , this geo pos area needs to be an enum where user
         can enter his/her town to get accurate sunrise, sunet , panchanga data
     */

    // Chennai Coordinates (approximate, you might want more precise ones)
    const val LATITUDE_CHENNAI = 13.0827 // North
    const val LONGITUDE_CHENNAI = 80.2707 // East
    const val ALTITUDE_CHENNAI = 7.0 // meters above sea level (approx)

    val GEO_POS_CHENNAI = doubleArrayOf(LONGITUDE_CHENNAI, LATITUDE_CHENNAI, ALTITUDE_CHENNAI)

    const val RADIUS_MILES = 3.0
    const val LOCATION_UPDATE_INTERVAL_MS = 300000L  // once every 5 minutes
    const val LOCATION_MIN_DISTANCE_METERS = 5000f   // 5 kms or 5000 ft = 1 km ?
    const val CHIME_COOLDOWN_MS = 10000L

    const val NOTIFICATION_SCHEDULE_HOUR = 6    // for alarms to work
    const val NOTIFICATION_SCHEDULE_MINUTE = 1

    const val RASI_DEGREES_PER_SEGMENT = 30
    const val YOGA_DEGREES = 13.333333333333334

    const val DEFAULT_MAP_ZOOM_LEVEL = 16.0

    const val FUTURE_PANCHANGAM_CALCULATION_DAYS = 7

    val dttmFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm")

    const val DEFAULT_ALERT_INTERVAL = 36
    const val DEFAULT_FINAL_COUNT = 1008
    const val DEFAULT_EARLY_REMINDER_DAYS = 2

    const val TABLE_NAME = "mobile_app_temples" // ✅ Use this constant everywhere

    const val NITHYADHARMA_BUSINESS_NUMBER = "7695803124" // Replace with your number
    const val NITHYADHARMA_BUSINESS_UPI = "templepages@upi"
//    const val NITHYADHARMA_TRUST_UPI = "nithyadharma@upi"

//    const val PANCHANGAM_REFRESH_INTERVAL = 60 * 60 * 1000L // one hour in milliseconds

    // card related constants
    val Ivory = Color(0xFFFFFFF0)
    val DarkIvory = Color(0xFFF8EAA8)

    // Product data
    val products =
        listOf(
            Product("Chandanam (10 gms)", 130, R.drawable.chandanam),
            Product("Sambrani (30 cups)", 320, R.drawable.sambrani),
            Product("Vibhuthi (1 kg)", 340, R.drawable.vibhuthi),
            Product("Kungumam (50 gms)", 80, R.drawable.kunkumam) ,
            Product("Vibhuthi (100 gms)", 70, R.drawable.vibhuthi)
        )


    // --- card color JSON CONFIGURATION ---
    const val CARD_TIME_SLOTS = """
    [
        {
            "slotId": "DAWN",
            "startTime": "04:50",
            "endTime": "07:20",
            "gradientColorHex2": "0xFF163A24", 
            "gradientColorHex1": "0xFF52B788",
            "textColorHex": "0xFFE8F5E9"
        },
        {
            "slotId": "MORNING",
            "startTime": "07:20",
            "endTime": "12:10",
            "gradientColorHex2": "0xFF4D3319",
            "gradientColorHex1": "0xFFD2A679",
            "textColorHex": "0xFFFFF8DC"
        },
        {
            "slotId": "NOON",
            "startTime": "12:10",
            "endTime": "17:50",
            "gradientColorHex2": "0xFF5C0000",
            "gradientColorHex1": "0xFFFF5722",
            "textColorHex": "0xFFFFF8DC"
        },
        {
            "slotId": "EVENING",
            "startTime": "17:50", 
            "endTime": "21:10",
            "gradientColorHex2": "0xFF4A148C",
            "gradientColorHex1": "0xFFCE93D8",
            "textColorHex": "0xFFFFFFFF"
        },
        {
            "slotId": "NIGHT",
            "startTime": "21:10",
            "endTime": "04:50",
            "gradientColorHex2": "0xFF12233A",
            "gradientColorHex1": "0xFF315A8F",
            "textColorHex": "0xFFE0E1DD"
        }
    ]
    """
}
