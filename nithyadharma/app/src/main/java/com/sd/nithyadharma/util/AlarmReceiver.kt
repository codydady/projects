package com.sd.nithyadharma.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sd.nithyadharma.dao.PanchangamRepository
import com.sd.nithyadharma.util.AlarmSlotNotificationHelpers.checkAndSendDailyHCNotifications
import com.sd.nithyadharma.util.AlarmSlotNotificationHelpers.checkDPForChandraashtamamAndSaveIfFound
import com.sd.nithyadharma.util.AlarmSlotNotificationHelpers.createDynamicPanchangamNotifications
import com.sd.nithyadharma.util.AlarmSlotNotificationHelpers.createSlotChangeCards
import com.sd.nithyadharma.util.Constants.INDIA_ZONE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered! Action: ${intent.action}")

        // 1. Extract Enum Type from Extra
        val typeName = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TYPE)
        val alarmType = typeName?.let { runCatching { AlarmType.valueOf(it) }.getOrNull() }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val appContext = context.applicationContext

                when (alarmType) {
                    AlarmType.DAILY_SCHEDULE -> handleDailySchedule(appContext)
                    AlarmType.DP_EXPIRY -> handleDynamicPanchangamExpiry(appContext)
                    AlarmType.SLOT_CHANGE -> handleSlotChange(appContext)
                    AlarmType.RANGE_RANDOM -> TODO()
                    null -> {
                        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                            handleBootCompleted(appContext)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Alarm execution error", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    // ----------------------------------------------------
    // 1. DAILY 6:00 AM SCHEDULE (Extracted existing logic)
    // ----------------------------------------------------

    private suspend fun handleDailySchedule(appContext: Context) {
        Log.d("AlarmReceiver", "Executing Daily Schedule Notifications...")

        val preferencesManager = PreferencesManager(appContext)

        // 1. handle notifications
        checkAndSendDailyHCNotifications(appContext, preferencesManager)

        // 2. calculate future panchangam once every day
        PanchangamRepository.refreshFuturePanchangam() // Recalculates 7-day outlook

        // 3.check chandrashtama todo
        checkDPForChandraashtamamAndSaveIfFound(appContext, preferencesManager)

        // Reschedule for NOTIFICATION_SCHEDULE_HOUR and minute tomorrow
        AlarmScheduler.scheduleDailyMorningFixedAlarm(appContext)
    }


    // ----------------------------------------------------
    // 2. DYNAMIC PANCHANGAM EXPIRY (Stub)
    // ----------------------------------------------------
    private suspend fun handleDynamicPanchangamExpiry(appContext: Context) {
        Log.d("AlarmReceiver", "alarm Handling Dynamic Panchangam Expiry...")

        /* imp this is function that is always executed from
             every alarm expiry and is the only way dp is refreshed
         */

        val nextDpRefreshDttm = createDynamicPanchangamNotifications(
            appContext = appContext
        )

        Log.d("AlarmReceiver", "in the alarm after add card")

        // 4. Convert expdttm to epoch milliseconds
        val expiryEpochMs = nextDpRefreshDttm
            .atZone(INDIA_ZONE)
            .toInstant()
            .toEpochMilli()

        // 5. schedule the next alarm based on expirydttm of the next immediate
        AlarmScheduler.schedulePanchangamExpiry(appContext, expiryEpochMs)
    }

    // ----------------------------------------------------
    // 3. SLOT CHANGE (Stub)
    // ----------------------------------------------------
    private suspend fun handleSlotChange(appContext: Context) {
        Log.d("AlarmReceiver", "Handling Slot Change...")

        val preferencesManager = PreferencesManager(appContext)

        val nextSlotBoundaryEpochMs = createSlotChangeCards(
            appContext = appContext,
            preferencesManager = preferencesManager
        )

        // schedule the next alarm
        AlarmScheduler.scheduleSlotChange(appContext, nextSlotBoundaryEpochMs)
    }

    // ----------------------------------------------------
    // 4. CARD REMINDER (Stub)
    // ----------------------------------------------------
    private suspend fun handleCardReminder(appContext: Context, intent: Intent) {
        val cardId = intent.getStringExtra(AlarmScheduler.EXTRA_CARD_ID) ?: return
        Log.d("AlarmReceiver", "Handling Card Reminder for ID: $cardId")

        // Fetch card metadata and send specific notification
    }

    // ----------------------------------------------------
    // 5. SYSTEM REBOOT RECOVERY
    // ----------------------------------------------------
    private suspend fun handleBootCompleted(appContext: Context) {
        Log.d("AlarmReceiver", "Device rebooted. Re-bootstrapping alarms...")
        AlarmScheduler.bootstrapAppAlarms(appContext)
    }

}