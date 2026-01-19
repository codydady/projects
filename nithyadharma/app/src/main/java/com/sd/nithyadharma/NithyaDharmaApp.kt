package com.sd.nithyadharma

import android.app.Application
import android.util.Log
import com.sd.nithyadharma.dao.AppDatabase
import com.sd.nithyadharma.dao.DataRepository
import com.sd.nithyadharma.util.AlarmScheduler
import com.sd.nithyadharma.util.LocationTracker
import com.sd.nithyadharma.util.PanchangamCalculator
import com.sd.nithyadharma.util.TTSManager
import java.io.File

class NithyaDharmaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // this line nukes the existing database with the new one. no more errors on update
        AppDatabase.getDatabase(applicationContext)

        DataRepository.initialize(applicationContext)

        LocationTracker.initialize(applicationContext)

        TTSManager.initialize(applicationContext)

        copyEphemerisFiles()

        PanchangamCalculator.initializeEphimeris(applicationContext)

        AlarmScheduler.ensureAlarmExists(applicationContext)
    }

    private fun copyEphemerisFiles() {
        val epheDir = File(filesDir, "ephe")
        if (!epheDir.exists()) epheDir.mkdirs()
        listOf("sepl_18.se1", "semo_18.se1").forEach { fileName ->
            val destFile = File(epheDir, fileName)
            if (!destFile.exists()) {
                try {
                    assets.open("ephe/$fileName").use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d("MainActivity", "Copied $fileName to $epheDir")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to copy $fileName", e)
                }
            }
        }
    }
}