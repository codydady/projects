package com.sd.nithyadharma.util

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.sd.nithyadharma.model.HoroscopeAttr.HoroscopeInputParams

object FirebaseAppAnalytics {

    // Get the Firebase Analytics instance
    // 💡 Lazy delegate: Initialized on first call, cached thereafter
    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }

    // Log a screen view event
    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logCardLiked(analyticsKey: String, loggingKey: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, analyticsKey) // "card_type"
            putString(FirebaseAnalytics.Param.ITEM_ID, loggingKey)     // "content_id"
            //  putString("card_type", analyticsKey) // Unique, non-localized card identifier
            //  putString("content_id", loggingKey) // Optional detail parameter
        }
        firebaseAnalytics.logEvent("nd_card_liked", bundle)
//        Log.i("FirebaseAppAnalytics","Logged card_liked (via AppAnalytics)") // Optional: for debugging
    }

    fun logRating(analyticsKey: String, rating: Int) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, analyticsKey) // "card_type"
            putString(FirebaseAnalytics.Param.ITEM_ID, ""+rating)     // ""+ int makes string
            //  putString("card_type", analyticsKey) // Unique, non-localized card identifier
            //  putString("content_id", loggingKey) // Optional detail parameter
        }
        firebaseAnalytics.logEvent("nd_rating", bundle)
        Log.i("FirebaseAppAnalytics","Logged nd_rating") // Optional: for debugging
    }

    // Log the counter milestone event
    fun logCounterMilestone(currentCount: Int) {
        val bundle = Bundle().apply {
            putInt("final_count", currentCount) // Custom parameter
        }
        firebaseAnalytics.logEvent("nd_counter_milestone", bundle) // Custom event name
    }

    // Log the horoscope input params
    fun logHoroscopeInputs(horoscopeInputParams: HoroscopeInputParams) {
        val bundle = Bundle().apply {
            putString("name", horoscopeInputParams.name)
            putString("date", horoscopeInputParams.date.toString())
            putString("time", horoscopeInputParams.time.toString())
            putDouble("latitude", horoscopeInputParams.latitude)
            putDouble("longitude", horoscopeInputParams.longitude)
        }
        firebaseAnalytics.logEvent("nd_horoscope_details", bundle) // Custom event name
    }

    // You could add other analytics logging functions here too!
    // fun logButtonPressedEvent(buttonName: String) { ... }
}
