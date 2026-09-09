package com.sd.nithyadharma.dao

import android.util.Log
import com.sd.nithyadharma.model.PanchangaAttr
import com.sd.nithyadharma.util.CommonFunctions
import com.sd.nithyadharma.util.Constants
import com.sd.nithyadharma.util.PanchangamCalculator
import com.sd.nithyadharma.util.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PanchangamRepository {

    // for sp
    private val _staticPanchangam = MutableStateFlow<PanchangaAttr.StaticPanchangam?>(null)
    val staticPanchangam: StateFlow<PanchangaAttr.StaticPanchangam?> = _staticPanchangam.asStateFlow()

    /**
     * Calculates SP for the current date, updates StateFlow for MainScreen,
     * and generates the Naal Kaatti card.
     */
    suspend fun generateStaticPanchangam():
            PanchangaAttr.StaticPanchangam {
        Log.d("PanchangamRepository", "generateStaticPanchangam called upon app launch or dawn alarm")

        // todo all of these methods use a common function in the constants or general helper object
        val now = CommonFunctions.getCurrentTime()
        val calculatedSp = PanchangamCalculator.calculateStaticPanchangamDetails(now)

        // 2. Update memory cache so active collectors get notified instantly
        _staticPanchangam.value = calculatedSp

        return calculatedSp
    }

    // for dp
    // imp , this is interesting. dp aint a local object in this repo.
    // it comes from preferences which is set by every exp-refresh call
    // Pure, cold/hot Flow provided directly from the data source

    fun getDynamicPanchangam(preferencesManager: PreferencesManager):
            Flow<PanchangaAttr.DynamicPanchangam?> {
        return preferencesManager.getPreviousPanchangam()
    }

    // for fp
    private val _futurePanchangam =
        MutableStateFlow<List<Pair<PanchangaAttr.StaticPanchangam, PanchangaAttr.DynamicPanchangam>>?>(null)
    val futurePanchangam: StateFlow<List<Pair<PanchangaAttr.StaticPanchangam, PanchangaAttr.DynamicPanchangam>>?>
            = _futurePanchangam.asStateFlow()

    suspend fun refreshFuturePanchangam(): List<Pair<PanchangaAttr.StaticPanchangam, PanchangaAttr.DynamicPanchangam>> {
        val futureList = PanchangamCalculator.calculateFuturePanchangam(
                            days = Constants.FUTURE_PANCHANGAM_CALCULATION_DAYS )
        _futurePanchangam.value = futureList
        return futureList
    }

}