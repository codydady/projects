package com.sd.nithyadharma.util

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sd.nithyadharma.cards.CardFactory
import com.sd.nithyadharma.cards.CardMode
import com.sd.nithyadharma.cards.CardType
import com.sd.nithyadharma.dao.CardRepository
import com.sd.nithyadharma.dao.PostOfDayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class NDFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "NDFirebaseMessagingService"

    private val preferencesManager by lazy {
        PreferencesManager(applicationContext)
    }

    // Use a background CoroutineScope for async network calls and DB writes
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "onMessageReceived - From: ${remoteMessage.from}")

        // 1. Process Data Payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            val newSlug = remoteMessage.data["newslug"]

            if (!newSlug.isNullOrEmpty()) {
                handleNewSlug(newSlug)
            } else {
                Log.w(TAG, "Payload received, but 'newslug' was null or empty.")
            }
        }

        // this section is required only when
//        If you go into the Firebase Web Console and click "Send test message"
//        using a standard notification payload, your app will receive the event,
//        but remoteMessage.data will be empty, so no status bar notification will pop up.
//
        // 2. Process Notification Payload (Fallback when app is in foreground)
        remoteMessage.notification?.let { notification ->
//            Log.d(TAG, "Message Notification Body: ${notification.body}")
            // Only trigger if data payload didn't already process the notification
            if (remoteMessage.data.isEmpty()) {

                CoroutineScope(Dispatchers.IO).launch {
                    val notificationTitle = CommonFunctions.getLocaleAwareString("todays_dharma")

                    AlarmSlotNotificationHelpers.sendNotification(
                        applicationContext,
                        notificationTitle,
                        notification.body?.take(60) ?: "",
                        Random.nextInt(1, Int.MAX_VALUE)
                    )
                } // coroutine ends
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun handleNewSlug(slug: String) {
        Log.d(TAG, "handleNewSlug called")

        serviceScope.launch {
            try {
                // Initialize repository
                val repository = PostOfDayRepository()

                // Fetch post details directly using the newly added slug
                val post = repository.getPostBySlug(slug)

                // postExcerpt is required only for loggingkey in the firebase database logging.
                val postExcerpt = post?.title?.take(60) ?: ""

                val imageBase64 = post?.imageBytes?.let { bytes ->
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
                val notificationData = mapOf(
                    "postTitle" to JsonPrimitive(post?.title),
                    "postExcerpt" to JsonPrimitive(postExcerpt),
                    "postContent" to JsonPrimitive(post?.contentsMarkdown),
                    "postImage" to JsonPrimitive(post?.imageUrl), // todo may not be required as image bytes are available
                    "postImageBytes" to JsonPrimitive(imageBase64) // Stored as Base64 String
                )

                if (post != null) {
                    Log.d(TAG, "Post found: slug=${post.slug}, title=${post.title}, " +
                            "imageBytes=${post.imageBytes?.size ?: "null"} bytes, " +
                            "markdownLen=${post.contentsMarkdown.length}")

                    // Create and save the TODAYS_DHARMA card
                    val card = CardFactory.makeCard(
                        mode = CardMode.WRITE_FG,
                        type = CardType.TODAYS_DHARMA,
                        expiryOffsetMillis = CardFactory.CARD_EXPIRY_OFFSET_LONG,
                        customParams = notificationData
                    )
                    Log.d(TAG, "Card created")

                    CardRepository.saveAndAddCard(preferencesManager, card)
                    Log.d(TAG, "Card saved successfully")

                    // Decode raw bytes directly to Bitmap for system tray notification
                    val postImageBitmap = post.imageBytes?.let { bytes ->
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }

                    // Display system tray notification in foreground and background
                    CoroutineScope(Dispatchers.IO).launch {
                        val notificationTitle = CommonFunctions.getLocaleAwareString("todays_dharma")
                        AlarmSlotNotificationHelpers.sendNotification(
                            applicationContext,
                            notificationTitle,
                            postExcerpt,
                            Random.nextInt(1, Int.MAX_VALUE),
                            postImageBitmap // Pass Bitmap directly to BigPictureStyle
                        )
                    }

                } else {
                    Log.e(TAG, "Failed to fetch post details for slug: $slug")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling new slug notification", e)
            }
        }
    }

}
