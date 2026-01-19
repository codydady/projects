package com.sd.nithyadharma.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object FirebaseAppAnalytics {

    // Get the Firebase Analytics instance
    // Using lazy delegate or getting it directly inside functions is fine
    // Let's get it when needed for simplicity in this example
    private val firebaseAnalytics: FirebaseAnalytics
        get() = Firebase.analytics

    // Log a screen view event
    fun logScreenView(screenName: String, screenClass: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
//        println("Logged screen view: $screenName (via AppAnalytics)") // Optional: for debugging
    }

    // Log the counter milestone event
    fun logCounterMilestone(currentCount: Int) {
        val bundle = Bundle().apply {
            putInt("final_count", currentCount) // Custom parameter
        }
        firebaseAnalytics.logEvent("counter_milestone", bundle) // Custom event name
//        println("Logged counter milestone: $currentCount (via AppAnalytics)") // Optional: for debugging
    }

    // You could add other analytics logging functions here too!
    // fun logButtonPressedEvent(buttonName: String) { ... }
}
