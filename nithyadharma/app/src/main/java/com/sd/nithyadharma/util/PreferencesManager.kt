package com.sd.nithyadharma.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sd.nithyadharma.model.BreathingTimings
import com.sd.nithyadharma.model.CustomerInfo
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.model.Rasi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// Define a preferences key for Early, Near reminder, alertInterval, finalCount, and colorCodeVisited
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesManager(context: Context) {

    private val dataStore = context.dataStore

    // from firebase authentication
    private val FIREBASE_EMAIL_ID = stringPreferencesKey("firebase_auth_email")

    // Keys for storing data
    private val EARLY_REMINDER_KEY = intPreferencesKey("early_reminder")
    private val ALERT_INTERVAL_KEY = intPreferencesKey("alert_interval")
    private val FINAL_COUNT_KEY = intPreferencesKey("final_count")
    private val HIDE_VISITED_TEMPLE = booleanPreferencesKey("hide_visited_temples")
    private val SHOW_ONLY_MARKED_TEMPLE = booleanPreferencesKey("show_only_marked_temples")

    private val SELECTED_RASI_KEY = stringPreferencesKey("selected_rasi")

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

    // In PreferencesManager class:
// --- Breathing Timing Keys ---
    private val INHALE_KEY = floatPreferencesKey("inhale_time")
    private val HOLD_KEY = floatPreferencesKey("hold_time")
    private val EXHALE_KEY = floatPreferencesKey("exhale_time")
    private val PAUSE_KEY = floatPreferencesKey("pause_time")

// --- Save/Retrieve Functions ---

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
        }
        Log.d("PreferencesManager", "Saved CustomerInfo: $info")
    }

    fun getCustomerInfo(): Flow<CustomerInfo> = dataStore.data.map { preferences ->
        CustomerInfo(
            name = preferences[CUSTOMER_NAME] ?: "",
            email = preferences[CUSTOMER_EMAIL] ?: "",
            phone = preferences[CUSTOMER_PHONE] ?: "",
            address1 = preferences[CUSTOMER_ADDRESS1] ?: "",
            address2 = preferences[CUSTOMER_ADDRESS2] ?: "",
            city = preferences[CUSTOMER_CITY] ?: "",
            state = preferences[CUSTOMER_STATE] ?: "",
            pincode = preferences[CUSTOMER_PINCODE] ?: ""
        )
    }

    // Save counter value
//    suspend fun saveAndroidLoginEmail(email: String) {
//        dataStore.edit { preferences ->
//            preferences[FIREBASE_EMAIL_ID] = email
//        }
//        Log.d("PreferencesManager", "Saved FIREBASE_EMAIL_ID value: $email")
//    }

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

    /**
     * Accepts the Rasi Enum directly.
     * This prevents you from accidentally saving "Mesha" or "Aries".
     */
    suspend fun saveSelectedRasi(rasi: Rasi) {
        dataStore.edit { preferences ->
            preferences[SELECTED_RASI_KEY] = rasi.name // Saves "MESHA", "VRISHABHA", etc.
        }
    }

    /**
     * Returns a Flow of the Rasi Enum.
     * The conversion logic happens once here, so your ViewModel doesn't have to deal with Strings.
     */
    fun getSelectedRasi(): Flow<Rasi> = dataStore.data
        .map { preferences ->
            val rasiName = preferences[SELECTED_RASI_KEY] ?: "MESHA"
            try {
                Rasi.valueOf(rasiName)
            } catch (e: Exception) {
                Rasi.MESHA // Fallback if data is corrupted
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
