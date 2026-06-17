package com.sd.nithyadharma.util

import com.sd.nithyadharma.R
import com.sd.nithyadharma.model.Product
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

object Constants {
//    const val SUPER_USER = "sriram" // more beta features
//    const val OTHER_USER = "others" // or it is others
//    const val CURRENT_USER = SUPER_USER // more beta features
    const val PAYING_CUSTOMER = true // stable version

    const val APP_VERSION = 10

    val INDIA_ZONE = ZoneId.of("Asia/Kolkata")
    val UTC_ZONE = ZoneOffset.UTC

    const val RADIUS_MILES = 3.0
    const val LOCATION_UPDATE_INTERVAL_MS = 300000L  // once every 5 minutes
    const val LOCATION_MIN_DISTANCE_METERS = 5000f   // 5 kms or 5000 ft = 1 km ?
    const val CHIME_COOLDOWN_MS = 10000L

    const val NOTIFICATION_SCHEDULE_HOUR = 6
    const val NOTIFICATION_SCHEDULE_MINUTE = 10

    const val RASI_DEGREES_PER_SEGMENT = 30
    const val YOGA_DEGREES = 13.333333333333334

    const val RAHU_YAMA_GULIKAN_NALLANERAM_SCHEDULE_HOUR = 4
    const val RAHU_YAMA_GULIKAN_NALLANERAM_SCHEDULE_MINUTE = 10

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

    // Product data
    val products =
        listOf(
            Product("Chandanam (10 gms)", 130, R.drawable.chandanam),
            Product("Sambrani (30 cups)", 320, R.drawable.sambrani),
            Product("Vibhuthi (1 kg)", 340, R.drawable.vibhuthi),
            Product("Kungumam (50 gms)", 80, R.drawable.kunkumam) ,
            Product("Vibhuthi (100 gms)", 70, R.drawable.vibhuthi)
        )

}
