package com.sd.nithyadharma.util

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.sd.nithyadharma.MainActivity
import com.sd.nithyadharma.R
import androidx.core.graphics.createBitmap

class NDFirebaseMessagingService : FirebaseMessagingService() {

    // Called when a message is received.
    private val TAG = "NDFCMService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle incoming message here
        // remoteMessage.notification contains notification payload
        // remoteMessage.data contains data payload

        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if message contains a notification payload.
        // THIS BLOCK IS EXECUTED WHEN THE APP IS IN THE FOREGROUND AND GETS A NOTIFICATION
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification Body: ${notification.body}")
            Log.d(TAG, "Message Notification Title: ${notification.title}") // Also log the title

            // *** Call the sendNotification function here to display it ***
            // Pass the title and body from the received notification
            sendNotification(notification.title, notification.body)
        }

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            // Handle data messages (e.g., update UI, trigger background task)
        }

        // Messages with both notification and data payloads are handled first as notification,
        // then optionally by onMessageReceived if app is in foreground.
    }

    // Called when a new token for the default Firebase project is generated.
    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        // Send this token to your app server.
        // You'll need to associate this token with your user's account (e.g., using their Firebase Auth UID)
        // so you know which token to use when sending a message to a specific user.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     * This is typically called FROM onMessageReceived when the app is in the foreground
     * and receives a notification payload.
     */
    private fun sendNotification(notificationTitle: String?, notificationBody: String?) {
        // Intent to launch your main activity when the user taps the notification
        val intent = Intent(this, MainActivity::class.java).apply { // Replace MainActivity
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE is required
        )

        // Use the default channel ID you defined in AndroidManifest.xml and strings.xml
        val channelId = getString(R.string.default_notification_channel_id) // Gets the ID from strings.xml
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // *** Add a reference to your large icon drawable resource ***
        val largeIconDrawable = getDrawable(R.drawable.logo) // *** REPLACE with YOUR large icon resource ***

        // Convert the drawable to a Bitmap for setLargeIcon
        val largeIconBitmap: Bitmap? = (largeIconDrawable as? BitmapDrawable)?.bitmap
            ?: if (largeIconDrawable is VectorDrawableCompat || largeIconDrawable is VectorDrawable) {
                // Handle VectorDrawable case if your large icon is a vector
                createBitmap(largeIconDrawable.intrinsicWidth, largeIconDrawable.intrinsicHeight).apply {
                    val canvas = Canvas(this)
                    largeIconDrawable.setBounds(0, 0, canvas.width, canvas.height)
                    largeIconDrawable.draw(canvas)
                }
            } else {
                null // Handle other drawable types if necessary, or provide error handling
            }

        // Build the notification using NotificationCompat.Builder for backward compatibility
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // *** REPLACE with YOUR small notification icon resource ***
            .setContentTitle(notificationTitle ?: "Nithya Dharma") // Use received title or default
            .setContentText(notificationBody ?: "You have a new message") // Use received body or default
            .setAutoCancel(true) // Automatically dismisses the notification
            .setSound(defaultSoundUri) // Default sound
            .setContentIntent(pendingIntent) // What happens when tapped
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Priority for older Android

        // *** ADD THIS LINE to set the Large Icon ***
        if (largeIconBitmap != null) {
            notificationBuilder.setLargeIcon(largeIconBitmap)
        }
        // Get the NotificationManager system service
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // *** Create the NotificationChannel for Android 8.0 and higher ***
        // Best practice: Create this channel ONCE in your Application class's onCreate.
        // Including it here for completeness, but move this to your Application class for production.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.default_notification_channel_name) // From strings.xml
            val channelDescription = "Default channel for general app notifications" // Add this string resource
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                // Further configure channel behavior (vibration, lights, etc.)
            }
            // Register the channel
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created (or updated): $channelId")
        }

        // Show the notification using a unique ID
        val notificationId = System.currentTimeMillis().toInt() // Use a unique ID
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Foreground notification displayed with ID: $notificationId")
    }
}
