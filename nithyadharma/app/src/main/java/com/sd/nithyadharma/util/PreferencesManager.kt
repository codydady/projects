package com.sd.nithyadharma.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import com.google.gson.JsonPrimitive

import com.sd.nithyadharma.model.BreathingTimings
import com.sd.nithyadharma.model.CustomerInfo
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.model.PanchangaAttr
import com.sd.nithyadharma.model.PanchangaAttr.Rasi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime


// Define a preferences key for Early, Near reminder, alertInterval, finalCount, and colorCodeVisited
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesManager(context: Context) {

    private val dataStore = context.dataStore

    // new stuff pertaining to storing the cards etc
    // --- Saved Cards Keys ---
    private val SAVED_CARDS_MAP_KEY = stringPreferencesKey("saved_cards_map")

    // 🔑 Place it here as a class property
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, JsonSerializer<LocalDateTime> { src, _, _ ->
            if (src == null) null else JsonPrimitive(src.toString())
        })
        .registerTypeAdapter(LocalDateTime::class.java, JsonDeserializer { json, _, _ ->
            if (json == null || json.asString.isEmpty()) null
            else runCatching { LocalDateTime.parse(json.asString) }.getOrNull()
        })
        .create()

    /**
     * Saves or updates a card in the map stored in DataStore.
     * Stored as a JSON Map of String (cardId) -> String (jsonPayload).
     */
    suspend fun saveCardMetadata(cardId: String, jsonPayload: String) {
        dataStore.edit { preferences ->
            val currentMapJson = preferences[SAVED_CARDS_MAP_KEY] ?: "{}"
            val currentMap = parseCardsMap(currentMapJson).toMutableMap()

            // Put or overwrite by cardId
            currentMap[cardId] = jsonPayload

            // 👈 Explicitly specify <Map<String, String>> here
            preferences[SAVED_CARDS_MAP_KEY] =
                Json.encodeToString<Map<String, String>>(currentMap)
        }
    }

    // this is not card removal but removal of card from the stored preferences
    suspend fun removeCardMetadata(cardId: String) {
        Log.i("PreferencesManager", "entered removeCardMetadata cardid - ${cardId}")

        dataStore.edit { preferences ->
            val currentMapJson = preferences[SAVED_CARDS_MAP_KEY] ?: "{}"
            val currentMap = parseCardsMap(currentMapJson).toMutableMap()

            // Removes key if present; returns null if absent
            if (currentMap.remove(cardId) != null) {
                preferences[SAVED_CARDS_MAP_KEY] =
                    Json.encodeToString<Map<String, String>>(currentMap)
            }
        }
    }

    /**
     * Retrieves all saved card JSON strings from the SAVED_CARDS_MAP_KEY entry.
     */
    fun getAllCardsMetadata(): Flow<List<String>> {
        return dataStore.data.map { preferences ->
            val currentMapJson = preferences[SAVED_CARDS_MAP_KEY] ?: "{}"
            val currentMap = parseCardsMap(currentMapJson)
            currentMap.values.toList()
        }
    }

    /**
     * Synchronous / Direct snapshot retrieval of saved card JSONs.
     */
