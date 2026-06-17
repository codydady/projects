package com.sd.nithyadharma.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.compose.runtime.collectAsState

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.sd.nithyadharma.MainActivity
import com.sd.nithyadharma.R

import com.sd.nithyadharma.dao.DataRepository
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.PanchangamCalculator.calculateDynamicPanchangamDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.ZonedDateTime

/**
 * -----------------------------------------------------------
 * AlarmReceiver
 * This BroadcastReceiver is the execution point for AlarmManager.
 * It fetches data, shows the notification, and reschedules itself.
 * -----------------------------------------------------------
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "nithya_dharma_channel"
        private const val CHANNEL_NAME = "Daily Dharma Reminders"
        const val REQUEST_CODE = 100 // Unique ID for the PendingIntent
    }

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("AlarmReceiver", "Alarm triggered! Action: ${intent.action ?: "null"}")

        val preferencesManager = PreferencesManager(context)
        // this is required by the alarm manager

        val currentLang = getSelectedLanguageBlocking(preferencesManager)

        // VERY IMPORTANT for async work inside BroadcastReceiver
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val appContext = context.applicationContext

                // Ensure repository is initialized (process may be cold-started)
                DataRepository.initialize(appContext)
                // ✅ SAFE suspend call (no runBlocking)
                DataRepository.ensureScheduleLoaded()
                val scheduleItems = DataRepository.scheduleData.value

                val today = Calendar.getInstance().time
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                // section 1: show matching notifications from hindu daily calendar

                // Get the early reminder days from Preferences (non-blocking)
                val earlyReminderDays = PreferencesManager(appContext).getEarlyReminder().first()

                if (scheduleItems.isNotEmpty()) {
                    var notificationsShown = 0
                    val outputFormat = SimpleDateFormat("MMMM d", Locale.getDefault())

                    for (item in scheduleItems) {
                        val eventDate = sdf.parse(item.date) ?: continue

                        val todayLocal = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        val eventLocal = eventDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

                        val daysDiff = ChronoUnit.DAYS.between(todayLocal, eventLocal).toInt()

                        val title = when {
                            daysDiff == 0 -> LocaleManager.getString("cmn_todaysevent", currentLang)
                            daysDiff == 1 -> LocaleManager.getString("cmn_tomorrowevent", currentLang)
                            daysDiff in 2..earlyReminderDays && Constants.PAYING_CUSTOMER -> LocaleManager.getString("cmn_futureevent", currentLang)
                            else -> null
                        } ?: continue

//                        Log.d("------AlarmReceiver", "🔔 daysDiff=$daysDiff | ${item.occasionEn} on ${item.date}")

                        val formattedDate = outputFormat.format(eventDate)

                        var occasionDtl = "-"
                        val isTamil = currentLang == NDLanguage.TA
                        if (isTamil) {
                            occasionDtl = item.occasionTa
                        } else {
                            occasionDtl = item.occasionEn
                        }
                        val notificationText = "${occasionDtl} on $formattedDate"

                        val notificationId = Random.nextInt(1, Int.MAX_VALUE)

                        sendNotification(
                            appContext,
                            title,
                            notificationText,
                            notificationId
                        )
                        notificationsShown++
                    } // scheduled data hindu calendar loop ends

                    // section 2: if user is experiencing chandrashtama, show an alert as weall

                    // Get the user's janma rasi from Preferences (non-blocking)
                    val userRasi = PreferencesManager(appContext).getSelectedRasi().first()

                    val nowIst = LocalDateTime.now(ZoneId.of("Asia/Kolkata"))

                    // 3. Call the calculation function directly
                    val panchangamData = calculateDynamicPanchangamDetails(nowIst, userRasi, true)

                    if ( panchangamData.chandrashtamaRasi == userRasi) {
                        val title = LocaleManager.getString("str_crtoday", currentLang)
                        val notificationText = LocaleManager.getString("str_crdtl", currentLang)
                        val notificationId = Random.nextInt(1, Int.MAX_VALUE)

                        sendNotification(
                            appContext,
                            title,
                            notificationText,
                            notificationId
                        )
                        notificationsShown++
                    }
                    if (notificationsShown == 0) {
                        Log.d("AlarmReceiver", "No qualifying events today.")
                    }

                } else {
                    Log.w(
                        "AlarmReceiver",
                        "Schedule list empty AFTER ensureScheduleLoaded()"
                    )
                }

                // ✅ Always reschedule explicitly
                Log.d(
                    "AlarmReceiver",
                    "Rescheduling next daily alarm (forced)"
                )
                AlarmScheduler.scheduleNextDailyAlarm(
                    appContext,
                    forceReschedule = true
                )

            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Alarm processing failed", e)
            } finally {
                // CRITICAL: tells system we're done
                pendingResult.finish()
            }
        }
    }

    private fun sendNotification(context: Context, title: String, notificationText: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("AlarmReceiver", "Notification permission denied (Android 13+). Cannot show notification.")
            return
        }

        createNotificationChannel(context)

        // Intent to launch MainActivity with extra
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigateTo", "hinduCalendar") // Pass data to navigate to a specific screen
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, // Use applicationContext for consistency
            notificationId, // Use a unique request code for each pending intent if multiple notifications
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val largeIconBitmap: Bitmap? = try {
            ContextCompat.getDrawable(context, R.mipmap.ic_launcher)?.toBitmap()
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error loading large icon: ${e.message}", e)
            null
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIconBitmap)
            .setContentTitle(title)
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.d("AlarmReceiver", "Notification ID $notificationId issued for: $title")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to issue notification: ${e.message}", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Reminders for daily rituals and festival starts."
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

fun getSelectedLanguageBlocking(preferencesManager: PreferencesManager): NDLanguage {
    return runBlocking { preferencesManager.getSelectedLanguage().first() }
}

/**
 * -----------------------------------------------------------
 * AlarmScheduler
 * Helper object containing the AlarmManager scheduling functions.
 * -----------------------------------------------------------
 */
