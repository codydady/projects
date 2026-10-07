package com.sd.nithyadharma.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sd.nithyadharma.model.HoroscopeAttr.HoroscopeInputParams

object FirebaseAppAnalytics {

    // Get the Firebase Analytics instance
    // 💡 Lazy delegate: Initialized on first call, cached thereafter
    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    private var fbUserName: String = "" // initial dummy name

    // for firebase
    fun setUser(userName: String) {
        val generatedId = SimpleIdCipher.encrypt(userName)

        // this is for firebase purposes and the func is provided by firebase
        firebaseAnalytics.setUserId(generatedId)
        // this is for our firebase db stores and the func is provided by us
        fbUserName = userName
    }

    // Log a screen view event
    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logCardLiked(analyticsKey: String, loggingKey: String) {
        // Add real-time write
        val likeData = hashMapOf(
            "user" to fbUserName,
            "cardType" to analyticsKey,
            "contentId" to loggingKey,
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        Firebase.firestore.collection("likes").add(likeData)
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

}
