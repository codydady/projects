package com.sd.nithyadharma.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.sd.nithyadharma.R
import org.osmdroid.util.GeoPoint

@SuppressLint("StaticFieldLeak")
object LocationTracker {
    private var fusedClient: FusedLocationProviderClient? = null
    private var lastKnownLocation: Location? = null
    private var context: Context? = null
    private var soundPlayed = false
    var onLocationChanged: ((GeoPoint) -> Unit)? = null  //callback function to register location change
    private var lastChimeTime = 0L

    fun initialize(appContext: Context) {
        context = appContext.applicationContext
        fusedClient = LocationServices.getFusedLocationProviderClient(context!!)
//        startTracking()  - not needed now and it may be in paid version
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, Constants.LOCATION_UPDATE_INTERVAL_MS)
            .setMinUpdateDistanceMeters(Constants.LOCATION_MIN_DISTANCE_METERS)
            .build()

        fusedClient?.requestLocationUpdates(request, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val newLocation = result.lastLocation ?: return

                val oldLocation = lastKnownLocation
                lastKnownLocation = newLocation

                if (oldLocation != null) {
                    val distance = oldLocation.distanceTo(newLocation)
                    if (distance > Constants.LOCATION_MIN_DISTANCE_METERS) {
                        Toast.makeText(context, "Moved more than 3m: starttracking", Toast.LENGTH_SHORT).show()
                        TTSManager.speak("you have moved to a new location, reloading map")

                        // play a music
                        playChime()

                        val geoPoint = GeoPoint(newLocation.latitude, newLocation.longitude)

                        // Trigger UI state change and it will render with new temples
                        // no need to call a reload temples function from here.
                        onLocationChanged?.invoke(geoPoint)

                        Log.d("LocationTracker", "--- Moved more than 3m: $distance")
                    }
                }
            }
        }, Looper.getMainLooper())
    }

    private fun playChime() {
        val now = System.currentTimeMillis()
        if (now - lastChimeTime < Constants.CHIME_COOLDOWN_MS) return // 10s cooldown

        if (soundPlayed) return
        soundPlayed = true

        context?.let {
            val mediaPlayer = MediaPlayer.create(it, R.raw.chime)
            if (mediaPlayer == null) {
                Log.e("LocationTracker", "Failed to create MediaPlayer for chime")
                soundPlayed = false
                return
            }

            mediaPlayer.setOnCompletionListener {
                it.release()
                soundPlayed = false
            }
            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Log.e("LocationTracker", "MediaPlayer error: $what, $extra")
                soundPlayed = false
                mp.release()
                true
            }
            mediaPlayer.start()
            lastChimeTime = now
        }
    }

    /**
     * Returns the current location as a GeoPoint via the callback.
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentLocation(
        onLocationReceived: (GeoPoint) -> Unit
    ) {
        fusedClient?.lastLocation?.addOnSuccessListener { location: Location? ->
            location?.let {
                val geoPoint = GeoPoint(it.latitude, it.longitude)
                Log.i("LocationTracker", "Got location: ${geoPoint.latitude}, ${geoPoint.longitude}")
                onLocationReceived(geoPoint)
            } ?: Log.w("LocationTracker", "Location was null.")
        } ?: Log.e("LocationTracker", "FusedClient was null.")
    }

    /**
     * Returns the last known location from tracking.
     */
    fun getLastKnownLocation(): Location? {
        return lastKnownLocation
    }

    /**
     * Simple permission check.
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
