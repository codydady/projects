package com.sd.nithyadharma.dao

import android.util.Log
import com.sd.nithyadharma.cards.CardFactory
import com.sd.nithyadharma.cards.CardMode
import com.sd.nithyadharma.cards.CardModel
import com.sd.nithyadharma.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object CardRepository {

    private val _activeCards = MutableStateFlow<List<CardModel>>(emptyList())
    val activeCards: StateFlow<List<CardModel>> = _activeCards.asStateFlow()

    suspend fun loadSavedCards(preferencesManager: PreferencesManager) {
        Log.d("CardRepository", "loadSavedCards called upon app launch or resume")

        val savedCardJsons: List<String> = preferencesManager.getAllCardsMetadata().first()
        val recreatedCards = savedCardJsons.mapNotNull { jsonString ->
            runCatching {
                val cm = Json.Default.decodeFromString(CardModel.serializer(), jsonString)
                CardFactory.makeCard(
                    mode = CardMode.READ,
                    type = cm.type,
                    serializedJson = jsonString
                )
            }.getOrNull()
        }.sortedByDescending { it.createdTime }

        _activeCards.update { currentMemoryCards ->
            val diskCardIds = recreatedCards.map { it.id }.toSet()
            val rescuedCards = currentMemoryCards.filterNot { it.id in diskCardIds }

            if (rescuedCards.isNotEmpty()) {
                Log.w(
                    "CardRepository",
                    "⚡ RACE CONDITION DETECTED & RESCUED! " +
                            "The following ${rescuedCards.size} card(s) were generated in memory " +
                            "before disk load finished: ${rescuedCards.map { it.id }}"
                )
            } else {
                Log.d("CardRepository", "Clean load: No in-memory race condition detected.")
            }

            // 🔑 Use nullsLast() to prevent NPE during comparison
            try {
                val sortedList = (currentMemoryCards + recreatedCards)
                    .distinctBy { it.id }
                    .sortedWith(
                        compareByDescending<CardModel, Boolean?>(nullsLast()) { it.isPinned }
                            .thenByDescending(nullsLast()) { it.createdTime }
                    )

                sortedList
            } catch (e: Exception) {
                Log.e("CardRepository", "&&&&&&Error combining and sorting active cards", e)
                currentMemoryCards // Fallback to avoid crashing app
            }
        }
    }

    suspend fun purgeExpiredCards(preferencesManager: PreferencesManager) {
        val currentTime = System.currentTimeMillis()
        val removedCards = mutableListOf<CardModel>()

        _activeCards.update { currentList ->
            val (validCards, expiredCards) = currentList.partition { card ->
                card.isPinned || card.expiryTime > currentTime
            }
            removedCards.addAll(expiredCards)
            validCards
        }

        removedCards.forEach { expiredCard ->
            preferencesManager.removeCardMetadata(expiredCard.id)
        }

        if (removedCards.isNotEmpty()) {
            Log.d("CardRepository", "Purge triggered | Removed ${removedCards.size} expired card(s) from UI and DataStore.")
        }
    }

    suspend fun saveAndAddCard(preferencesManager: PreferencesManager, newCard: CardModel) {
        val jsonPayload: String? = try {
            Json.Default.encodeToString(newCard)
        } catch (e: Exception) {
            null
        }

        if (jsonPayload != null) {
            preferencesManager.saveCardMetadata(newCard.id, jsonPayload)
        }

        _activeCards.update { currentList ->
            (listOf(newCard) + currentList)
                .distinctBy { it.id }
                .sortedWith(
                    compareByDescending<CardModel> { it.isPinned }
                        .thenByDescending { it.createdTime }
                )
        }
        Log.i("CardRepository", "card saved and added to active cards")

    }

    suspend fun dismissAndRemoveCard(preferencesManager: PreferencesManager, cardId: String) {
        Log.i("CardRepository", "entered dismissAndRemoveCard  cardid - ${cardId}")
        // 1. Remove from DataStore / Disk first
        preferencesManager.removeCardMetadata(cardId)

        // 2. Remove from Memory StateFlow safely
        _activeCards.update { currentCards ->
            try {
                currentCards.filterNot { it.id == cardId }
            } catch (e: NullPointerException) {
                Log.e("CardRepository", "NPE during card removal equality check, filtering safely", e)
                // Fallback: Manually filter by index/id to avoid broken .equals() calls
                val newList = mutableListOf<CardModel>()
                for (card in currentCards) {
                    if (card.id != cardId) {
                        newList.add(card)
                    }
                }
                newList
            }
        }
    }

    fun togglePinCard(cardId: String) {
        _activeCards.update { currentList ->
            currentList.map { card ->
                if (card.id == cardId) card.copy(isPinned = !card.isPinned) else card
            }.sortedByDescending { it.isPinned }
        }
    }
}