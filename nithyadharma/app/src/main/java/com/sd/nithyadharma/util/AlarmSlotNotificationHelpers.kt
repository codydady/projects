package com.sd.nithyadharma.util

import LocaleManager
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.sd.nithyadharma.MainActivity
import com.sd.nithyadharma.R
import com.sd.nithyadharma.cards.CardFactory
import com.sd.nithyadharma.cards.CardMode
import com.sd.nithyadharma.cards.CardType
import com.sd.nithyadharma.dao.*
import com.sd.nithyadharma.model.Audio_Files
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.model.PanchangaAttr.DynamicPanchangam
import com.sd.nithyadharma.util.Constants.INDIA_ZONE
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.encodeToString
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

object AlarmSlotNotificationHelpers {

    fun getSelectedLanguageBlocking(preferencesManager: PreferencesManager): NDLanguage {
        return runBlocking { preferencesManager.getSelectedLanguage().first() }
    }

    object AlarmNotificationChannel{
        const val CHANNEL_ID = "nithya_dharma_channel"
        const val CHANNEL_NAME = "Daily Dharma Reminders"
    }

    suspend fun checkAndSendDailyHCNotifications(appContext: Context,preferencesManager: PreferencesManager) {
        Log.d("AlarmReceiver", "Executing Daily Schedule Notifications...")

        val currentLang = getSelectedLanguageBlocking(preferencesManager)

        HinduCalendarRepository.ensureScheduleLoaded()
        val scheduleItems = HinduCalendarRepository.scheduleData.value

        val today = Calendar.getInstance().time
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val earlyReminderDays = preferencesManager.getEarlyReminder().first()

        if (scheduleItems.isEmpty()) {
            return
        }

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

            val formattedDate = outputFormat.format(eventDate)
//            val occasionDtl = if (currentLang == NDLanguage.TA) item.occasionTa else item.occasionEn
            val occasionDtl = when (currentLang) {
                NDLanguage.TA -> item.occasionTa
                NDLanguage.KA -> item.occasionKa
                NDLanguage.EN -> item.occasionEn
                NDLanguage.HI -> item.occasionHi
            }
            val notificationText = "$occasionDtl on $formattedDate"
            val notificationId = Random.nextInt(1, Int.MAX_VALUE)

            sendNotification(appContext, title, notificationText, notificationId)
        }

    }

    // Standard Notification Builder
    private fun sendNotification(context: Context, title: String, notificationText: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val largeIconBitmap: Bitmap? = try {
            ContextCompat.getDrawable(context, R.mipmap.ic_launcher)?.toBitmap()
        } catch (e: Exception) {
            null
        }

        val notification = NotificationCompat.Builder(context, AlarmNotificationChannel.CHANNEL_ID)
            .setSmallIcon(R.mipmap.dakshinamurthy)  // this shows up in the top bar of the phone
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
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to show notification", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(AlarmNotificationChannel.CHANNEL_ID,
                AlarmNotificationChannel.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders for daily rituals and festival starts."
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // this is to overwrite the static panchangam every day at alarm time
    suspend fun addGreetingCard(preferencesManager: PreferencesManager) {
        try {
            // this method overwrites the sp in viewmodel everyday
            Log.d("AlarmNotificationHelper", "addGreetingCard  generated successfully")
            val userName = preferencesManager.getCustomerInfo().first().name
            val currDttm = CommonFunctions.getCurrentTime()
            val greeting = if (currDttm.hour < 12) "good_morning" else "good_evening"
            val motto = "greeting_motto"

            val notificationData = mapOf(
                "name" to JsonPrimitive(userName), // imp not great design - actually locale aware string, but we cant maie string locale aware
                "greeting" to JsonPrimitive(greeting),
                "motto" to JsonPrimitive(motto)
            )

            // now save it to cards
            val card = CardFactory.makeCard(
                mode = CardMode.WRITE_FG,
                type = CardType.GREETING,
                expiryOffsetMillis = CardFactory.CARD_EXPIRY_OFFSET_SHORT,
                customParams = notificationData
            )
            // this local function is a wrapper to cardfactory.saveandaddcard
            CardRepository.saveAndAddCard(preferencesManager,card)
        }
        catch (e: Exception) {
            Log.e("AlarmNotificationHelper", "Error generating greeting card", e)
        }
    } // fun addGreetingCard ends

    // saves the panchanga notification card to activecards so it triggers a screen refresh
    // and calculates the next expiry and sends it to caller for next alarm registration
    suspend fun createDynamicPanchangamNotifications(
        appContext: Context
    ): LocalDateTime {
        Log.d("AlarmReceiver", "in the alarm for calculateDynamicPanchangamDetails")

        val preferencesManager = PreferencesManager(appContext)

        // 1. Fetch previousDp from PreferencesManager
        var previousDp = preferencesManager.getPreviousPanchangam().first()

        // 2. Calculate newDp using PanchangamCalculator
        val currDttm = LocalDateTime.now(INDIA_ZONE)

        val currentDp = PanchangamCalculator.calculateDynamicPanchangamDetails(
            currDttm = currDttm,
            currentMode = true
        )

        // 3.lets first add a card to in-mem activecards
        val result = findDPNotificationCause(previousDp!!,currentDp,currDttm)

        result?.let { (title, message, untilTime) ->
            saveDpAndTriggerPanchangaNotification(
                appContext = appContext,
                title = title,
                message = message,
                untilTime = untilTime,
                score = currentDp.score
            )
        }

        // 2. Save to DataStore
        preferencesManager.setPreviousPanchangam(currentDp)

        return currentDp.expiryDttm
    }

    /**
     * used when both old and new dps are present. ie from iteration 2.
     */
    private fun findDPNotificationCause(
        oldDp: DynamicPanchangam,
        newDp: DynamicPanchangam,
        currentDttm: LocalDateTime
    ): Triple<String, String, String>? {
        Log.i("findDPNotificationCause", "entered formatPanchangamNotification 2 args")

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

        // 1. THITHI CHANGE
        if (oldDp.thithi != newDp.thithi && newDp.thithi != null) {
            val untilTime = newDp.thithiEndTime?.format(timeFormatter) ?: "11:59 PM"
            val title = "str_tt" // LocaleManager.getString("str_tt", currentLanguage)
            val message = "pa_${oldDp.thithi} | pa_${newDp.thithi}".lowercase()
            return Triple(title, message, untilTime)
        }
//        Log.i("findDPNotificationCause", "2 place 1")

        // 2. NAKSHATRA CHANGE
        if (oldDp.nakshatra != newDp.nakshatra && newDp.nakshatra != null) {
            val untilTime = newDp.nakshatraEndTime?.format(timeFormatter) ?: "11:59 PM"
            val title = "str_nk" // LocaleManager.getString("str_nk", currentLanguage)
            val message = "pa_${oldDp.nakshatra} | pa_${newDp.nakshatra}".lowercase()
            return Triple(title, message, untilTime)
        }
//        Log.i("findDPNotificationCause", "2 place 2")

        // 3. YOGA CHANGE

        if (oldDp.yoga != newDp.yoga && newDp.yoga != null) {
            val untilTime = newDp.yogaEndTime?.format(timeFormatter) ?: "11:59 PM"
            val title = "str_yg" // LocaleManager.getString("str_yg", currentLanguage)
            val message = "pa_${oldDp.yoga} | pa_${newDp.yoga}".lowercase()
            return Triple(title, message, untilTime)
        }

        // 4. KARANA CHANGE
        if (oldDp.karana != newDp.karana && newDp.karana != null) {
            val untilTime = newDp.karanaEndTime?.format(timeFormatter) ?: "11:59 PM"
            val title = "str_kr" //LocaleManager.getString("str_kr", currentLanguage)
            val message = "pa_${oldDp.karana} | pa_${newDp.karana}".lowercase()
            return Triple(title, message, untilTime)
        }

        val (rahuKalam, yamaGandam, gulikai) = getRahuKaalamYamaGandamGulikaiPeriods(newDp.sunrise!!, newDp.sunset!!, useDynamic = false)

        Log.i("findDPNotificationCause", "none of the angas match, " +
                "must be ragu kala / yama ganda, cur time ${currentDttm} , exp time ${rahuKalam.end}")

        /* imp end is never captured as its a time comparison and currentdttm
            most likely beats the rahu or yama end as it can happen anywhere in the
            minute whereas the former would be on sec 0. its okay to ignore that - important
         */
        val bufferSeconds = 5L

        // --- Rahu Kalam Checks ---
        val isRahuActive = currentDttm >= rahuKalam.start && currentDttm <= rahuKalam.end
        val isRahuJustEnded = currentDttm > rahuKalam.end && currentDttm <= rahuKalam.end.plusSeconds(bufferSeconds)

        if (isRahuActive || isRahuJustEnded) {
            val untilTime = newDp.expiryDttm.format(timeFormatter) ?: "11:59 PM"
            val title = "str_rg"
            val message = if (isRahuJustEnded) "str_mudivu" else "str_arambam" // Adjust string if needed
            return Triple(title, message, untilTime)
        }

        // --- Yamagandam Checks ---
        val isYamaActive = !currentDttm.isBefore(yamaGandam.start) && !currentDttm.isAfter(yamaGandam.end)
        val isYamaJustEnded = currentDttm > yamaGandam.end && currentDttm <= yamaGandam.end.plusSeconds(bufferSeconds)

        if (isYamaActive || isYamaJustEnded) {
            val untilTime = newDp.expiryDttm.format(timeFormatter) ?: "11:59 PM"
            val title = "str_ya"
            val message = if (isYamaJustEnded) "str_mudivu" else "str_arambam"
            return Triple(title, message, untilTime)
        }

        // this is simple yet i didnt catch it. it always happens. when rg or yg ends,
        // this happens as the run happens 3 secs after the fact
        Log.i("findDPNotificationCause", "not even rg or yama , wth ?")

        // lets skip gulikai for now
//        if (!currentDttm.isBefore(gulikai.start) && !currentDttm.isAfter(gulikai.end)) {
//
//        }
        return null
    }

    // 3. Format transition notification and save new card to PreferencesManager
    private suspend fun saveDpAndTriggerPanchangaNotification(appContext: Context,
                                                              title: String,
                                                              message: String,
                                                              untilTime: String,
                                                              score: Int) {
        // create a new notification card and add it to the activecards so view is refreshed immdly
        // 1. For Notification Card
        Log.d("AlarmNotificationHelper", "inside triggerNotification ")

        val notificationData = mapOf(
            "title" to JsonPrimitive(title), // todo not great design - actually locale aware string
            "message" to JsonPrimitive(message),
            "untilTime" to JsonPrimitive(untilTime),
            "score" to JsonPrimitive(score)
        )

        // expiry time is short if rahu or yama ganda
        val expiryOffsetMillis = when (title) {
            "str_rg", "str_ya" -> CardFactory.CARD_EXPIRY_OFFSET_SHORT
            else -> CardFactory.CARD_EXPIRY_OFFSET_MEDIUM
        }

        val card = CardFactory.makeCard(
            mode = CardMode.WRITE_FG,
            type = CardType.DP_NOTIFICATION,
            expiryOffsetMillis = expiryOffsetMillis,
            customParams = notificationData
        )
        Log.d("AlarmNotificationHelper", "triggerNotification after making card")

        // thats all we need to do. this will add to activecards
        // and mainscreen will automatically recompose.
        val preferencesManager = PreferencesManager(appContext)
        CardRepository.saveAndAddCard(preferencesManager,card)
    } // fun saveDpAndTriggerPanchangaNotification ends

    // this is to check if there is a post today in site every day at noon
    suspend fun addDharmaTodayCard(preferencesManager: PreferencesManager) {
        // shouldnt be checking here but dont have a better place.
        // will move logic soon out of this
        PostOfDayRepository.refresh()

        // 2. Read the latest StateFlow value after refresh completes
        val todaysPost = PostOfDayRepository.postOfDay.value

        // 3. Early return if no post exists for today
        if (todaysPost == null) {
            Log.i("AlarmNotificationHelper", "Skipping card creation: No post available for today.")
            return
        }

        try {
            // if there is a post today in templepages site, it loads it here
            Log.d("AlarmNotificationHelper", "addDharmaTodayCard entry")

            val notificationData = mapOf(
                "post" to JsonPrimitive("dummy as post object is collected and passed")
            )
            // now save it to cards
            val card = CardFactory.makeCard(
                mode = CardMode.WRITE_FG,
                type = CardType.TODAYS_DHARMA,
                expiryOffsetMillis = CardFactory.CARD_EXPIRY_OFFSET_MEDIUM,
                customParams = notificationData
            )
            // this local function is a wrapper to cardfactory.saveandaddcard
            CardRepository.saveAndAddCard(preferencesManager,card)
        }
        catch (e: Exception) {
            Log.e("AlarmNotificationHelper", "Error generating Static Panchangam card", e)
        }
    } // fun addDharmaTodayCard ends

    // this is to overwrite the static panchangam every day at alarm time
    suspend fun addRasiPalanCard(preferencesManager: PreferencesManager) {
        try {
            val userRasi = preferencesManager.getCustomerInfo().first().rasi

            // 1. Create instance
            val calculator = HoroscopeCalculator()

            // 2. Call default function (uses getCurrentTime() automatically)
            val currentPlanetaryPositions = calculator.calcPlanetaryPositions()

            val todaysRasiPalanReport = RasiPalanEngine.generateRasiPalanReport(userRasi, currentPlanetaryPositions)
            Log.d("AlarmNotificationHelper", "todaysRasiPalanReport card generated successfully")

            val reportJson = Json.encodeToString(todaysRasiPalanReport)
            val customParams = mapOf("rasi_palan_data" to JsonPrimitive(reportJson))

            // now save it to cards
            val card = CardFactory.makeCard(
                mode = CardMode.WRITE_FG,
                type = CardType.RASI_PALAN,
                expiryOffsetMillis = CardFactory.CARD_EXPIRY_OFFSET_MEDIUM,
                customParams = customParams
            )
            // this local function is a wrapper to cardfactory.saveandaddcard
            CardRepository.saveAndAddCard(preferencesManager,card)
        }
        catch (e: Exception) {
            Log.e("AlarmNotificationHelper", "Error generating Static Panchangam card", e)
        }
    } // fun addRasiPalanCard ends

    // this is to overwrite the static panchangam every day at alarm time
    suspend fun addNaalKaatiCardWithSP(preferencesManager: PreferencesManager) {
        try {
            // this method overwrites the sp in PanchangamRepository which inturn
            // updates the listener in viewmodel rightaway
            PanchangamRepository.generateStaticPanchangam()
            Log.d("AlarmNotificationHelper", "Static Panchangam card generated successfully")

            val notificationData = mapOf(
                "staticPanchangam" to JsonPrimitive("dummy as static pachangam is collected and passed")
            )
            // now save it to cards
            val card = CardFactory.makeCard(
                mode = CardMode.WRITE_FG,
                type = CardType.NAAL_KAATTI,
                expiryOffsetMillis = CardFactory.CARD_EXPIRY_OFFSET_MEDIUM,
                customParams = notificationData
            )
            // this local function is a wrapper to cardfactory.saveandaddcard
            CardRepository.saveAndAddCard(preferencesManager,card)
        }
        catch (e: Exception) {
            Log.e("AlarmNotificationHelper", "Error generating Static Panchangam card", e)
        }
    } // fun addNaalKaatiCardWithStaticPanchangam ends

    // calc if chandrashtama for this user and alert him by a card
    suspend fun checkDPForChandraashtamamAndSaveIfFound(
        appContext: Context, preferencesManager: PreferencesManager) {

        val userRasi = preferencesManager.getCustomerInfo().first().rasi
        val currentLang = getSelectedLanguageBlocking(preferencesManager)

        val nowIst = CommonFunctions.getCurrentTime()

        // 3. Call the calculation function directly
        val newDp = PanchangamCalculator.calculateDynamicPanchangamDetails(nowIst, true)

        if ( newDp.chandrashtamaRasi == userRasi) {
            val title = LocaleManager.getString("str_cr", currentLang)
            val notificationText = LocaleManager.getString("str_crdtl", currentLang)
            val notificationId = Random.nextInt(1, Int.MAX_VALUE)

            // send notification to show it to the user , does it work from here
            sendNotification(
                appContext,
                title,
                notificationText,
                notificationId
            )

            // also save a card to be displayed
            val notificationData = mapOf(
                "title" to JsonPrimitive("str_cr"),
                "message" to JsonPrimitive("str_crdtl"),
                "untilTime" to JsonPrimitive("11:59 PM"),
                "score" to JsonPrimitive(newDp.score - 15 ) // chndrstma penalty is 15 pts
            )

            val card = CardFactory.makeCard(
                mode = CardMode.WRITE_FG,
                type = CardType.DP_NOTIFICATION,
                expiryOffsetMillis = CardFactory.CARD_EXPIRY_OFFSET_MEDIUM,
                customParams = notificationData
            )
            Log.d("AlarmNotificationHelper", "after making chandrashtama card")
            CardRepository.saveAndAddCard(preferencesManager,card)
        }
    } // func checkDPForChandraashtamamAndSaveIfFound ends

    // slot related functions
    suspend fun createSlotChangeCards(appContext: Context,
                                      preferencesManager: PreferencesManager): Long {
        // 1. Calculate active slot and update PreferencesManager
        SlotManager.refreshSlot()

        // 2. Calculate epoch millis for next slot boundary and reschedule:
        val nextSlotBoundaryEpochMs = SlotManager.getNextSlotBoundaryEpochMs()
        val etinmins = nextSlotBoundaryEpochMs / 60000
        Log.d("AlarmNotificationHelper", "curr slot ${SlotManager.currentSlot?.value?.slotId} , next Slot Change at ..${etinmins} mins")

        // 3. do the actual jobs pertaining to slot changes
        // 3.0 sound alert while a slot changes
        slotChangeSoundAlert(appContext)

        // 3.1 music card
        slotChangeMusicCardLoader(preferencesManager)

        // 3.2 sp naal kaatti card & greeting card
        slotChange_GR_SP_TD_RT_Loader(preferencesManager)

        return nextSlotBoundaryEpochMs
    }

    private suspend fun slotChangeSoundAlert(appContext: Context) {
        val soundManager = SoundManager.getInstance(appContext)
        // Play sound (the playPending fix will trigger playback once loaded)
        soundManager.playThreeBells()
        // Allow enough time for the sound effect to finish playing (~1-2s)
        delay(1500)
        // Clean up native audio resources
        soundManager.release()
    }// Get singleton instance using applicationContext

    private suspend fun slotChange_GR_SP_TD_RT_Loader(preferencesManager: PreferencesManager) {

        // 1. Get the current day of the week
        val currentSlotId = SlotManager.currentSlot?.value?.slotId // e.g., "DAWN", "MORNING", "EVENING"
        val currentDay = LocalDate.now().dayOfWeek

        // 2. order of cards is very important as they dictate display order
        when (currentSlotId) {
            "DAWN" -> {
                addGreetingCard(preferencesManager)
            }
            "MORNING" -> {
                addNaalKaatiCardWithSP(preferencesManager)
                addRasiPalanCard(preferencesManager)
            }
            "NOON" -> {
                addDharmaTodayCard(preferencesManager)
            }
            "EVENING" -> {
                addGreetingCard(preferencesManager)
            }
            "NIGHT" ->  when (currentDay) {
                DayOfWeek.SATURDAY -> addRatingCard(preferencesManager) // show rating card only on saturdays
                else -> null
            }
            else -> null // Fallback or unhandled slots , noon is never played with !!
        }
    }

    private suspend fun slotChangeMusicCardLoader(preferencesManager: PreferencesManager) {

        // 1. Get the current day of the week
        val currentDay = LocalDate.now().dayOfWeek
        val currentSlotId = SlotManager.currentSlot?.value?.slotId // e.g., "DAWN", "MORNING", "EVENING"

        // 2. Determine the Audio_File based on Slot ID and Day of Week
        val audioFile: Audio_Files? = when (currentSlotId) {
            "DAWN" -> {
                // Suprabhatham every day at Dawn
                Audio_Files.SUPRABHATHAM
            }

            "MORNING" -> when (currentDay) {
                DayOfWeek.MONDAY -> Audio_Files.KOLARU_PATHIGAM
                DayOfWeek.TUESDAY -> Audio_Files.PANCHAMIRDHA_VANNAM
                DayOfWeek.FRIDAY -> Audio_Files.KOLARU_PATHIGAM
                DayOfWeek.SATURDAY -> Audio_Files.VISHNU_SAHASRANAMAM
                else -> null
            }

            "EVENING" -> when (currentDay) {
                DayOfWeek.MONDAY -> Audio_Files.RUDHRAM
                DayOfWeek.TUESDAY -> Audio_Files.KANDHA_SASHTI_KAVACHAM
                DayOfWeek.WEDNESDAY -> Audio_Files.VISHNU_SAHASRANAMAM
                DayOfWeek.THURSDAY -> Audio_Files.DAKSHINAMURTHY_STHOTHRAM
                DayOfWeek.FRIDAY -> Audio_Files.MAHISHASURA_MARDHINI
                DayOfWeek.SATURDAY -> Audio_Files.NAMO_ANJANEYAM
                else -> Audio_Files.DHATHATREYA_STHUTHI
            }

            "NIGHT" ->  when (currentDay) {
                DayOfWeek.WEDNESDAY -> Audio_Files.RAMA_RAMA_JEYA_RAJARAM
                DayOfWeek.SATURDAY -> Audio_Files.RAMA_RAMA_JEYA_RAJARAM
                else -> null
            }

            else -> null // Fallback or unhandled slots , noon is never played with !!
        }

        Log.d("AlarmNotificationHelper", "after figuring song in slotChangeMusicProvider")

        // 3. Build card if audio file exists for the slot
        audioFile?.let { selectedAudio ->
            val customParams = mapOf(
                "audioKey" to JsonPrimitive(selectedAudio.toString())
            )
            Log.d("AlarmNotificationHelper", "non null song slotChangeMusicProvider")

            // Save or emit card
            val card = CardFactory.makeCard(
                mode = CardMode.WRITE_FG,
                type = CardType.MUSIC,
                expiryOffsetMillis = CardFactory.CARD_EXPIRY_OFFSET_SHORT,
                customParams = customParams
            )

            // 🔑 Push the new slot to the UI flow instantly
            CardRepository.saveAndAddCard(preferencesManager,card)
        }
    } // func slotChangeMusicProvider ends

    suspend fun addRatingCard(preferencesManager: PreferencesManager) {
        val customParams = mapOf(
            "post" to JsonPrimitive("dummy as post object is collected and passed")
        )
        Log.d("AlarmNotificationHelper", "in createRatingCard")

        // Save or emit card
        val card = CardFactory.makeCard(
            mode = CardMode.WRITE_FG,
            type = CardType.RATING_SHARE,
            expiryOffsetMillis = CardFactory.CARD_EXPIRY_OFFSET_SHORT,
            customParams = customParams
        )

        // 🔑 Push the new slot to the UI flow instantly
        CardRepository.saveAndAddCard(preferencesManager,card)
    }
}