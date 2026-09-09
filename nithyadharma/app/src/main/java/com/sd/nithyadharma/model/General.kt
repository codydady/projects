package com.sd.nithyadharma.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sd.nithyadharma.R
import com.sd.nithyadharma.model.PanchangaAttr.Rasi
import com.sd.nithyadharma.util.Constants
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.LocalTime
//
//enum class NDLanguage(val displayName: String) {
//    EN("English"),
//    TA("தமிழ்"),
//    KA("ಕನ್ನಡ"),
//    HI("हिन्दी")
//}
enum class NDLanguage(
    val displayName: String,
    val shortName: String
) {
    EN("English", "En"),           // English short name
    TA("தமிழ்", "த"),               // Tamil short: "த"
    KA("ಕನ್ನಡ", "ಕ"),               // Kannada short: "ಕ"
    HI("हिन्दी", "हि")              // Hindi short: "हि"
}
data class TimeRange(
    val start: LocalDateTime,
    val end: LocalDateTime
)

data class TimeWindow(
    val start: LocalTime,
    val end: LocalTime
)

// for puja product order
data class Product(
    val name: String,
    val price: Int,
    val imageRes: Int // Add image resource ID
)
// for puja product order
data class Order(
    val customer: CustomerInfo,
    val items: Map<Product, Int>, // Product -> Quantity
    val shippingCost: Int
) {
    // Computed properties
    val subtotal: Int
        get() = items.entries.sumOf { (product, qty) -> product.price * qty }

    val total: Int
        get() = subtotal + shippingCost
}

data class CustomerInfo(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address1: String = "",
    val address2: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val dttmOfBirth: String = "",
    val lat: String = "",
    val lon: String = "",
    val rasi: Rasi= Rasi.MESHA
) {
    companion object {
        val EMPTY = CustomerInfo()
    }
}

// for daily schedule
// todo , this must move to locale area folder or some utility class
@Serializable
data class ScheduleItem(
    val date: String,
    val occasionEn: String,
    val occasionTa: String,
    val occasionKa: String,
    val occasionHi: String,
    val remarks: String
)

// for templemap -
// UPDATED FOR ROOM DATABASE
@Entity(tableName = Constants.TABLE_NAME) // Specify table name for Room
data class TempleItem(
    @PrimaryKey // Marks temple_id as the primary key for Room
    val temple_id: String, // Keeping the Int type for consistency with your current field
    val name: String,
    val deity: String,
    val latlong: String,
    val tags: String?,
    val place: String?,
    val weight: Int,
    var visit_dt: String?, // Made nullable for "not visited" state, as requested
    val marked: String?,
)

// Data class to hold all timings together (optional but useful)
data class BreathingTimings(
    val inhale: Float,
    val hold: Float,
    val exhale: Float,
    val pause: Float
)

@Serializable
data class TimeSlotConfig(
    val slotId: String,
    val startTime: String,
    val endTime: String,
    val gradientColorHex1: String,
    val gradientColorHex2: String,
    val textColorHex: String
) {
    fun isTimeInSlot(time: LocalTime): Boolean {
        return try {
            val start = LocalTime.parse(startTime)
            val end = LocalTime.parse(endTime)

            if (start.isBefore(end)) {
                !time.isBefore(start) && time.isBefore(end)
            } else {
                !time.isBefore(start) || time.isBefore(end)
            }
        } catch (e: Exception) {
            false
        }
    }
}

// following variables and constants are for the new design
enum class Audio_Files(
    @RawRes val resId: Int,
    @DrawableRes val imageResId: Int
) {
    SUPRABHATHAM(
        resId = R.raw.suprabhatham,
        imageResId = R.drawable.nd_vishnu
    ),
    DAKSHINAMURTHY_STHOTHRAM(
        resId = R.raw.dakshinamurthy_stotram,
        imageResId = R.drawable.nd_dakshinamurthy
    ),
//    KAAKKUM_KADAVUL(
//        resId = R.raw.kaakkum_kadavul,
//        imageResId = R.drawable.nd_pillaiyar
//    ),
    KANDHA_SASHTI_KAVACHAM(
        resId = R.raw.kandha_sashti_kavacham,
        imageResId = R.drawable.nd_murugan
    ),
    KOLARU_PATHIGAM(
        resId = R.raw.kolaru_pathigam,
        imageResId = R.drawable.nd_shiva
    ),
    MAHISHASURA_MARDHINI(
        resId = R.raw.mahishasura_mardini,
        imageResId = R.drawable.nd_ambal
    ),
    NAMO_ANJANEYAM(
        resId = R.raw.namo_anjaneyam,
        imageResId = R.drawable.nd_hanuman
    ),
    PANCHAMIRDHA_VANNAM(
        resId = R.raw.panchamirdha_vannam,
        imageResId = R.drawable.nd_murugan
    ),
    RUDHRAM(
        resId = R.raw.rudram,
        imageResId = R.drawable.nd_shiva
    ),
//    SIVAMAYAMAAGHA_THERIGIRADHE(
//        resId = R.raw.sivamayamaga_therigiradhe,
//        imageResId = R.drawable.nd_shiva
//    ),
    VISHNU_SAHASRANAMAM(
        resId = R.raw.vishnu_sahasranamam,
        imageResId = R.drawable.nd_vishnu
    ),
//    OM_CHANTING(
//        resId = R.raw.om_chanting,
//        imageResId = R.drawable.nd_pillaiyar
//    ),
    RAMA_RAMA_JEYA_RAJARAM(
        resId = R.raw.rama_rama_jeya,
        imageResId = R.drawable.nd_vishnu
    ),
    DHATHATREYA_STHUTHI(
        resId = R.raw.dhathatreya_sthuthi,
        imageResId = R.drawable.nd_dattatreya
    )
}