//    fun getSavedCardsJsonDirectly(): List<String> = runBlocking {
//        val preferences = dataStore.data.first()
//        val rawJson = preferences[SAVED_CARDS_MAP_KEY] ?: "{}"
//        parseCardsMap(rawJson).values.toList()
//    }

    /**
     * Clears all saved cards from DataStore.
     */
    suspend fun clearAllCards() {
        dataStore.edit { preferences ->
            preferences[SAVED_CARDS_MAP_KEY] = "{}"
        }
    }

    // Private Helper to Safely Deserialize Map
    private fun parseCardsMap(rawJson: String): Map<String, String> {
        return runCatching {
            Json.decodeFromString<Map<String, String>>(rawJson)
        }.getOrDefault(emptyMap())
    }

    /// -- existing stuff starts

    // from firebase authentication
    private val FIREBASE_EMAIL_ID = stringPreferencesKey("firebase_auth_email")

    // Keys for storing data
    private val EARLY_REMINDER_KEY = intPreferencesKey("early_reminder")
    private val ALERT_INTERVAL_KEY = intPreferencesKey("alert_interval")
    private val FINAL_COUNT_KEY = intPreferencesKey("final_count")
    private val HIDE_VISITED_TEMPLE = booleanPreferencesKey("hide_visited_temples")
    private val SHOW_ONLY_MARKED_TEMPLE = booleanPreferencesKey("show_only_marked_temples")

    private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")

    // Key for storing the counter value
    private val COUNTER_KEY = intPreferencesKey("counter_value")

    // to store the temple ids to delete
    private val DELETED_TEMPLE_IDS_KEY = stringPreferencesKey("to_be_deleted_temple_ids")

    // Keys
    private val CUSTOMER_NAME = stringPreferencesKey("customer_name")
    private val CUSTOMER_EMAIL = stringPreferencesKey("customer_email")
    private val CUSTOMER_PHONE = stringPreferencesKey("customer_phone")
    private val CUSTOMER_ADDRESS1 = stringPreferencesKey("customer_address1")
    private val CUSTOMER_ADDRESS2 = stringPreferencesKey("customer_address2")
    private val CUSTOMER_CITY = stringPreferencesKey("customer_city")
    private val CUSTOMER_STATE = stringPreferencesKey("customer_state")
    private val CUSTOMER_PINCODE = stringPreferencesKey("customer_pincode")
    // for horoscope
    private val CUSTOMER_DTOB_KEY = stringPreferencesKey("customer_dttm_birth")
    private val CUSTOMER_LAT_KEY = stringPreferencesKey("customer_birth_lat")
    private val CUSTOMER_LON_KEY = stringPreferencesKey("customer_birth_lon")
    private val CUSTOMER_RASI_KEY = stringPreferencesKey("customer_rasi")

    // --- Breathing Timing Keys ---
    private val INHALE_KEY = floatPreferencesKey("inhale_time")
    private val HOLD_KEY = floatPreferencesKey("hold_time")
    private val EXHALE_KEY = floatPreferencesKey("exhale_time")
    private val PAUSE_KEY = floatPreferencesKey("pause_time")

    // for panchangam storage
    private val PREVIOUS_PANCHANGAM = stringPreferencesKey("previous_panchangam")


    // --- Save/Retrieve Functions ---

//    fun getPreviousPanchangam(): Flow<PanchangaAttr.DynamicPanchangam?> = dataStore.data
//        .map { preferences ->
//            val jsonString = preferences[PREVIOUS_PANCHANGAM]
//            if (jsonString != null) {
//                // Using Gson (or your preferred JSON parser)
//                Gson().fromJson(jsonString, PanchangaAttr.DynamicPanchangam::class.java)
//            } else {
//                null
//            }
//        }
    // Example for Gson / Moshi deserialization inside PreferencesManager
    fun getPreviousPanchangam(): Flow<PanchangaAttr.DynamicPanchangam?> {
        return dataStore.data.map { preferences ->
            val json = preferences[PREVIOUS_PANCHANGAM] ?: return@map null
            try {
                val parsed = gson.fromJson(json, PanchangaAttr.DynamicPanchangam::class.java)

                // 🔑 Sanity check: Ensure nested LocalDateTime fields are NOT null
                if (parsed?.calcDttm == null || parsed.expiryDttm == null) {
                    null
                } else {
                    parsed
                }
            } catch (e: Exception) {
                Log.e("PreferencesManager", "Failed to parse saved DynamicPanchangam", e)
                null
            }
        }
    }

    private val dataStoreMutex = Mutex()

    suspend fun setPreviousPanchangam(panchangam: PanchangaAttr.DynamicPanchangam) {

        // 🔑 2. MUTEX: Lock out concurrent DataStore reads while editing
        dataStoreMutex.withLock {
            try {
                val jsonString = gson.toJson(panchangam)
                Log.d("PreferencesManager", "Successfully parsed DynamicPanchangam ${jsonString}")

                dataStore.edit { preferences ->
                    preferences[PREVIOUS_PANCHANGAM] = jsonString
                }
                Log.d("PreferencesManager", "Successfully saved DynamicPanchangam to DataStore")
            } catch (e: Exception) {
                Log.e("PreferencesManager", "Failed to write DynamicPanchangam to DataStore", e)
            }
        }
    }

    // --- Set/Save Function ---
