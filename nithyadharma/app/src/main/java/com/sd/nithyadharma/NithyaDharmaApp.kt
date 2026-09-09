package com.sd.nithyadharma

import android.app.Application
import android.util.Log
import com.sd.nithyadharma.dao.AppDatabase
import com.sd.nithyadharma.dao.HinduCalendarRepository
import com.sd.nithyadharma.model.PanchangaAttr.DynamicPanchangam
import com.sd.nithyadharma.util.AlarmScheduler
import com.sd.nithyadharma.util.CommonFunctions
import com.sd.nithyadharma.util.Constants.INDIA_ZONE
import com.sd.nithyadharma.util.LocationTracker
import com.sd.nithyadharma.util.PanchangamCalculator
import com.sd.nithyadharma.util.PreferencesManager
import com.sd.nithyadharma.util.SlotManager
import com.sd.nithyadharma.util.TTSManager
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NithyaDharmaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // this line nukes the existing database with the new one. no more errors on update
        AppDatabase.getDatabase(applicationContext)

        HinduCalendarRepository.initialize(applicationContext)

        LocationTracker.initialize(applicationContext)

        TTSManager.initialize(applicationContext)

        copyEphemerisFiles()

        PanchangamCalculator.initializeEphimeris(applicationContext)


        // todo ensure alarm mgr initialisation being in async aint
        //  broke none down the line
        // move it to a different file
        CoroutineScope(Dispatchers.IO).launch {
            // Clear stored Panchangam state on app cold start
            val currentDp = initialiseDynamicPanchangam()

            // 3. Convert expdttm to epoch milliseconds
            val expiryEpochMs = currentDp.expiryDttm
                .atZone(INDIA_ZONE)
                .toInstant()
                .toEpochMilli()

            // 1. Calculate active slot and update PreferencesManager
            SlotManager.refreshSlot()

            // 2. Calculate epoch millis for next slot boundary and reschedule:
            val nextSlotBoundaryEpochMs = SlotManager.getNextSlotBoundaryEpochMs()

            // 4. Bootstrap alarms sequentially with guaranteed data
            AlarmScheduler.bootstrapAppAlarms(       // by default sets notification alarms
                context = applicationContext,
                initialExpiryEpochMs = expiryEpochMs, // for dynamic panchanga alarm
                initialSlotEpochMs = nextSlotBoundaryEpochMs
            )
        }
    }

    private suspend fun initialiseDynamicPanchangam() : DynamicPanchangam {
        val preferencesManager = PreferencesManager(applicationContext)

        // 1. Calculate current state on background thread
        // Calculate current state and immediately save as the baseline for future alarms
        val currentDp = PanchangamCalculator.calculateDynamicPanchangamDetails(
            currDttm = CommonFunctions.getCurrentTime(),
            currentMode = true
        )

        // 2. Save to DataStore
        preferencesManager.setPreviousPanchangam(currentDp)
        return currentDp
    }

    private fun copyEphemerisFiles() {
        val epheDir = File(filesDir, "ephe")
        if (!epheDir.exists()) epheDir.mkdirs()

        listOf("sepl_18.se1", "semo_18.se1").forEach { fileName ->
            val destFile = File(epheDir, fileName)

            // 🔑 Re-copy if file is missing OR corrupted/empty (0 bytes)
            if (!destFile.exists() || destFile.length() == 0L) {
                try {
                    assets.open("ephe/$fileName").use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                            output.flush()
                        }
                    }
                    Log.d("MainActivity", "Successfully copied $fileName (${destFile.length()} bytes)")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to copy $fileName", e)
                    destFile.delete() // Clean up corrupt or partial file on failure
                }
            }
        }
    }
}