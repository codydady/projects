package com.sd.nithyadharma.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sd.nithyadharma.cards.CardFactory
import com.sd.nithyadharma.cards.CardMode
import com.sd.nithyadharma.cards.CardModel
import com.sd.nithyadharma.cards.CardType
import com.sd.nithyadharma.dao.CardRepository
import com.sd.nithyadharma.dao.PanchangamRepository
import com.sd.nithyadharma.model.PanchangaAttr.Rasi
import com.sd.nithyadharma.util.FirebaseAppAnalytics
import com.sd.nithyadharma.dao.PostOfDayRepository
import com.sd.nithyadharma.util.AlarmSlotNotificationHelpers
import com.sd.nithyadharma.util.PreferencesManager
import com.sd.nithyadharma.util.SimpleIdCipher
import com.sd.nithyadharma.util.SlotManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

class MainViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // todo works when someone clicks the logo - remove later
    fun testingDummyMusicCardMaker() {
        Log.d("MainViewModel", "testing dummy")
        val audioFile = Audio_Files.entries.random()

        val customParams = mapOf(
            "audioKey" to JsonPrimitive(audioFile.toString())
        )

        val card = CardFactory.makeCard(
            mode = CardMode.WRITE_FG,
            type = CardType.MUSIC,
            expiryOffsetMillis = CardFactory.CARD_EXPIRY_OFFSET_SHORT,
            customParams = customParams
        )
        CoroutineScope(Dispatchers.IO).launch {
            //val preferencesManager = PreferencesManager(appContext)
            CardRepository.saveAndAddCard(preferencesManager, card)
        }
    }  // testingDummy ends

    // todo works when someone clicks the title - remove later
    suspend fun testingDummyRatingCardMaker() {
        Log.d("MainViewModel", "testing testingDummyRatingCardMaker")
        AlarmSlotNotificationHelpers.addRatingCard(preferencesManager)
    }  // testingDummy ends

    // todo works when someone clicks the title - remove later
    suspend fun testingDummyRasiPalanCardMaker() {
        Log.d("MainViewModel", "testing testingDummyRasiPalanCardMaker")
        AlarmSlotNotificationHelpers.addRasiPalanCard(preferencesManager)
    }  // testingDummy ends


    // ----------------------------------
    // section 1 - customer info related
    // ----------------------------------

    val customerInfo: StateFlow<CustomerInfo?> = preferencesManager.getCustomerInfo()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun updateCustomerNameRasiLanguage(newName: String,
                                       newRasi: Rasi,
                                       language: NDLanguage) {
        viewModelScope.launch {
            val currentInfo = customerInfo.value ?: CustomerInfo.EMPTY
            val updatedInfo = currentInfo.copy(
                name = newName,
                rasi = newRasi
            )
            preferencesManager.saveCustomerInfo(updatedInfo)
            preferencesManager.saveSelectedLanguage(language)
        }
    }

    fun saveCustomerInfo(info: CustomerInfo) {
        viewModelScope.launch {
            preferencesManager.saveCustomerInfo(info)
        }
    }

    /**
     * Always fetches the latest data directly from Preferences/DataStore on-demand,
     * bypassing StateFlow caching or active UI subscription requirements.
     */
    suspend fun getCustomerInfo(): CustomerInfo? {
        return preferencesManager.getCustomerInfo().firstOrNull()
    }

    // ----------------------------------
    // section 2 - panchangam related
    // ----------------------------------

    // Automatically mirrors the repository's SP and keeps it active during UI lifespan
    // Direct reference to repository's in-memory StateFlow
    val staticPanchangam: StateFlow<PanchangaAttr.StaticPanchangam?> =
        PanchangamRepository.staticPanchangam

    init {
        viewModelScope.launch(Dispatchers.Default) {
            /*  imp the return value aint used here, will be used in future
                when sp gets built with customparams instead of argument sp
                to naalkaatti card. but since the following lines get called
                everytime i wake my phone , i am commenting them. its unnecessary.
                it should rather be replaced with a quiz or game card to intrigue
                the user
             */

            // 1. Recalculates fresh for 'now', updating PanchangamRepository._staticPanchangam
            PanchangamRepository.generateStaticPanchangam()
//            AlarmSlotNotificationHelpers.addNaalKaatiCardWithSP(preferencesManager)
//
//            // 2. we put a customer related card last because else
//            // the naal kaatti comes above greeting.
//            // lets make a greeting card once while the aqp launches here
//            AlarmSlotNotificationHelpers.addGreetingCard(preferencesManager)
        }
    }

    // instead of calling prefs directly to get dp, we call repo method ssot
    val dynamicPanchangam: StateFlow<PanchangaAttr.DynamicPanchangam?> =
        PanchangamRepository.getDynamicPanchangam(preferencesManager)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Direct reference to Repository's StateFlow—just like SP
    val futurePanchangam = PanchangamRepository.futurePanchangam

    init {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                PanchangamRepository.refreshFuturePanchangam()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error calculating future panchangam", e)
            }
        }
    }