//    suspend fun setPreviousPanchangam(panchangam: PanchangaAttr.DynamicPanchangam) {
//        val jsonString = Gson().toJson(panchangam)
//        dataStore.edit { preferences ->
//            preferences[PREVIOUS_PANCHANGAM] = jsonString
//        }
//    }

    // --- Clear Function (Optional) ---
    suspend fun clearPreviousPanchangam() {
        dataStore.edit { preferences ->
            preferences.remove(PREVIOUS_PANCHANGAM)
        }
    }

    suspend fun saveBreathingTimings(inhale: Float, hold: Float, exhale: Float, pause: Float) {
        dataStore.edit { preferences ->
            preferences[INHALE_KEY] = inhale
            preferences[HOLD_KEY] = hold
            preferences[EXHALE_KEY] = exhale
            preferences[PAUSE_KEY] = pause
        }
    }

    fun getInhaleTime(): Flow<Float> = dataStore.data.map { preferences ->
        preferences[INHALE_KEY] ?: 6f
    }
    fun getHoldTime(): Flow<Float> = dataStore.data.map { preferences ->
        preferences[HOLD_KEY] ?: 10.5f
    }
    fun getExhaleTime(): Flow<Float> = dataStore.data.map { preferences ->
        preferences[EXHALE_KEY] ?: 12f
    }
    fun getPauseTime(): Flow<Float> = dataStore.data.map { preferences ->
        preferences[PAUSE_KEY] ?: 0.01f
    }

    fun getBreathingTimings(): Flow<BreathingTimings> = dataStore.data.map { preferences ->
        BreathingTimings(
            inhale = preferences[INHALE_KEY] ?: 6f,
            hold = preferences[HOLD_KEY] ?: 10.5f,
            exhale = preferences[EXHALE_KEY] ?: 12f,
            pause = preferences[PAUSE_KEY] ?: 0.01f
        )
    }

    suspend fun saveCustomerInfo(info: CustomerInfo) {
        dataStore.edit { preferences ->
            preferences[CUSTOMER_NAME] = info.name
            preferences[CUSTOMER_EMAIL] = info.email
            preferences[CUSTOMER_PHONE] = info.phone
            preferences[CUSTOMER_ADDRESS1] = info.address1
            preferences[CUSTOMER_ADDRESS2] = info.address2
            preferences[CUSTOMER_CITY] = info.city
            preferences[CUSTOMER_STATE] = info.state
            preferences[CUSTOMER_PINCODE] = info.pincode
            preferences[CUSTOMER_DTOB_KEY] = info.dttmOfBirth
            preferences[CUSTOMER_LAT_KEY] = info.lat
            preferences[CUSTOMER_LON_KEY] = info.lon
            preferences[CUSTOMER_RASI_KEY] = info.rasi.name // 🔑 Save enum .name string
        }
        Log.d("PreferencesManager", "Saved CustomerInfo: $info")
    }

    fun getCustomerInfo(): Flow<CustomerInfo> = dataStore.data.map { preferences ->
        val rasiString = preferences[CUSTOMER_RASI_KEY] ?: Rasi.MESHA.name
        val parsedRasi = runCatching { Rasi.valueOf(rasiString) }.getOrDefault(Rasi.MESHA)

        CustomerInfo(
            name = preferences[CUSTOMER_NAME] ?: "",
            email = preferences[CUSTOMER_EMAIL] ?: "",
            phone = preferences[CUSTOMER_PHONE] ?: "",
            address1 = preferences[CUSTOMER_ADDRESS1] ?: "",
            address2 = preferences[CUSTOMER_ADDRESS2] ?: "",
            city = preferences[CUSTOMER_CITY] ?: "",
            state = preferences[CUSTOMER_STATE] ?: "",
            pincode = preferences[CUSTOMER_PINCODE] ?: "",
            dttmOfBirth = preferences[CUSTOMER_DTOB_KEY] ?: "",
            lat = preferences[CUSTOMER_LAT_KEY] ?: "",
            lon = preferences[CUSTOMER_LON_KEY] ?: "",
            rasi = parsedRasi // 🔑 Cleanly parsed enum
        )
    }

    // Retrieve counter value
    fun getAndroidLoginEmail(): Flow<String> = dataStore.data
        .map { preferences ->
            preferences[FIREBASE_EMAIL_ID] ?: "couldnt@get.email" // Default to 0 if not found
        }

    // NEW: Functions for managing deleted temple IDs
    suspend fun addDeletedTempleId(templeId: String) {
        dataStore.edit { preferences ->
            val currentIdsString = preferences[DELETED_TEMPLE_IDS_KEY] ?: ""
            val currentIds = currentIdsString.split(",").filter { it.isNotBlank() }.toMutableSet() // Use a Set for uniqueness
            if (currentIds.add(templeId)) { // Add returns true if element was added (i.e., not a duplicate)
                preferences[DELETED_TEMPLE_IDS_KEY] = currentIds.joinToString(",")
                Log.d("PreferencesManager", "Added deleted temple ID: $templeId. Current list: ${preferences[DELETED_TEMPLE_IDS_KEY]}")
            } else {
                Log.d("PreferencesManager", "Temple ID $templeId already in deleted list. No change.")
            }
        }
    }

    fun getDeletedTempleIds(): Flow<String> = dataStore.data
        .map { preferences ->
            preferences[DELETED_TEMPLE_IDS_KEY] ?: ""
        }

    suspend fun clearDeletedTempleIds() {
        dataStore.edit { preferences ->
            preferences[DELETED_TEMPLE_IDS_KEY] = ""
            Log.d("PreferencesManager", "Cleared all deleted temple IDs.")
        }
    }
    // Save counter value
    suspend fun saveCounterValue(count: Int) {
        dataStore.edit { preferences ->
            preferences[COUNTER_KEY] = count
        }
//        Log.d("PreferencesManager", "Saved counter value: $count")
    }

    // Retrieve counter value
    fun getCounterValue(): Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[COUNTER_KEY] ?: 0 // Default to 0 if not found
        }

    suspend fun saveSelectedLanguage(lang: NDLanguage) {
        dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE] = lang.name
        }
    }

    // Update the getter to return a Flow of the Enum
    fun getSelectedLanguage(): Flow<NDLanguage> = dataStore.data
        .map { preferences ->
            val langName = preferences[SELECTED_LANGUAGE] ?: NDLanguage.EN.name
            try {
                NDLanguage.valueOf(langName)
            } catch (e: Exception) {
                NDLanguage.EN
            }
        }

    fun getSavedLanguageDirectly(): NDLanguage {
        return runBlocking { // 🔑 Temporarily blocks to get the snapshot
            val preferences = dataStore.data.first() // Grab only the first emission
            val langName = preferences[SELECTED_LANGUAGE] ?: NDLanguage.EN.name
            try {
                NDLanguage.valueOf(langName)
            } catch (e: Exception) {
                NDLanguage.EN
            }
        }
    }

    // Function to store early reminder
    suspend fun saveEarlyReminder(days: Int) {
        dataStore.edit { preferences ->
            preferences[EARLY_REMINDER_KEY] = days
        }
//        Log.d("PreferencesManager", "Saved early reminder: $days days")
    }

    // Function to store alert interval
    suspend fun saveAlertInterval(interval: Int) {
        dataStore.edit { preferences ->
            preferences[ALERT_INTERVAL_KEY] = interval
        }
//        Log.d("PreferencesManager", "Saved alert interval: $interval")
    }

    // Function to store final count
    suspend fun saveFinalCount(count: Int) {
        dataStore.edit { preferences ->
            preferences[FINAL_COUNT_KEY] = count
        }
//        Log.d("PreferencesManager", "Saved final count: $count")
    }

    // Function to store color code visited setting
    suspend fun saveHideVisitedTemples(isChecked: Boolean) {
        dataStore.edit { preferences ->
            preferences[HIDE_VISITED_TEMPLE] = isChecked
        }
        Log.d("PreferencesManager", "Saved hide visited temple: $isChecked")
    }

    // Function to store color code visited setting
    suspend fun saveShowOnlyMarkedTemples(isChecked: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_ONLY_MARKED_TEMPLE] = isChecked
        }
        Log.d("PreferencesManager", "Saved show only marked temple: $isChecked")
    }

    // Function to retrieve early reminder value
    fun getEarlyReminder(): Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[EARLY_REMINDER_KEY] ?: Constants.DEFAULT_EARLY_REMINDER_DAYS // default to 1 day
        }

    // Function to retrieve alert interval value
    fun getAlertInterval(): Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[ALERT_INTERVAL_KEY] ?: Constants.DEFAULT_ALERT_INTERVAL // default to 7
        }

    // Function to retrieve final count value
    fun getFinalCount(): Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[FINAL_COUNT_KEY] ?: Constants.DEFAULT_FINAL_COUNT // default to 14
        }

    // Function to retrieve color code visited setting
    fun getHideVisitedTemples(): Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[HIDE_VISITED_TEMPLE] ?: true // default to true
        }

    // Function to retrieve color code visited setting
    fun getShowOnlyMarkedTemples(): Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SHOW_ONLY_MARKED_TEMPLE] ?: true // default to true
        }
}