object AlarmScheduler {

    // Request code must match the one used in the PendingIntent
    private const val ALARM_REQUEST_CODE = AlarmReceiver.REQUEST_CODE

    fun ensureAlarmExists(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val existing = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (existing == null) {
            Log.i("AlarmScheduler", "No existing alarm. Scheduling first alarm.")
            scheduleNextDailyAlarm(context, forceReschedule = true)
        } else {
            Log.i("AlarmScheduler", "Alarm already exists. No action needed.")
        }
    }

    /**
     * Schedules the next exact alarm time for the target time defined in Constants.
     * * @param context Application context.
     * @param forceReschedule If true, cancels the existing alarm and sets a new one.
     * If false (default), it checks if the alarm exists and skips
     * rescheduling if it's already active (idempotent).
     */
    fun scheduleNextDailyAlarm(context: Context, forceReschedule: Boolean = false) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // --- IDEMPOTENT CHECK ---
        val existingIntent = Intent(context, AlarmReceiver::class.java)
        val alarmPendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            existingIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (alarmPendingIntent != null && !forceReschedule) {
            // Alarm already exists and we were not forced to reschedule (e.g., normal Activity startup).
            Log.i("AlarmScheduler", "Alarm already scheduled. Skipping new registration.")
            return
        }

        val pendingIntent = createPendingIntent(context)

        val nextAlarmTimeMillis = calculateNextAlarmTimeMillis(
            Constants.NOTIFICATION_SCHEDULE_HOUR,
            Constants.NOTIFICATION_SCHEDULE_MINUTE
        )

        val logTime = Calendar.getInstance().apply { timeInMillis = nextAlarmTimeMillis }
        Log.i("AlarmScheduler", "Attempting to schedule alarm for: ${logTime.time}")

        // Cancellation: Clear any existing alarm before setting a new one.
        // This is necessary whether we forced it (forceReschedule=true) or if it didn't exist (null check failed).
        alarmManager.cancel(pendingIntent)
        Log.i("AlarmScheduler", "Previous alarm cancelled to ensure a single, precise schedule.")

        // Use setExactAndAllowWhileIdle for high precision, overriding battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAlarmTimeMillis,
                pendingIntent
            )
        } else {
            // Use setExact for older devices
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                nextAlarmTimeMillis,
                pendingIntent
            )
        }

        Log.i("AlarmScheduler", "Next exact alarm successfully scheduled for: ${logTime.time}")
        // Toast.makeText(context, "Daily alarm set for $timeString.", Toast.LENGTH_LONG).show()
    }

    /**
     * Calculates the timestamp (in milliseconds) for the next occurrence of a given hour and minute.
     */
    private fun calculateNextAlarmTimeMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val nextAlarm = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the target time has already passed today, set it for tomorrow
        if (nextAlarm.before(now)) {
            nextAlarm.add(Calendar.DAY_OF_YEAR, 1)
        }

        return nextAlarm.timeInMillis
    }

    /**
     * Creates the PendingIntent that AlarmManager will use to trigger the AlarmReceiver.
     */
    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        // FLAG_IMMUTABLE is required starting from API 31
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            flags
        )
    }

}