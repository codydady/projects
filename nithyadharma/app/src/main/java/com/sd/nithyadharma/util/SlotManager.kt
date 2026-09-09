package com.sd.nithyadharma.util

import android.util.Log
import com.sd.nithyadharma.model.TimeSlotConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.LocalTime

object SlotManager {

    private val slotConfigs: List<TimeSlotConfig> by lazy {
        parseSlotsFromJson(Constants.CARD_TIME_SLOTS)
    }

    // 🔑 Reactive StateFlow managed centrally
    private val _currentSlot = MutableStateFlow(determineCurrentSlot())
    val currentSlot: StateFlow<TimeSlotConfig> = _currentSlot.asStateFlow()

    fun determineCurrentSlot(now: LocalTime = LocalTime.now(Constants.INDIA_ZONE)): TimeSlotConfig {
        return slotConfigs.firstOrNull { it.isTimeInSlot(now) } ?: slotConfigs.first()
    }

    /**
     * Call this when the app opens or when SlotAlarmReceiver fires
     * to push the updated slot to all UI collectors instantly.
     */
    fun refreshSlot() {
//        Log.d("SlotManager", "SlotManager refreshSlot entry")

        val newSlot = determineCurrentSlot()
        Log.d("SlotManager", "SlotManager refreshSlot currslot ${_currentSlot.value.slotId } , newslot ${newSlot.slotId}")

        if (_currentSlot.value.slotId != newSlot.slotId) {
            _currentSlot.value = newSlot
        }
    }

    fun getNextSlotBoundaryEpochMs(now: LocalDateTime = CommonFunctions.getCurrentTime()): Long {
        val currentSlot = determineCurrentSlot(now.toLocalTime())
        val slotEndTime = LocalTime.parse(currentSlot.endTime)

        var nextBoundary = now.with(slotEndTime)
        if (now.isAfter(nextBoundary) || now.isEqual(nextBoundary)) {
            nextBoundary = nextBoundary.plusDays(1)
        }

        return nextBoundary.atZone(Constants.INDIA_ZONE).toInstant().toEpochMilli()
    }

    private fun parseSlotsFromJson(jsonString: String): List<TimeSlotConfig> {
        val list = mutableListOf<TimeSlotConfig>()
        try {
            val jsonArray = org.json.JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TimeSlotConfig(
                        slotId = obj.getString("slotId"),
                        startTime = obj.getString("startTime"),
                        endTime = obj.getString("endTime"),
                        gradientColorHex1 = obj.getString("gradientColorHex1"),
                        gradientColorHex2 = obj.getString("gradientColorHex2"),
                        textColorHex = obj.getString("textColorHex")
                    )
                )
            }
        } catch (e: Exception) {
            Log.d("SlotManager", "SlotManager parseSlotsFromJson error")
            e.printStackTrace()
        }
        return list
    }
}