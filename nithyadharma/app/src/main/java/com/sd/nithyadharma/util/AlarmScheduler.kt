package com.sd.nithyadharma.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import java.util.Calendar

/**
 * Enum defining all distinct alarm categories and their base request codes.
 */
enum class AlarmType(val requestCode: Int) {
    DAILY_SCHEDULE(1001),
    DP_EXPIRY(1002),
    SLOT_CHANGE(1003),
    RANGE_RANDOM(1004)
}

object AlarmScheduler {

    const val EXTRA_ALARM_TYPE = "EXTRA_ALARM_TYPE"
    const val EXTRA_CARD_ID = "EXTRA_CARD_ID"

    /**
     * Call this during app startup or phone boot to ensure base alarms are set.
     */
    fun bootstrapAppAlarms(context: Context,
                           initialExpiryEpochMs: Long? = null,
                           initialSlotEpochMs: Long? = null) {
        // 1. Schedule Daily 6:00 AM Alarm
        scheduleDailyMorningFixedAlarm(context)

        // 2. Schedule Initial Dynamic Panchangam Expiry if timestamp provided
        initialExpiryEpochMs?.let { expiry ->
            schedulePanchangamExpiry(context, expiry)
        }

        // 3. Schedule Initial Slot Change Boundary if timestamp provided
        initialSlotEpochMs?.let { slotBoundary ->
            scheduleSlotChange(context, slotBoundary)
        }
    }

    fun scheduleDailyMorningFixedAlarm(context: Context) {
        val next6AMMillis = calculateNextStaticPanchAndNotificatonMillis()
        Log.d("Alarmscheduler", "Static Panchangam next at ${next6AMMillis}")

        scheduleExact(context, AlarmType.DAILY_SCHEDULE, next6AMMillis)
    }

    fun schedulePanchangamExpiry(context: Context, expiryEpochMs: Long) {
        scheduleExact(context, AlarmType.DP_EXPIRY, expiryEpochMs)
    }

    fun scheduleSlotChange(context: Context, triggerEpochMs: Long) {
        scheduleExact(context, AlarmType.SLOT_CHANGE, triggerEpochMs)
    }

    fun scheduleCardReminder(context: Context, cardId: String, triggerEpochMs: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_TYPE, AlarmType.RANGE_RANDOM.name)
            putExtra(EXTRA_CARD_ID, cardId)
        }
        val uniqueRequestCode = AlarmType.RANGE_RANDOM.requestCode + cardId.hashCode()
        scheduleExactWithIntent(context, uniqueRequestCode, intent, triggerEpochMs)
    }

    fun cancelCardReminder(context: Context, cardId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val uniqueRequestCode = AlarmType.RANGE_RANDOM.requestCode + cardId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleExact(context: Context, alarmType: AlarmType, triggerEpochMs: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_TYPE, alarmType.name)
        }
        scheduleExactWithIntent(context, alarmType.requestCode, intent, triggerEpochMs)
    }

    private fun scheduleExactWithIntent(context: Context, requestCode: Int, intent: Intent, triggerEpochMs: Long) {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            triggerEpochMs,
            pendingIntent
        )
    }

    private fun calculateNextStaticPanchAndNotificatonMillis(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, Constants.NOTIFICATION_SCHEDULE_HOUR)
            set(Calendar.MINUTE, Constants.NOTIFICATION_SCHEDULE_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}