//    init {
//        // 🔑 Calculate future panchangam on app startup
//        viewModelScope.launch(Dispatchers.Default) {
//            try {
//                val futureList = PanchangamRepository.calculateFuturePanchangam()
//                _futurePanchangam.value = futureList
//                Log.d("MainViewModel", "Future Panchangam loaded: ${futureList.size} days")
//            } catch (e: Exception) {
//                Log.e("MainViewModel", "Error calculating future panchangam", e)
//            }
//        }
//    }

    // ----------------------------------
    // section 3 - firebase related
    // ----------------------------------

    init {
        observeCustomerInfoForFirebase()
    }

    private fun observeCustomerInfoForFirebase() {
        viewModelScope.launch {
            customerInfo
                .mapNotNull { it?.name?.trim() }
                .filter { name -> name.isNotBlank() }
                .distinctUntilChanged()
                .collect { validName ->
                    val generatedId = SimpleIdCipher.encrypt(validName)
                    FirebaseAppAnalytics.setUserId(generatedId)
                }
        }
    }

    // ---------------------------------------
    // section 4 - slot configuration related
    // ---------------------------------------
    // 🔑 Single line: UI directly observes SlotManager's reactive state
    // rest of the logic moved to slotmanager file

    val currentSlot: StateFlow<TimeSlotConfig> = SlotManager.currentSlot

    // ----------------------------------
    // section 5 - language related
    // ----------------------------------

    val currentLang: StateFlow<NDLanguage> = preferencesManager.getSelectedLanguage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NDLanguage.EN
        )

    fun setLanguage(language: NDLanguage) {
        viewModelScope.launch {
            preferencesManager.saveSelectedLanguage(language)
        }
    }

    private var currentLanguage = NDLanguage.EN

    init {
        viewModelScope.launch {
            currentLang.collect { currentLanguage = it }
        }
    }

    // -------------------------------------------------------
    // section 6 - cards related (Delegated to CardRepository)
    // -------------------------------------------------------

    val activeCards: StateFlow<List<CardModel>> = CardRepository.activeCards

    init {
        viewModelScope.launch {
            CardRepository.loadSavedCards(preferencesManager)
        }
    }

    fun purgeExpiredCards() {
        viewModelScope.launch {
            CardRepository.purgeExpiredCards(preferencesManager)
        }
    }

    fun saveAndAddCard(newCard: CardModel) {
        viewModelScope.launch {
            CardRepository.saveAndAddCard(preferencesManager, newCard)
        }
    }

    fun dismissAndRemoveCard(cardId: String) {
        viewModelScope.launch {
            CardRepository.dismissAndRemoveCard(preferencesManager, cardId)
        }
    }

    fun togglePinCard(cardId: String) {
        CardRepository.togglePinCard(cardId)
    }

    // ----------------------------------
    // section 7 - counter related
    // ----------------------------------

    val counterValue: StateFlow<Int> = preferencesManager.getCounterValue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val alertInterval: StateFlow<Int> = preferencesManager.getAlertInterval()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 18)

    val finalCount: StateFlow<Int> = preferencesManager.getFinalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 36)

    fun updateCounter(newCount: Int) {
        viewModelScope.launch {
            preferencesManager.saveCounterValue(newCount)
        }
    }

    fun updateCounterSettings(interval: Int, target: Int) {
        viewModelScope.launch {
            preferencesManager.saveAlertInterval(interval)
            preferencesManager.saveFinalCount(target)
        }
    }

    // -------------------------------------
    // section 8 - post of the day  related
    // -------------------------------------

    val postOfDay = PostOfDayRepository.postOfDay

}
