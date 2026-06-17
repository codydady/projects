import com.sd.nithyadharma.model.NDLanguage
import java.util.Formatter

/**
 * A robust Localization Registry
 */
object LocaleManager {
    private val templates = mapOf(
        "app_title" to mapOf(
            NDLanguage.EN to "NithyaDharma",
            NDLanguage.TA to "நித்ய தர்மா"
        ),
        "app_motto" to mapOf(
            NDLanguage.EN to "Dharmo Rakṣati Rakṣitaḥa\nAll things perish; Dharma alone endures",
            NDLanguage.TA to "அன்றறிவாம் என்னாது அறஞ்செய்க; மற்றது பொன்றுங்காற் பொன்றாத் துணை"
        ),
        "advance_title" to mapOf(
            NDLanguage.EN to "Advance yourself",
            NDLanguage.TA to "அறம் செய விரும்பு"
        ),
        "nextndays" to mapOf(
            NDLanguage.EN to "Next %s days",
            NDLanguage.TA to "அடுத்த %s நாட்கள்"
        ),
        "btn_hc" to mapOf(
            NDLanguage.EN to "Hindu Calendar",
            NDLanguage.TA to "பண்டிகை நாட்கள்"
        ),
        "btn_mc" to mapOf(
            NDLanguage.EN to "Manthra Counter",
            NDLanguage.TA to "மந்திர மாலை"
        ),
        "btn_tm" to mapOf(
            NDLanguage.EN to "Temple Guide",
            NDLanguage.TA to "கோவில் வழிகாட்டி"
        ),
        "btn_ll" to mapOf(
            NDLanguage.EN to "Light a Lamp",
            NDLanguage.TA to "தீபம் ஏற்றுவோம்"
        ),
        "btn_mp" to mapOf(
            NDLanguage.EN to "Manthra Player",
            NDLanguage.TA to "தெய்வீக இசை"
        ),
        "btn_pp" to mapOf(
            NDLanguage.EN to "Puja Products",
            NDLanguage.TA to "பூஜைப் பொருட்கள்"
        ),
        "btn_md" to mapOf(
            NDLanguage.EN to "Meditation Timer",
            NDLanguage.TA to "தியானம் பழக"
        ),
        "btn_pf" to mapOf(
            NDLanguage.EN to "Preferences",
            NDLanguage.TA to "விருப்பங்கள்"
        ),
        "btn_rh" to mapOf(
            NDLanguage.EN to "Request Help",
            NDLanguage.TA to "உதவி கோர"
        ),
        "btn_fb" to mapOf(
            NDLanguage.EN to "Feedback",
            NDLanguage.TA to "எதிரொலி"
        ),
        "btn_ab" to mapOf(
            NDLanguage.EN to "About",
            NDLanguage.TA to "எதற்கு இது"
        ),
        "btn_hr" to mapOf(
            NDLanguage.EN to "Horoscope",
            NDLanguage.TA to "ஜாதகம்"
        ),
        "str_nm" to mapOf(
            NDLanguage.EN to "Name",
            NDLanguage.TA to "பெயர்"
        ),
        "str_bdt" to mapOf(
            NDLanguage.EN to "Birth Date & Time",
            NDLanguage.TA to "பிறந்தநாள் நேரம்"
        ),
        "str_lat" to mapOf(
            NDLanguage.EN to "Latitude",
            NDLanguage.TA to "அட்ச ரேகை"
        ),
        "str_lon" to mapOf(
            NDLanguage.EN to "Longitude",
            NDLanguage.TA to "தீர்க்க ரேகை"
        ),
        "str_sunrise" to mapOf(
            NDLanguage.EN to "Sunrise",
            NDLanguage.TA to "சூர்யோதயம்"
        ),
        "str_sunset" to mapOf(
            NDLanguage.EN to "Sunset",
            NDLanguage.TA to "சூர்யாஸ்தமனம்"
        ),
        "str_sunriseset" to mapOf(
            NDLanguage.EN to "Sunrise/Sunset",
            NDLanguage.TA to "சூர்யோதய/அஸ்தமனம்"
        ),
        "str_rg" to mapOf(
            NDLanguage.EN to "Raahu Kaala",
            NDLanguage.TA to "ராகு காலம்"
        ),
        "str_ya" to mapOf(
            NDLanguage.EN to "Yama Ganda",
            NDLanguage.TA to "எம கண்டம்"
        ),
        "str_gk" to mapOf(
            NDLanguage.EN to "Gulikai",
            NDLanguage.TA to "குளிகை"
        ),
        "str_nn" to mapOf(
            NDLanguage.EN to "Auspicious Times",
            NDLanguage.TA to "நல்ல நேரம்"
        ),
        "str_tt" to mapOf(
            NDLanguage.EN to "Thithi",
            NDLanguage.TA to "திதி"
        ),
        "str_vr" to mapOf(
            NDLanguage.EN to "Day",
            NDLanguage.TA to "கிழமை"
        ),
        "str_nk" to mapOf(
            NDLanguage.EN to "Nakshatra",
            NDLanguage.TA to "நக்ஷத்ரம்"
        ),
        "str_yg" to mapOf(
            NDLanguage.EN to "Yoga",
            NDLanguage.TA to "யோகம்"
        ),
        "str_kr" to mapOf(
            NDLanguage.EN to "Karana",
            NDLanguage.TA to "கரணம்"
        ),
        "str_cr" to mapOf(
            NDLanguage.EN to "Chandrashtama",
            NDLanguage.TA to "சந்த்ராஷ்டமம்"
        ),
        "str_month" to mapOf(
            NDLanguage.EN to "Maasam",
            NDLanguage.TA to "மாதம்"
        ),
        "str_muhurtha" to mapOf(
            NDLanguage.EN to "Subha Muhurtham",
            NDLanguage.TA to "சுப முகூர்த்தம்"
        ),
        "str_st" to mapOf(
            NDLanguage.EN to "Status",
            NDLanguage.TA to "நிலை"
        ),
        "str_gd" to mapOf(
            NDLanguage.EN to "Good",
            NDLanguage.TA to "நன்று"
        ),
        "str_pr" to mapOf(
            NDLanguage.EN to "Poor",
            NDLanguage.TA to "மோசம்"
        ),
        "str_av" to mapOf(
            NDLanguage.EN to "Average",
            NDLanguage.TA to "சுமார்"
        ),
        "str_ex" to mapOf(
            NDLanguage.EN to "Excellent",
            NDLanguage.TA to "அருமை"
        ),
        "str_pd" to mapOf(
            NDLanguage.EN to "Paadha",
            NDLanguage.TA to "பாதம்"
        ),
        "str_today" to mapOf(
            NDLanguage.EN to "Today",
            NDLanguage.TA to "இன்று"
        ),
        "str_tomorrow" to mapOf(
            NDLanguage.EN to "Tomorrow",
            NDLanguage.TA to "நாளை"
        ),
        "str_timeend" to mapOf(
            NDLanguage.EN to "%1\$s, until %2\$s %3\$s %4\$s %5\$s",
            NDLanguage.TA to "%1\$s, %2\$s %3\$s %4\$s மணி வரை"
        ),
        "str_fut_timeend" to mapOf(
            NDLanguage.EN to "%1\$s, until %2\$s %3\$s %4\$s, then %5\$s",
            NDLanguage.TA to "%1\$s, %2\$s %3\$s மணி வரை, பின்னர் %5\$s"
        ),
        "str_currastrostatus" to mapOf(
            NDLanguage.EN to "From %1s %2s - %3s %4s, Status: %5s",
            NDLanguage.TA to "%1s %2s - %3s %4s வரை, நிலை: %5s"
        ),
        "str_currastrostatus_sameday" to mapOf(
            NDLanguage.EN to "From %1s %2s - %3s, Status: %5s",
            NDLanguage.TA to "%1s %2s - %3s வரை, நிலை: %5s"
        ),
        "about_title" to mapOf(
            NDLanguage.EN to "Why the App?",
            NDLanguage.TA to "எதற்காக இது?"
        ),
        "about_us" to mapOf(
            NDLanguage.EN to "Namaskar,\n\nAmidst incessant distractions from external media, the yearning for true happiness often slips the conscious thought. What ensues is a vicious cycle of endless consumption of content we don't need with time we don't have. It tires both physically and mentally. \n\nUnlike brute faith, Contemplation and Self Reflection are core Sanathanic traits. Without Gurus, we need some help illuminating a path towards a more mindful journey. Nithya Dharma aims to be your technological agent!\n\nConsuming is plodding whereas Dharma is momentous. It paves way for happiness. \n\n🙏",
            NDLanguage.TA to "வணக்கம்,\n\nவெளியிலிருந்து வரும் ஓயாத கவனச் சிதறல்களுக்கு மத்தியில், உண்மையான மகிழ்ச்சிக்கான ஏக்கம் பெரும்பாலும் நினைவில் இருப்பதில்லை. அதன் விளைவாக தேவையில்லாதவற்றைத் தொடர்ந்து உள்வாங்கிக் கொண்டே இருக்கும் குப்பைத் தொட்டியாக நாம் மாறி வருகிறோம். இது உடலையும் மனதையும் களைப்படையச் செய்கிறது.\n\nமூடநம்பிக்கையைப் போலல்லாமல், சிந்தனையும் சுய பரிசீலனையும் சனாதன தர்மத்தின் மையப் பண்புகளாகும். \n\nகுருக்கள் இல்லாத நிலையில், நாம் ஒரு சிந்தனைமிக்க பயணத்திற்கு வழிகாட்டும் ஒளியைத் தேட வேண்டியுள்ளது. நித்ய-தர்மா உங்கள் தொழில்நுட்ப உதவியாளராக இருக்க முயல்கிறது!\n\nவெற்று மத நம்பிக்கை என்பது மெதுவான பயணம்; ஆனால் தர்மம் என்பது வேகமானது, உயிர்த்துடிப்பானது. அது மகிழ்ச்சிக்கு வழிவகுக்கிறது. \n\n🙏"
        ),
        "about_other" to mapOf(
            NDLanguage.EN to "Our website: www.templepages.com\nVersion: %1s",
            NDLanguage.TA to "வலை தளம்: www.templepages.com\nபதிப்பு: %1s"
        ),
        "hc_bottom" to mapOf(
            NDLanguage.EN to "Dates are in accordance with Tamizh calendar. Alarm time ",
            NDLanguage.TA to "தமிழ்நாட்டு முறையில் தேதிகள் கணிக்கப்பட்டுள்ளன. நேரம் "
        ),
        "cmn_date" to mapOf(
            NDLanguage.EN to "Date",
            NDLanguage.TA to "தேதி"
        ),
        "cmn_occasion" to mapOf(
            NDLanguage.EN to "Occasion",
            NDLanguage.TA to "நிகழ்வு"
        ),
        "cmn_todaysevent" to mapOf(
            NDLanguage.EN to "Today!",
            NDLanguage.TA to "இன்று!"
        ),
        "cmn_tomorrowevent" to mapOf(
            NDLanguage.EN to "Tomorrow!",
            NDLanguage.TA to "நாளை!"
        ),
        "cmn_futureevent" to mapOf(
            NDLanguage.EN to "Down the line",
            NDLanguage.TA to "வரும் நாட்களில் ..."
        ),
        "hs_birthdtls" to mapOf(
            NDLanguage.EN to "Birth details",
            NDLanguage.TA to "பிறப்புத் தகவல்கள்"
        ),
        "hs_birthchart" to mapOf(
            NDLanguage.EN to "Birth Chart",
            NDLanguage.TA to "ஜாதகம்"
        ),
        "hs_dba" to mapOf(
            NDLanguage.EN to "Dasha Bukthi",
            NDLanguage.TA to "தசா புக்தி"
        ),
        "ct_finalct" to mapOf(
            NDLanguage.EN to "Final Count",
            NDLanguage.TA to "இறுதி எண்ணிக்கை"
        ),
        "ct_reset" to mapOf(
            NDLanguage.EN to "Reset",
            NDLanguage.TA to "மீட்டமைக்க"
        ),
        "ct_motto" to mapOf(
            NDLanguage.EN to "Keep count of your favorite manthram",
            NDLanguage.TA to "உங்களுக்கு பிடித்த மந்திரத்தை எண்ணிக்கொள்ளுங்கள்"
        ),
        "ll_bottom" to mapOf(
            NDLanguage.EN to "Lighting a dheepam at the sanctum blossoms the home with grace!",
            NDLanguage.TA to "கோயில் தீபம் வீட்டில் ஒளி"
        ),
        "ll_title" to mapOf(
            NDLanguage.EN to "Light a Lamp",
            NDLanguage.TA to "ஆலய தீபம் ஏற்றுவோம்"
        ),
        "ll_msg1" to mapOf(
            NDLanguage.EN to "Temples like this 1000-year-old temple once echoed with chants. Today, its walls crumble silently, with no funds even for oil or incense. Your contribution brings warmth to abandoned temples and hope to forgotten keepers.",
            NDLanguage.TA to "இத்தகைய ஆயிரம் ஆண்டு பழமையான கோயில்கள் ஒருகாலத்தில் மந்திர ஒலிகளால் எதிரொலித்தன.\n" +
                    "இன்று அதன் சுவர்கள் அமைதியாக இடிந்து கொண்டிருக்கின்றன — எண்ணெய் வாங்குவதற்கு கூட நிதி இல்லை, பூஜைக்கு தூபமும் இல்லை. கவனிப்பாரற்று விடப்பட்ட கோயில்களுக்கு உங்கள் பங்களிப்பு  தீபத்தையும், மறக்கப்பட்ட கோயில் பணியாளர்களுக்கு நம்பிக்கையையும் தரும்."
        ),
        "ll_contrib" to mapOf(
            NDLanguage.EN to "If you would like to contribute, pay to %1s " +
                    "( NithyaDharma Charitable Trust ) from any UPI app " +
                    "or scan the qr code provided above. You may whatsapp %2s with details " +
                    "so you will be notified when your contribution is used. ",
            NDLanguage.TA to "நீங்கள் தீபம் ஏற்ற விரும்பினால், %1s (நித்ய தர்மா அறக்கட்டளை) " +
                    "எனும் முகவரிக்கு பணம் செலுத்திவிட்டு " +
                    "%2s எனும் எண்ணிற்கு வாட்ஸ்அப் அனுப்பினால் " +
                    "உங்கள் அன்பளிப்பு பயன்படுத்தப்படும் போது தகவல் அனுப்பப்படும். "
        ),
        "ll_trustname" to mapOf(
            NDLanguage.EN to "NithyaDharma Charitable Trust",
            NDLanguage.TA to "நித்ய தர்மா அறக்கட்டளை"
        ),
        "ll_bankremit" to mapOf(
            NDLanguage.EN to "Alternately you may do old style remittance " +
                    "to our bank account %1s" ,
            NDLanguage.TA to "நீங்கள் வங்கிக் கணக்கில் பணம் செலுத்த விரும்பினால் " +
                    "\n%1s \nஎனும் எண்ணிற்கு பணம் அனுப்பலாம்."
        ),
        "ll_usage" to mapOf(
            NDLanguage.EN to "Your contribution provides for one of \n  " +
                    "a) Puja essentials like Oil, Vibhuthi, Kungumam, Rice etc\n  " +
                    "b) Services like Pradosham, Kruthikai, Shankatahara Chathurthi\n   " +
                    "c) Cleaning and Upkeep of rural temples",
            NDLanguage.TA to "உங்கள் பங்களிப்பு பின்வருவனவற்றில் ஒன்றுக்கு உதவுகிறது:\n " +
                    "அ) பூஜைக்கு தேவையான பொருட்கள் — எண்ணெய், விபூதி, குங்குமம், அரிசி போன்றவை\n " +
                    "ஆ) சிறப்பு சேவைகள் — பிரதோஷம், கிருத்திகை, சங்கடஹர சதுர்த்தி\n " +
                    "இ) கிராம கோயில்களின் சுத்தம் மற்றும் பராமரிப்பு"
        ),
        "mp_bottom" to mapOf(
            NDLanguage.EN to "Keep playing your favorite manthram",
            NDLanguage.TA to "விருப்பமானதைக் கேளுங்கள்"
        ),
        "mp_stop" to mapOf(
            NDLanguage.EN to "Stop",
            NDLanguage.TA to "நிறுத்த"
        ),
        "mp_ganesha" to mapOf(
            NDLanguage.EN to "Ganesha Manthra",
            NDLanguage.TA to "விநாயகர் துதி"
        ),
        "mp_murugan" to mapOf(
            NDLanguage.EN to "Shanmuga Manthra",
            NDLanguage.TA to "சண்முகர் துதி"
        ),
        "mp_vishnu" to mapOf(
            NDLanguage.EN to "Vishnu Manthra",
            NDLanguage.TA to "விஷ்ணு துதி"
        ),
        "mp_ddtrya" to mapOf(
            NDLanguage.EN to "Dhatrathreya Manthra",
            NDLanguage.TA to "தத்தாத்ரேயர் துதி"
        ),
        "mp_shiva" to mapOf(
            NDLanguage.EN to "Shiva Manthra",
            NDLanguage.TA to "சிவன் துதி"
        ),
        "mp_devi" to mapOf(
            NDLanguage.EN to "Devi Manthra",
            NDLanguage.TA to "அம்பிகை துதி"
        ),
        "mp_tara" to mapOf(
            NDLanguage.EN to "Tara Manthra",
            NDLanguage.TA to "தாரா துதி"
        ),
        "tm_title" to mapOf(
            NDLanguage.EN to "Temple Locator",
            NDLanguage.TA to "ஆலய வழிகாட்டி"
        ),
        "tm_bottom" to mapOf(
            NDLanguage.EN to "Click anywhere on map and press [icon] on top right to see temples there",
            NDLanguage.TA to "படத்தில் எங்கு வேண்டுமானாலும் கிளிக் செய்துவிட்டு [icon] ஐ அழுத்தவும்"
        ),
        "pp_title" to mapOf(
            NDLanguage.EN to "Puja Store",
            NDLanguage.TA to "பூஜைப் பொருட்கள்"
        ),
        "pp_discount" to mapOf(
            NDLanguage.EN to "10%% discount for app orders",
            NDLanguage.TA to "இங்கு வாங்கினால் 10%% தள்ளுபடி"
        ),
        "pp_custdtls" to mapOf(
            NDLanguage.EN to "Customer Details",
            NDLanguage.TA to "வாடிக்கையாளர் முகவரி்"
        ),
        "pf_lang" to mapOf(
            NDLanguage.EN to "Language",
            NDLanguage.TA to "மொழி"
        ),
        "pf_rasi" to mapOf(
            NDLanguage.EN to "Rasi",
            NDLanguage.TA to "ராசி"
        ),
        "pf_intercnt" to mapOf(
            NDLanguage.EN to "Chime every N count: %1s",
            NDLanguage.TA to "அடுக்கு எண்ணிக்கை: %1s"
        ),
        "pf_finalcnt" to mapOf(
            NDLanguage.EN to "Final Count: %1s",
            NDLanguage.TA to "இறுதி எண்ணிக்கை: %1s"
        ),
        "rh_title" to mapOf(
            NDLanguage.EN to "Submit Request for Oil+",
            NDLanguage.TA to "உதவி கோருங்கள்"
        ),
        "rh_bottom" to mapOf(
            NDLanguage.EN to "All fields must be entered and also Name in Preferences",
            NDLanguage.TA to "அனைத்து தகவல்களையும் நிரப்புக மற்றும் விருப்பங்களில் பெயரை நிரப்பவும்"
        ),
        "fb_title" to mapOf(
            NDLanguage.EN to "Customer",
            NDLanguage.TA to "வாடிக்கையாளர்"
        ),
        "fb_bottom" to mapOf(
            NDLanguage.EN to "Name must be set in preferences",
            NDLanguage.TA to "விருப்பங்களில் வாடிக்கையாளர் பெயரை நிரப்பவும்"
        ),
        "hr_bottom" to mapOf(
            NDLanguage.EN to "Set Name,Date and Time of birth, lat and long in preferences",
            NDLanguage.TA to "விருப்பங்களில் பெயர், பிறந்த தேதி/நேரம், அட்சரேகை, தீர்க்க ரேகை நிரப்பவும்"
        ),
        "cmn_products" to mapOf(
            NDLanguage.EN to "Products",
            NDLanguage.TA to "பொருட்கள்"
        ),
        "cmn_submit" to mapOf(
            NDLanguage.EN to "Submit",
            NDLanguage.TA to "அனுப்ப"
        ),
        "str_crtoday" to mapOf(
            NDLanguage.EN to "Chandrashtama Today",
            NDLanguage.TA to "இன்று சந்திராஷ்டமம்"
        ),
        "str_crdtl" to mapOf(
            NDLanguage.EN to "Its best to be cautious about big decisions today",
            NDLanguage.TA to "சந்திராஷ்டமத்தை அனுசரித்து முக்கிய முடிவுகள் எடுக்கவும்"
        ),
    )

    /**
     * Formats the string based on key and language.
     * Uses vararg to accept any number of placeholder values.
     */
    fun getString(key: String, lang: NDLanguage, vararg args: Any): String {
        val template = templates[key]?.get(lang) ?: return "Key Not Found: $key"

        return try {
            // Using java.util.Formatter for cross-platform compatibility
            val sb = StringBuilder()
            val formatter = Formatter(sb)
            formatter.format(template, *args)
            sb.toString()
        } catch (e: Exception) {
            "Format Error: $template"
        }
    }
}
