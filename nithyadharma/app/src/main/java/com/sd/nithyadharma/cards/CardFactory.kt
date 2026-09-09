package com.sd.nithyadharma.cards

import android.util.Log
import com.sd.nithyadharma.util.CommonFunctions
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// sud i do these enums and dataclass inside the object ?
enum class CardType {
    MUSIC,         // WE WILL HAVE THE SONG NAME ALONE TO CREATE IN PAYLOAD
    RATING_SHARE,
    GREETING,
    TODAYS_DHARMA,
    COUNTER,
    RASI_PALAN,
    PUJA_STORE,
    HINDU_CALENDAR,
    LIGHT_A_LAMP,
    TEMPLE_NEEDS, // we can ignore recreation of this card as the user wouldve whatsapped rqst
    PANCHANGAM,
    DP_NOTIFICATION,
    MAP,        // aint used as a card, the screen is used by me for now.
    NAAL_KAATTI,
    FUTURE_N_DAYS
}

enum class CardMode {
    READ,  // supplied when app crashes and viewmodel reads activecards from prefs and recreates
    WRITE_FG,  // when menu items are clicked to create card
    WRITE_BG  // when bakground jobs like slot changer/daily refresh/alarmmgr create card
}

@Serializable
data class CardModel(
    val id: String,
    val loggingKey: String,
    val type: CardType,
    val isPinned: Boolean = false,
    val isCloseable: Boolean = true,
    val createdTime: Long , //= System.currentTimeMillis(),
    val expiryTime: Long , //= System.currentTimeMillis() + EXPIRY_OFFSET_SHORT/MED/LONG,
    val customParams: Map<String, JsonElement>? = null // 👈 Works for ANY card type - used with text, music, panchangam info cards
    /*  removed title and content as they are messing with serialisation.
        keepin it simble title is derived from the card type for localisation
        reasons and content is generated on the fly obviating their need!
        so it works well
    */
)


object CardFactory {

    // --- 1. CONSTANTS ---
    const val CARD_EXPIRY_OFFSET_LONG = 72 * 60 * 60 * 1000L // 72 hours used by temple of day
    const val CARD_EXPIRY_OFFSET_MEDIUM = 24 * 60 * 60 * 1000L // 1 day for normal cards
    const val CARD_EXPIRY_OFFSET_SHORT = 4 * 60 * 60 * 1000L // 2 hours for i donno

    fun makeCard(
        mode: CardMode,
        type: CardType,
        serializedJson: String? = null,  //imp populated on read only
        expiryOffsetMillis: Long? = null,
        customParams: Map<String, JsonElement>? = null // imp populated on write only
    ): CardModel {
        Log.d("CardFactory", "card mode ${mode} and type ${type} ")

        return when (mode) {
            CardMode.READ -> {
                requireNotNull(serializedJson) { "serializedJson cannot be null in READ mode" }
                /*  this line unmarshalls the serializedJson->CardModel
                    so no createCardWrapper is required in this step. but
                    loadSavedCards in mainviewmodel will just put the marshalled
                    json in this field. only 2 places
                 */

                Json.decodeFromString(CardModel.serializer(), serializedJson)
            }

            CardMode.WRITE_FG -> {
//                val payloadJson = customParams?.let { Json.encodeToString(it) }
                val type = requireNotNull(type) { "type cannot be null in WRITE_FG mode" }
                val expiryOffsetMillis = requireNotNull(expiryOffsetMillis) {
                    Log.e("CardFactory", "expiryOffsetMillis is null in WRITE_FG mode for new card of type $type")
                    "expiryOffsetMillis cannot be null in WRITE_FG mode"
                }
                createCardWrapper(
                    type = type,
                    customParams = customParams, // passing string as is as it comes encoded from map to json string
                    expiryOffsetMillis = expiryOffsetMillis
                )
            }

            CardMode.WRITE_BG -> TODO()
        }
    }
}

/**
 * Generates a unique random alphanumeric ID (e.g., "a7Xk9P2").
 */
private fun generateCardId(length: Int = 7): String {
    val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

/**
 * Formats timestamp to "dd MMM, hh:mm a" (e.g., "26 Mar, 11:30 PM")
 */
fun formatCardTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)
    return formatter.format(Date(timestamp))
}

fun Enum<*>.toEnglish(): String {
    return name.lowercase()
}

private fun createCardWrapper(
    type: CardType,
    isPinned: Boolean = false,
    isCloseable: Boolean = true,
    loggingKey: String = type.toEnglish(),
    expiryOffsetMillis: Long,
    customParams:  Map<String, JsonElement>? = null  //  its the responsibility of the caller to supply a json encoded string.
): CardModel {
    val currentTime = System.currentTimeMillis()

    val cardModel = CardModel(
        id = generateCardId(),
        loggingKey = loggingKey + CommonFunctions.getCurrentDate().toString(), // 2007-12-03.
        type = type,
        isPinned = isPinned,
        isCloseable = isCloseable,
        createdTime = currentTime,
        expiryTime = currentTime + expiryOffsetMillis,
        customParams = customParams
    )
    return cardModel
}




