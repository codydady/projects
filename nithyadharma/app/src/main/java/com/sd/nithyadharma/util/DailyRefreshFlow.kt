package com.sd.nithyadharma.util

import android.util.Log
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object DailyRefreshFlow {

    // for static panchangam details
    fun observeRefreshForStaticPanchangamDetails(triggerTime: LocalTime) = flow {
        // i removed it since it calculated twice but then found without this
        // it wont work when app starts from doze again - lets see
        emit(Unit) // Emit immediately on first collection

        while (true) {
            val now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
            val nextTrigger = if (now.toLocalTime().isAfter(triggerTime)) {
                // Already past trigger time today, schedule for tomorrow
                LocalDateTime.of(now.toLocalDate().plusDays(1), triggerTime)
            } else {
                // Trigger time is later today
                LocalDateTime.of(now.toLocalDate(), triggerTime)
            }

            val delayMillis = ChronoUnit.MILLIS.between(now, nextTrigger).coerceAtLeast(0)
            delay(delayMillis)

            emit(Unit)
        }
    }

    // for dynamic panchangam details
    fun <T> observeRefreshForDynamicPanchangamDetails(
        calculator: (LocalDateTime) -> T,
        getExpiry: (T) -> LocalDateTime
    ) = flow {
        val zone = ZoneId.of("Asia/Kolkata") //todo

        while (currentCoroutineContext().isActive) {
            val now = LocalDateTime.now(zone)

            val data = try {
                calculator(now)
            } catch (e: Exception) {
                Log.e("DynamicRefresh", "Calculation failed", e)
                delay(7000)
                continue
            }

            emit(data)

            val expiryTime = getExpiry(data)

            // Get CURRENT time for accurate delay calculation
            val currentTime = LocalDateTime.now(zone)
            val delayMillis = ChronoUnit.MILLIS.between(currentTime, expiryTime)
                .coerceAtLeast(60_000L) // Minimum
            Log.d("DynamicRefresh", "Next refresh at: $expiryTime (in ${delayMillis/1000}s)")

            delay(delayMillis)
        }
    }


}