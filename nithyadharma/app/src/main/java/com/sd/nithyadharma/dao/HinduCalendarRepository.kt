package com.sd.nithyadharma.dao

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.sd.nithyadharma.R
import com.sd.nithyadharma.model.ScheduleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.InputStreamReader

object DataRepository {

    private var applicationContext: Context? = null

    // Backing state
    private val _scheduleData = mutableStateOf<List<ScheduleItem>>(emptyList())

    // Public immutable state
    val scheduleData: State<List<ScheduleItem>> = _scheduleData

    // Process-lifetime flag (resets automatically on process death)
    private var scheduleLoaded = false

    // JSON parser
    private val json = Json { ignoreUnknownKeys = true }

    /* ------------------------- */
    /* Initialization            */
    /* ------------------------- */

    fun initialize(context: Context) {
        if (applicationContext == null) {
            applicationContext = context.applicationContext
            Log.d("DataRepository", "Initialized with application context.")
        }
    }

    /* ------------------------- */
    /* Public helpers            */
    /* ------------------------- */

    /** Safe to call from UI, AlarmReceiver, anywhere */
    fun isScheduleLoaded(): Boolean {
        return scheduleLoaded && _scheduleData.value.isNotEmpty()
    }

    /** Entry point for UI / AlarmReceiver */
    suspend fun ensureScheduleLoaded() {
        if (isScheduleLoaded()) {
            Log.d("DataRepository", "Schedule already loaded, skipping reload.")
            return
        }
        loadScheduleDataInternal()
    }

    /* ------------------------- */
    /* Internal loader           */
    /* ------------------------- */

    private suspend fun loadScheduleDataInternal() {
        Log.i("DataRepository", "Loading schedule data")

        val context = applicationContext ?: run {
            Log.e(
                "DataRepository",
                "Not initialized! Call DataRepository.initialize(context) first."
            )
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream =
                    context.assets.open("hindu_calendar.json")
                //context.resources.openRawResource(R.raw.hindu_calendar)

                val jsonString = InputStreamReader(inputStream).use { it.readText() }

                val result =
                    json.decodeFromString<List<ScheduleItem>>(jsonString)

                withContext(Dispatchers.Main) {
                    _scheduleData.value = result
                    scheduleLoaded = true
                }

                Log.d(
                    "DataRepository",
                    "Loaded ${result.size} schedule items successfully."
                )
            } catch (e: Exception) {
                Log.e("DataRepository", "Failed to load schedule JSON", e)

                withContext(Dispatchers.Main) {
                    _scheduleData.value = emptyList()
                    scheduleLoaded = false
                }
            }
        }
    }
}
