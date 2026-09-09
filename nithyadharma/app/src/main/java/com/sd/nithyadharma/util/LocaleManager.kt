import com.sd.nithyadharma.model.NDLanguage
import java.util.Formatter

/**
 * A robust Localization Registry
 */
object LocaleManager {
    private val templates = mapOf(
        "app_title" to mapOf(
            NDLanguage.EN to "NithyaDharma",
            NDLanguage.TA to "நித்யதர்மா",
            NDLanguage.KA to "ನಿತ್ಯಧರ್ಮ",
            NDLanguage.HI to "नित्यधर्म"
        ),
        "app_motto" to mapOf(
            NDLanguage.EN to "Sanatana Wisdom for Daily Living",
            NDLanguage.TA to "அன்றறிவாம் என்னாது அறஞ்செய்க",
            NDLanguage.KA to "ದೈನಂದಿನ ಜೀವನಕ್ಕಾಗಿ ಸನಾತನ ಜ್ಞಾನ",
            NDLanguage.HI to "दैनिक जीवन के लिए सनातन ज्ञान"
        ),
        "greeting_motto" to mapOf(
            NDLanguage.EN to "Advance Yourself",
            NDLanguage.TA to "அறம் செய்ய விரும்பு",
            NDLanguage.KA to "ನಿಮ್ಮನ್ನು ನೀವೇ ಉನ್ನತಿಗೇರಿಸಿಕೊಳ್ಳಿ",
            NDLanguage.HI to "अपने आप को आगे बढ़ाएं"
        ),
        "greeting" to mapOf(
            NDLanguage.EN to "Hi there !",
            NDLanguage.TA to "வணக்கம்",
            NDLanguage.KA to "ನಮಸ್ಕಾರ!",
            NDLanguage.HI to "नमस्ते !"
        ),
        "good_morning" to mapOf(
            NDLanguage.EN to "Good Morning",
            NDLanguage.TA to "இனிய காலை",
            NDLanguage.KA to "ಶುಭೋದಯ",
            NDLanguage.HI to "शुभ प्रभात"
        ),
        "good_evening" to mapOf(
            NDLanguage.EN to "Good Evening",
            NDLanguage.TA to "இனிய மாலை",
            NDLanguage.KA to "ಶುಭ ಸಂಜೆ",
            NDLanguage.HI to "शुभ संध्या"
        ),
        "todays_dharma" to mapOf(
            NDLanguage.EN to "Dharma of the day",
            NDLanguage.TA to "இன்றைய ஆன்மீகம்",
            NDLanguage.KA to "ಇಂದಿನ ಧರ್ಮ",
            NDLanguage.HI to "आज का धर्म"
        ),
        "rating_share" to mapOf(
            NDLanguage.EN to "Rate Us & Share",
            NDLanguage.TA to "மதிப்பீடு மற்றும் பகிர",
            NDLanguage.KA to "ನಮ್ಮನ್ನು ರೇಟ್ ಮಾಡಿ ಮತ್ತು ಹಂಚಿಕೊಳ್ಳಿ",
            NDLanguage.HI to "हमें रेट करें और शेयर करें"
        ),
        "str_rate" to mapOf(
            NDLanguage.EN to "Rate Us",
            NDLanguage.TA to "மதிப்பிடுங்கள்",
            NDLanguage.KA to "ರೇಟ್ ಮಾಡಿ",
            NDLanguage.HI to "रेट करें"
        ),
        "str_share" to mapOf(
            NDLanguage.EN to "Share",
            NDLanguage.TA to "பகிருங்கள்",
            NDLanguage.KA to "ಹಂಚಿಕೊಳ್ಳಿ",
            NDLanguage.HI to "शेयर करें"
        ),
        "str_rate_share" to mapOf(
            NDLanguage.EN to "We hope you like this app.If so please rate and share! 🙏",
            NDLanguage.TA to "உங்களுக்கு இந்த ஆப் பிடித்திருந்தால் மதிப்பீடு செய்யவும் / பகிரவும். நன்றி 🙏",
            NDLanguage.KA to "ಈ ಆಪ್ ನಿಮಗೆ ಇಷ್ಟವಾದರೆ ದಯವಿಟ್ಟು ರೇಟ್ ಮಾಡಿ ಮತ್ತು ಹಂಚಿಕೊಳ್ಳಿ! 🙏",
            NDLanguage.HI to "आशा है आपको यह ऐप पसंद आया होगा। अगर हाँ तो कृपया रेट करें और शेयर करें! 🙏"
        ),
        "start_slot" to mapOf(
            NDLanguage.EN to "Let's play the song of the slot!",
            NDLanguage.TA to "இப்பொழுதிற்கான பாடலுடன் துவக்கலாமா!",
            NDLanguage.KA to "ಈ ಸಮಯದ ಹಾಡನ್ನು ಕೇಳುತ್ತಾ ಪ್ರಾರಂಭಿಸೋಣವೇ!",
            NDLanguage.HI to "आइए इस समय का गीत बजाएं!"
        ),
        "welcome_title" to mapOf(
            NDLanguage.EN to "Welcome!",
            NDLanguage.TA to "வணக்கம்!",
            NDLanguage.KA to "ಸ್ವಾಗತ!",
            NDLanguage.HI to "स्वागत है!"
        ),
        "welcome_subtitle" to mapOf(
            NDLanguage.EN to "Please enter your details for a personalized experience.",
            NDLanguage.TA to "தனிப்பட்ட அனுபவத்திற்கு உங்கள் விவரங்களை உள்ளிடவும்.",
            NDLanguage.KA to "ವೈಯಕ್ತಿಕ ಅನುಭವಕ್ಕಾಗಿ ದಯವಿಟ್ಟು ನಿಮ್ಮ ವಿವರಗಳನ್ನು ನಮೂದಿಸಿ.",
            NDLanguage.HI to "व्यक्तिगत अनुभव के लिए कृपया अपना विवरण दर्ज करें।"
        ),
        "your_name_label" to mapOf(
            NDLanguage.EN to "Your Name",
            NDLanguage.TA to "உங்கள் பெயர்",
            NDLanguage.KA to "ನಿಮ್ಮ ಹೆಸರು",
            NDLanguage.HI to "आपका नाम"
        ),
        "select_rasi_label" to mapOf(
            NDLanguage.EN to "Your Rasi",
            NDLanguage.TA to "உங்கள் ராசி",
            NDLanguage.KA to "ನಿಮ್ಮ ರಾಶಿ",
            NDLanguage.HI to "आपकी राशि"
        ),
        "err_fill_all_fields" to mapOf(
            NDLanguage.EN to "Please fill all fields",
            NDLanguage.TA to "அனைத்து விவரங்களையும் நிரப்பவும்",
            NDLanguage.KA to "ಎಲ್ಲಾ ವಿವರಗಳನ್ನು ಭರ್ತಿ ಮಾಡಿ",
            NDLanguage.HI to "कृपया सभी जानकारी भरें",
        ),
        "err_enter_temple_name" to mapOf(
            NDLanguage.EN to "Enter Temple Name",
            NDLanguage.TA to "கோவில் பெயரை உள்ளிடவும்",
            NDLanguage.KA to "ದೇವಸ್ಥಾನದ ಹೆಸರು ನಮೂದಿಸಿ",
            NDLanguage.HI to "मंदिर का नाम दर्ज करें",
        ),
        "err_enter_location" to mapOf(
            NDLanguage.EN to "Enter Place",
            NDLanguage.TA to "ஊர் பெயரை உள்ளிடவும்",
            NDLanguage.KA to "ಸ್ಥಳದ ಹೆಸರು ನಮೂದಿಸಿ",
            NDLanguage.HI to "स्थान दर्ज करें",
        ),
        "err_enter_requirement" to mapOf(
            NDLanguage.EN to "Enter Requirement",
            NDLanguage.TA to "தேவையை உள்ளிடவும்",
            NDLanguage.KA to "ಅಗತ್ಯವನ್ನು ನಮೂದಿಸಿ",
            NDLanguage.HI to "आवश्यकता दर्ज करें",
        ),
        "submit_btn" to mapOf(
            NDLanguage.EN to "Submit",
            NDLanguage.TA to "நுழைக",
            NDLanguage.KA to "ಸಲ್ಲಿಸಿ",
            NDLanguage.HI to "सबमिट करें"
        ),
        "advance_title" to mapOf(
            NDLanguage.EN to "Advance yourself",
            NDLanguage.TA to "அறம் செய விரும்பு",
            NDLanguage.KA to "ನಿಮ್ಮನ್ನು ನೀವೇ ಉನ್ನತಿಗೇರಿಸಿಕೊಳ್ಳಿ",
            NDLanguage.HI to "अपने आप को आगे बढ़ाएं"
        ),
        "nextndays" to mapOf(
            NDLanguage.EN to "Next %s days",
            NDLanguage.TA to "அடுத்த %s நாட்கள்",
            NDLanguage.KA to "ಮುಂದಿನ %s ದಿನಗಳು",
            NDLanguage.HI to "अगले %s दिन"
        ),
        "future_n_days" to mapOf(
            NDLanguage.EN to "Next 7 days",
            NDLanguage.TA to "அடுத்த 7 நாட்கள்",
            NDLanguage.KA to "ಮುಂದಿನ 7 ದಿನಗಳು",
            NDLanguage.HI to "अगले 7 दिन"
        ),
        "hindu_calendar" to mapOf(
            NDLanguage.EN to "Hindu Calendar",
            NDLanguage.TA to "பண்டிகை நாட்கள்",
            NDLanguage.KA to "ಹಿಂದೂ ಪಂಚಾಂಗ",
            NDLanguage.HI to "हिंदू कैलेंडर"
        ),
        "counter" to mapOf(
            NDLanguage.EN to "Manthra Counter",
            NDLanguage.TA to "மந்திர மாலை",
            NDLanguage.KA to "ಮಂತ್ರ ಎಣಿಕೆ",
            NDLanguage.HI to "मंत्र गणक"
        ),
        "btn_tm" to mapOf(
            NDLanguage.EN to "Temple Guide",
            NDLanguage.TA to "கோவில் வழிகாட்டி",
            NDLanguage.KA to "ದೇವಾಲಯ ಮಾರ್ಗದರ್ಶಿ",
            NDLanguage.HI to "मंदिर गाइड"
        ),
        "light_a_lamp" to mapOf(
            NDLanguage.EN to "Light a Lamp",
            NDLanguage.TA to "தீபம் ஏற்றுவோம்",
            NDLanguage.KA to "ದೀಪ ಬೆಳಗಿಸಿ",
            NDLanguage.HI to "दीपक जलाएं"
        ),
        "btn_mp" to mapOf(
            NDLanguage.EN to "Manthra Player",
            NDLanguage.TA to "தெய்வீக இசை",
            NDLanguage.KA to "ಮಂತ್ರ ಪ್ಲೇಯರ್",
            NDLanguage.HI to "मंत्र प्लेयर"
        ),
        "puja_store" to mapOf(
            NDLanguage.EN to "Puja Products",
            NDLanguage.TA to "பூஜைப் பொருட்கள்",
            NDLanguage.KA to "ಪೂಜಾ ಸಾಮಗ್ರಿಗಳು",
            NDLanguage.HI to "पूजा सामग्री"
        ),
        "btn_md" to mapOf(
            NDLanguage.EN to "Meditation Timer",
            NDLanguage.TA to "தியானம் பழக",
            NDLanguage.KA to "ಧ್ಯಾನ ಟೈಮರ್",
            NDLanguage.HI to "ध्यान टाइमर"
        ),
        "btn_pf" to mapOf(
            NDLanguage.EN to "Preferences",
            NDLanguage.TA to "விருப்பங்கள்",
            NDLanguage.KA to "ಆದ್ಯತೆಗಳು",
            NDLanguage.HI to "प्राथमिकताएँ"
        ),
        "temple_needs" to mapOf(
            NDLanguage.EN to "Temple Needs",
            NDLanguage.TA to "கோவில் உதவி",
            NDLanguage.KA to "ದೇವಾಲಯದ ಅಗತ್ಯಗಳು",
            NDLanguage.HI to "मंदिर की आवश्यकताएँ"
        ),
        "btn_fb" to mapOf(
            NDLanguage.EN to "Feedback",
            NDLanguage.TA to "எதிரொலி",
            NDLanguage.KA to "ಪ್ರತಿಕ್ರಿಯೆ",
            NDLanguage.HI to "प्रतिक्रिया"
        ),
        "btn_ab" to mapOf(
            NDLanguage.EN to "About",
            NDLanguage.TA to "எதற்கு இது",
            NDLanguage.KA to "ಈ ಆಪ್ ಬಗ್ಗೆ",
            NDLanguage.HI to "इस ऐप के बारे में"
        ),
        "btn_hr" to mapOf(
            NDLanguage.EN to "HoroscopeAttr",
            NDLanguage.TA to "ஜாதகம்",
            NDLanguage.KA to "ಜಾತಕ",
            NDLanguage.HI to "जातक / कुंडली"
        ),
        "str_nm" to mapOf(
            NDLanguage.EN to "Name",
            NDLanguage.TA to "பெயர்",
            NDLanguage.KA to "ಹೆಸರು",
            NDLanguage.HI to "नाम"
        ),
        "str_bdt" to mapOf(
            NDLanguage.EN to "Birth Date & Time",
            NDLanguage.TA to "பிறந்தநாள் நேரம்",
            NDLanguage.KA to "ಜನ್ಮ ದಿನಾಂಕ ಮತ್ತು ಸಮಯ",
            NDLanguage.HI to "जन्म तिथि और समय"
        ),
        "str_lat" to mapOf(
            NDLanguage.EN to "Latitude",
            NDLanguage.TA to "அட்ச ரேகை",
            NDLanguage.KA to "ಅಕ್ಷಾಂಶ",
            NDLanguage.HI to "अक्षांश"
        ),
        "str_lon" to mapOf(
            NDLanguage.EN to "Longitude",
            NDLanguage.TA to "தீர்க்க ரேகை",
            NDLanguage.KA to "ರೇಖಾಂಶ",
            NDLanguage.HI to "देशांतर"
        ),
        "str_sunrise" to mapOf(
            NDLanguage.EN to "Sunrise",
            NDLanguage.TA to "உதயம்",
            NDLanguage.KA to "ಸೂರ್ಯೋದಯ",
            NDLanguage.HI to "सूर्योदय"
        ),
        "str_sunset" to mapOf(
            NDLanguage.EN to "Sunset",
            NDLanguage.TA to "அஸ்தமனம்",
            NDLanguage.KA to "ಸೂರ್ಯಾಸ್ತ",
            NDLanguage.HI to "सूर्यास्त"
        ),
        "str_sunrisefull" to mapOf(
            NDLanguage.EN to "Sunrise",
            NDLanguage.TA to "சூர்யோதயம்",
            NDLanguage.KA to "ಸೂರ್ಯೋದಯ",
            NDLanguage.HI to "सूर्योदय"
        ),
        "str_sunsetfull" to mapOf(
            NDLanguage.EN to "Sunset",
            NDLanguage.TA to "சூர்யாஸ்தமனம்",
            NDLanguage.KA to "ಸೂರ್ಯಾಸ್ತ",
            NDLanguage.HI to "सूर्यास्त"
        ),
        "str_sunriseset" to mapOf(
            NDLanguage.EN to "Sunrise/Sunset",
            NDLanguage.TA to "சூர்யோதய/அஸ்தமனம்",
            NDLanguage.KA to "ಸೂರ್ಯೋದಯ / ಸೂರ್ಯಾಸ್ತ",
            NDLanguage.HI to "सूर्योदय / सूर्यास्त"
        ),
        "str_rg" to mapOf(
            NDLanguage.EN to "Raahu Kaala",
            NDLanguage.TA to "ராகு காலம்",
            NDLanguage.KA to "ರಾಹುಕಾಲ",
            NDLanguage.HI to "राहु काल"
        ),
        "str_arambam" to mapOf(
            NDLanguage.EN to "Started",
            NDLanguage.TA to "ஆரம்பம்",
            NDLanguage.KA to "ಪ್ರಾರಂಭ",
            NDLanguage.HI to "आरंभ"
        ),
        "str_mudivu" to mapOf(
            NDLanguage.EN to "Ended",
            NDLanguage.TA to "முடிந்துவிட்டது",
            NDLanguage.KA to "ಮುಕ್ತಾಯ",
            NDLanguage.HI to "समाप्त"
        ),
        "str_until" to mapOf(
            NDLanguage.EN to "Until",
            NDLanguage.TA to "முடிவு",
            NDLanguage.KA to "ತನಕ",
            NDLanguage.HI to "तक"
        ),
        "str_ya" to mapOf(
            NDLanguage.EN to "Yama Ganda",
            NDLanguage.TA to "எம கண்டம்",
            NDLanguage.KA to "ಯಮಗಂಡ",
            NDLanguage.HI to "यम गंड"
        ),
        "str_gk" to mapOf(
            NDLanguage.EN to "Gulikai",
            NDLanguage.TA to "குளிகை",
            NDLanguage.KA to "ಗುಳಿಕಕಾಲ",
            NDLanguage.HI to "गुलिक काल"
        ),
        "str_nn" to mapOf(
            NDLanguage.EN to "Auspicious Times",
            NDLanguage.TA to "நல்ல நேரம்",
            NDLanguage.KA to "ಶುಭ ಸಮಯಗಳು",
            NDLanguage.HI to "शुभ समय"
        ),
        "str_pk" to mapOf(
            NDLanguage.EN to "Paksha",
            NDLanguage.TA to "பக்ஷம்",
            NDLanguage.KA to "ಪಕ್ಷ",
            NDLanguage.HI to "पक्ष"
        ),
        "str_valarpirai" to mapOf(
            NDLanguage.EN to "Shukla",
            NDLanguage.TA to "வளர்பிறை",
            NDLanguage.KA to "ಶುಕ್ಲ",
            NDLanguage.HI to "शुक्ल पक्ष"
        ),
        "str_theipirai" to mapOf(
            NDLanguage.EN to "Krishna",
            NDLanguage.TA to "தேய்பிறை",
            NDLanguage.KA to "ಕೃಷ್ಣ",
            NDLanguage.HI to "कृष्ण पक्ष"
        ),
        "str_tt" to mapOf(
            NDLanguage.EN to "Thithi",
            NDLanguage.TA to "திதி",
            NDLanguage.KA to "ತಿಥಿ",
            NDLanguage.HI to "तिथि"
        ),
        "str_vr" to mapOf(
            NDLanguage.EN to "Day",
            NDLanguage.TA to "கிழமை",
            NDLanguage.KA to "ವಾರ",
            NDLanguage.HI to "वार"
        ),
        "str_nk" to mapOf(
            NDLanguage.EN to "Nakshatra",
            NDLanguage.TA to "நக்ஷத்ரம்",
            NDLanguage.KA to "ನಕ್ಷತ್ರ",
            NDLanguage.HI to "नक्षत्र"
        ),
        "str_yg" to mapOf(
            NDLanguage.EN to "Yoga",
            NDLanguage.TA to "யோகம்",
            NDLanguage.KA to "ಯೋಗ",
            NDLanguage.HI to "योग"
        ),
        "str_kr" to mapOf(
            NDLanguage.EN to "Karana",
            NDLanguage.TA to "கரணம்",
            NDLanguage.KA to "ಕರಣ",
            NDLanguage.HI to "करण"
        ),
        "str_cr" to mapOf(
            NDLanguage.EN to "Chandrashtama",
            NDLanguage.TA to "சந்த்ராஷ்டமம்",
            NDLanguage.KA to "ಚಂದ್ರಾಷ್ಟಮ",
            NDLanguage.HI to "चंद्राष्टम"
        ),
        "str_month" to mapOf(
            NDLanguage.EN to "Maasam",
            NDLanguage.TA to "மாதம்",
            NDLanguage.KA to "ಮಾಸ",
            NDLanguage.HI to "मास"
        ),
        "str_muhurtha" to mapOf(
            NDLanguage.EN to "Subha Muhurtham",
            NDLanguage.TA to "சுப முகூர்த்தம்",
            NDLanguage.KA to "ಶುಭ ಮುಹೂರ್ತ",
            NDLanguage.HI to "शुभ मुहूर्त"
        ),
        "str_st" to mapOf(
            NDLanguage.EN to "Status",
            NDLanguage.TA to "நிலை",
            NDLanguage.KA to "ಸ್ಥಿತಿ",
            NDLanguage.HI to "स्थिति"
        ),
        "str_sc" to mapOf(
            NDLanguage.EN to "Score",
            NDLanguage.TA to "மதிப்பு",
            NDLanguage.KA to "ಅಂಕ",
            NDLanguage.HI to "स्कोर"
        ),
        "str_gd" to mapOf(
            NDLanguage.EN to "Good",
            NDLanguage.TA to "நன்று",
            NDLanguage.KA to "ಉತ್ತಮ",
            NDLanguage.HI to "अच्छा"
        ),
        "str_pr" to mapOf(
            NDLanguage.EN to "Poor",
            NDLanguage.TA to "மோசம்",
            NDLanguage.KA to "ಕಳಪೆ",
            NDLanguage.HI to "खराब"
        ),
        "str_av" to mapOf(
            NDLanguage.EN to "Average",
            NDLanguage.TA to "சுமார்",
            NDLanguage.KA to "ಸರಾಸರಿ",
            NDLanguage.HI to "औसत"
        ),
        "str_ex" to mapOf(
            NDLanguage.EN to "Excellent",
            NDLanguage.TA to "அருமை",
            NDLanguage.KA to "ಅತ್ಯುತ್ತಮ",
            NDLanguage.HI to "उत्कृष्ट"
        ),
        "rasi_palan" to mapOf(
            NDLanguage.EN to "Daily Rasi Palan",
            NDLanguage.TA to "இன்றைய ராசிபலன்",
            NDLanguage.KA to "ದಿನ ಭವಿಷ್ಯ",
            NDLanguage.HI to "दैनिक राशिफल"
        ),
        "music" to mapOf(
            NDLanguage.EN to "Devotional Song",
            NDLanguage.TA to "தெய்வீக இசை",
            NDLanguage.KA to "ಭಕ್ತಿಗೀತೆ",
            NDLanguage.HI to "भक्ति गीत"
        ),
        "panchangam" to mapOf(
            NDLanguage.EN to "Panchangam",
            NDLanguage.TA to "பஞ்சாங்கம்",
            NDLanguage.KA to "ಪಂಚಾಂಗ",
            NDLanguage.HI to "पंचांग"
        ),
        "naal_kaatti" to mapOf(
            NDLanguage.EN to "Today's Calendar",
            NDLanguage.TA to "நாள்காட்டி",
            NDLanguage.KA to "ಇಂದಿನ ಪಂಚಾಂಗ",
            NDLanguage.HI to "आज का कैलेंडर"
        ),
        "dp_notification" to mapOf(
            NDLanguage.EN to "Panchangam Alert",
            NDLanguage.TA to "பஞ்சாங்க மாற்றம்",
            NDLanguage.KA to "ಪಂಚಾಂಗ ಸೂಚನೆ",
            NDLanguage.HI to "पंचांग सूचना"
        ),
        "audio_suprabhatham" to mapOf(
            NDLanguage.EN to "Suprabhatham - Vishnu",
            NDLanguage.TA to "சுப்ரபாதம்",
            NDLanguage.KA to "ಸುಪ್ರಭಾತ - ವಿಷ್ಣು",
            NDLanguage.HI to "सुप्रभात - विष्णु"
        ),
        "audio_dhathatreya_sthuthi" to mapOf(
            NDLanguage.EN to "Dhathatreya Sthuthi",
            NDLanguage.TA to "தத்தாத்ரேய ஸ்துதி",
            NDLanguage.KA to "ದತ್ತಾತ್ರೇಯ ಸ್ತುತಿ",
            NDLanguage.HI to "दत्तात्रेय स्तुति"
        ),
        "audio_dakshinamurthy_sthothram" to mapOf(
            NDLanguage.EN to "Dakshinamurthy Sthothram",
            NDLanguage.TA to "தக்ஷிணாமூர்த்தி ஸ்தோத்ரம்",
            NDLanguage.KA to "ದಕ್ಷಿಣಾಮೂರ್ತಿ ಸ್ತೋತ್ರ",
            NDLanguage.HI to "दक्षिणामूर्ति स्तोत्र"
        ),
        "audio_kaakkum_kadavul" to mapOf(
            NDLanguage.EN to "Kaakkum Kadavul - Ganapathy",
            NDLanguage.TA to "காக்கும் கடவுள் கணேசனை",
            NDLanguage.KA to "ಕಾಕ್ಕುಂ ಕಡವುಳ್ - ಗಣಪತಿ",
            NDLanguage.HI to "रक्षक देव गणपति"
        ),
        "audio_kandha_sashti_kavacham" to mapOf(
            NDLanguage.EN to "SKandha Sashti Kavacham - Karthikaeya",
            NDLanguage.TA to "கந்த சஷ்டி கவசம்",
            NDLanguage.KA to "ಸ್ಕಂದ ಷಷ್ಠಿ ಕವಚ - ಕಾರ್ತಿಕೇಯ",
            NDLanguage.HI to "स्कंद षष्ठी कवच - कार्तिकेय"
        ),
        "audio_kolaru_pathigam" to mapOf(
            NDLanguage.EN to "Kolaru Padhigam - Shiva",
            NDLanguage.TA to "கோளறு பதிகம்",
            NDLanguage.KA to "ಕೋಳರು ಪದಿಗಂ - ಶಿವ",
            NDLanguage.HI to "कोळरु पदिगम - शिव"
        ),
        "audio_mahishasura_mardhini" to mapOf(
            NDLanguage.EN to "Mahishasura Mardhini",
            NDLanguage.TA to "மகிஷாசுரமர்த்தினி ஸ்தோத்ரம்",
            NDLanguage.KA to "ಮಹಿಷಾಸುರ ಮರ್ದಿನಿ",
            NDLanguage.HI to "महिषासुर मर्दिनी"
        ),
        "audio_namo_anjaneyam" to mapOf(
            NDLanguage.EN to "Namo Anjaneyam",
            NDLanguage.TA to "நமோ ஆஞ்சநேயம்",
            NDLanguage.KA to "ನಮೋ ಆಂಜನೇಯಂ",
            NDLanguage.HI to "नमो आंजनेयम्"
        ),
        "audio_panchamirdha_vannam" to mapOf(
            NDLanguage.EN to "Panchamirdha Vannam - karthikeya",
            NDLanguage.TA to "பஞ்சாமிர்த வண்ணம் - முருகன்",
            NDLanguage.KA to "ಪಂಚಾಮೃತ ವಣ್ಣಂ - ಕಾರ್ತಿಕೇಯ",
            NDLanguage.HI to "पंचामृत वर्णन - कार्तिकेय"
        ),
        "audio_rudhram" to mapOf(
            NDLanguage.EN to "Sri Rudhram",
            NDLanguage.TA to "ஸ்ரீ ருத்ரம்",
            NDLanguage.KA to "ಶ್ರೀ ರುದ್ರಂ",
            NDLanguage.HI to "श्री रुद्रम्"
        ),
        "audio_sivamayamaagha_therigiradhe" to mapOf(
            NDLanguage.EN to "Sivamayamaaga - Shiva",
            NDLanguage.TA to "சிவமயமாக தெரிகிறதே",
            NDLanguage.KA to "ಶಿವಮಯವಾಗಿ ಕಾಣುತ್ತಿದೆ - ಶಿವ",
            NDLanguage.HI to "शिवमय दिख रहा है - शिव"
        ),
        "audio_vishnu_sahasranamam" to mapOf(
            NDLanguage.EN to "Vishnu Sahasranama",
            NDLanguage.TA to "விஷ்ணு சகஸ்ரநாமம்",
            NDLanguage.KA to "ವಿಷ್ಣು ಸಹಸ್ರನಾಮ",
            NDLanguage.HI to "विष्णु सहस्रनाम"
        ),
        "audio_rama_rama_jeya_rajaram" to mapOf(
            NDLanguage.EN to "Rama Rama Jeya Rajaram",
            NDLanguage.TA to "ராம ராம ஜெய ராஜாராம்",
            NDLanguage.KA to "ರಾಮ ರಾಮ ಜಯ ರಾಜಾರಾಮ",
            NDLanguage.HI to "राम राम जय राजाराम"
        ),
        "audio_om_chanting" to mapOf(
            NDLanguage.EN to "Pranava Manthra Om",
            NDLanguage.TA to "பிரணவ மந்திரம் ஓம்",
            NDLanguage.KA to "ಪ್ರಣವ ಮಂತ್ರ ಓಂ",
            NDLanguage.HI to "प्रणव मंत्र ॐ"
        ),
        "str_pd" to mapOf(
            NDLanguage.EN to "Paadha",
            NDLanguage.TA to "பாதம்",
            NDLanguage.KA to "ಪಾದ",
            NDLanguage.HI to "पाद"
        ),
        "str_today" to mapOf(
            NDLanguage.EN to "Today",
            NDLanguage.TA to "இன்று",
            NDLanguage.KA to "ಇಂದು",
            NDLanguage.HI to "आज"
        ),
        "str_tomorrow" to mapOf(
            NDLanguage.EN to "Tomorrow",
            NDLanguage.TA to "நாளை",
            NDLanguage.KA to "ನಾಳೆ",
            NDLanguage.HI to "कल"
        ),
        "str_timeend" to mapOf(
            NDLanguage.EN to "%1\$s, until %2\$s %3\$s %4\$s %5\$s",
            NDLanguage.TA to "%1\$s, %2\$s %3\$s %4\$s மணி வரை",
            NDLanguage.KA to "%1\$s, %2\$s %3\$s %4\$s ರವರೆಗೆ",
            NDLanguage.HI to "%1\$s, %2\$s %3\$s %4\$s %5\$s तक"
        ),
        "str_fut_timeend" to mapOf(
            NDLanguage.EN to "%1\$s, until %2\$s %3\$s %4\$s, then %5\$s",
            NDLanguage.TA to "%1\$s, %2\$s %3\$s மணி வரை, பின்னர் %5\$s",
            NDLanguage.KA to "%1\$s, %2\$s %3\$s %4\$s ರವರೆಗೆ, ನಂತರ %5\$s",
            NDLanguage.HI to "%1\$s, %2\$s %3\$s %4\$s तक, फिर %5\$s"
        ),
        "str_currastrostatusold" to mapOf(
            NDLanguage.EN to "From %1s %2s - %3s %4s, Status: %5s",
            NDLanguage.TA to "%1s %2s - %3s %4s வரை, நிலை: %5s",
            NDLanguage.KA to "%1s %2s ರಿಂದ %3s %4s ವರೆಗೆ, ಸ್ಥಿತಿ: %5s",
            NDLanguage.HI to "%1s %2s से %3s %4s तक, स्थिति: %5s"
        ),
        "str_currastrostatus" to mapOf(
            NDLanguage.EN to "From %1s %2s - %3s %4s, Status: %5s",
            NDLanguage.TA to "%1s %2s - %3s %4s வரை, நிலை: %5s",
            NDLanguage.KA to "%1s %2s ರಿಂದ %3s %4s ವರೆಗೆ, ಸ್ಥಿತಿ: %5s",
            NDLanguage.HI to "%1s %2s से %3s %4s तक, स्थिति: %5s"
        ),
        "str_currastrostatus_samedayold" to mapOf(
            NDLanguage.EN to "From %1s %2s - %3s, Status: %5s",
            NDLanguage.TA to "%1s %2s - %3s வரை, நிலை: %5s",
            NDLanguage.KA to "%1s %2s ರಿಂದ %3s ವರೆಗೆ, ಸ್ಥಿತಿ: %5s",
            NDLanguage.HI to "%1s %2s से %3s तक, स्थिति: %5s"
        ),
        "str_currastrostatus_sameday" to mapOf(
            NDLanguage.EN to "Today until %3s, Status: %5s",
            NDLanguage.TA to "இன்று %3s வரை, நிலை: %5s",
            NDLanguage.KA to "ಇಂದು %3s ರವರೆಗೆ, ಸ್ಥಿತಿ: %5s",
            NDLanguage.HI to "आज %3s तक, स्थिति: %5s"
        ),
        "about_title" to mapOf(
            NDLanguage.EN to "Why the App?",
            NDLanguage.TA to "எதற்காக இது?",
            NDLanguage.KA to "ಈ ಆಪ್ ಏಕೆ?",
            NDLanguage.HI to "यह ऐप क्यों?"
        ),
        "about_us" to mapOf(
            NDLanguage.EN to "Namaskar,\n\nAmidst incessant distractions from external media, the yearning for true happiness often slips the conscious thought. What ensues is a vicious cycle of endless consumption of content we don't need with time we don't have. It tires both physically and mentally. \n\nUnlike brute faith, Contemplation and Self Reflection are core Sanathanic traits. Without Gurus, we need some help illuminating a path towards a more mindful journey. Nithya Dharma aims to be your technological agent!\n\nConsuming is plodding whereas Dharma is momentous. It paves way for happiness. \n\n🙏",
            NDLanguage.TA to "வணக்கம்,\n\nவெளியிலிருந்து வரும் ஓயாத கவனச் சிதறல்களுக்கு மத்தியில், உண்மையான மகிழ்ச்சிக்கான ஏக்கம் பெரும்பாலும் நினைவில் இருப்பதில்லை. அதன் விளைவாக தேவையில்லாதவற்றைத் தொடர்ந்து உள்வாங்கிக் கொண்டே இருக்கும் குப்பைத் தொட்டியாக நாம் மாறி வருகிறோம். இது உடலையும் மனதையும் களைப்படையச் செய்கிறது.\n\nமூடநம்பிக்கையைப் போலல்லாமல், சிந்தனையும் சுய பரிசீலனையும் சனாதன தர்மத்தின் மையப் பண்புகளாகும். \n\nகுருக்கள் இல்லாத நிலையில், நாம் ஒரு சிந்தனைமிக்க பயணத்திற்கு வழிகாட்டும் ஒளியைத் தேட வேண்டியுள்ளது. நித்ய-தர்மா உங்கள் தொழில்நுட்ப உதவியாளராக இருக்க முயல்கிறது!\n\nவெற்று மத நம்பிக்கை என்பது மெதுவான பயணம்; ஆனால் தர்மம் என்பது வேகமானது, உயிர்த்துடிப்பானது. அது மகிழ்ச்சிக்கு வழிவகுக்கிறது. \n\n🙏",
            NDLanguage.KA to "ನಮಸ್ಕಾರ,\n\nಬಾಹ್ಯ ಮಾಧ್ಯಮಗಳಿಂದ ನಿರಂತರವಾಗಿ ಬರುವ ಗಮನಚಲನೆಗಳ ನಡುವೆ, ನಿಜವಾದ ಸಂತೋಷದ ಬಯಕೆ ನಮ್ಮ ಜಾಗೃತ ಮನಸ್ಸಿನಿಂದ ಬಹುಮಟ್ಟಿಗೆ ಮರೆಯಾಗುತ್ತದೆ. ಅದರ ಪರಿಣಾಮವಾಗಿ, ನಮಗೆ ಅಗತ್ಯವಿಲ್ಲದ ವಿಷಯಗಳನ್ನು ನಮಗೆ ಸಮಯವಿಲ್ಲದಿದ್ದರೂ ನಿರಂತರವಾಗಿ ಸೇವಿಸುವ ಒಂದು ಚಕ್ರದಲ್ಲಿ ಸಿಲುಕುತ್ತೇವೆ. ಇದು ದೇಹ ಮತ್ತು ಮನಸ್ಸನ್ನು ದಣಿಗೊಳಿಸುತ್ತದೆ. \n\nಕುರುಡು ನಂಬಿಕೆಯ ಬದಲು, ಚಿಂತನೆ ಮತ್ತು ಆತ್ಮಪರಿಶೀಲನೆ ಸನಾತನ ಧರ್ಮದ ಮೂಲ ಗುಣಗಳಾಗಿವೆ. ಗುರುಗಳಿಲ್ಲದ ಸಂದರ್ಭದಲ್ಲಿ, ಹೆಚ್ಚು ಜಾಗೃತಿಯುತ ಜೀವನದತ್ತ ಸಾಗಲು ನಮಗೆ ದಾರಿ ತೋರಿಸುವ ಬೆಳಕು ಬೇಕಾಗುತ್ತದೆ. ನಿತ್ಯಧರ್ಮ ನಿಮ್ಮ ತಾಂತ್ರಿಕ ಸಹಾಯಕನಾಗಲು ಪ್ರಯತ್ನಿಸುತ್ತದೆ!\n\nಕೇವಲ ಉಪಭೋಗವು ನಿಧಾನಗತಿಯ ಬದುಕು; ಧರ್ಮವು ಚೈತನ್ಯಮಯವಾದುದು. ಅದು ಸಂತೋಷದತ್ತ ದಾರಿ ಮಾಡುತ್ತದೆ. \n\n🙏",
            NDLanguage.HI to "नमस्कार,\n\nबाहरी मीडिया से आने वाली लगातार विकर्षणों के बीच, सच्ची खुशी की चाहत अक्सर हमारी चेतना से ओझल हो जाती है। इसका परिणाम यह होता है कि हम अपने पास मौजूद समय में अनावश्यक सामग्री का उपभोग करते रहते हैं। यह शारीरिक और मानसिक दोनों रूप से थकाता है।\n\nअंध विश्वास के विपरीत, चिंतन और आत्म-मंथन सनातन धर्म के मूल गुण हैं। गुरुओं के अभाव में, हमें एक अधिक सचेतन यात्रा की ओर मार्गदर्शन करने वाले प्रकाश की आवश्यकता है। नित्यधर्म आपका तकनीकी सहायक बनने का प्रयास करता है!\n\nउपभोग एक धीमी यात्रा है, जबकि धर्म सशक्त और जीवंत है। यह खुशी का मार्ग प्रशस्त करता है। 🙏"
        ),
        "about_other" to mapOf(
            NDLanguage.EN to "Our website: www.templepages.com\nVersion: %1s",
            NDLanguage.TA to "வலை தளம்: www.templepages.com\nபதிப்பு: %1s",
            NDLanguage.KA to "ನಮ್ಮ ವೆಬ್‌ಸೈಟ್: www.templepages.com\nಆವೃತ್ತಿ: %1s",
            NDLanguage.HI to "हमारी वेबसाइट: www.templepages.com\nसंस्करण: %1s"
        ),
        "hc_bottom" to mapOf(
            NDLanguage.EN to "Dates are in accordance with Tamizh calendar. Alarm time ",
            NDLanguage.TA to "தமிழ்நாட்டு முறையில் தேதிகள் கணிக்கப்பட்டுள்ளன. நேரம் ",
            NDLanguage.KA to "ದಿನಾಂಕಗಳು ತಮಿಳು ಪಂಚಾಂಗದ ಅನುಸಾರವಾಗಿವೆ. ಅಲಾರಂ ಸಮಯ ",
            NDLanguage.HI to "तिथियाँ तमिल कैलेंडर के अनुसार हैं। अलार्म का समय "
        ),
        "cmn_date" to mapOf(
            NDLanguage.EN to "Date",
            NDLanguage.TA to "தேதி",
            NDLanguage.KA to "ದಿನಾಂಕ",
            NDLanguage.HI to "तिथि"
        ),
        "cmn_occasion" to mapOf(
            NDLanguage.EN to "Occasion",
            NDLanguage.TA to "நிகழ்வு",
            NDLanguage.KA to "ಅವಸರ / ಕಾರ್ಯಕ್ರಮ",
            NDLanguage.HI to "अवसर"
        ),
        "cmn_todaysevent" to mapOf(
            NDLanguage.EN to "Today!",
            NDLanguage.TA to "இன்று!",
            NDLanguage.KA to "ಇಂದು!",
            NDLanguage.HI to "आज!"
        ),
        "cmn_tomorrowevent" to mapOf(
            NDLanguage.EN to "Tomorrow!",
            NDLanguage.TA to "நாளை!",
            NDLanguage.KA to "ನಾಳೆ!",
            NDLanguage.HI to "कल!"
        ),
        "cmn_futureevent" to mapOf(
            NDLanguage.EN to "Down the line",
            NDLanguage.TA to "வரும் நாட்களில் ...",
            NDLanguage.KA to "ಮುಂದಿನ ದಿನಗಳಲ್ಲಿ",
            NDLanguage.HI to "आने वाले दिनों में..."
        ),
        "hs_birthdtls" to mapOf(
            NDLanguage.EN to "Birth details",
            NDLanguage.TA to "பிறப்புத் தகவல்கள்",
            NDLanguage.KA to "ಜನ್ಮ ವಿವರಗಳು",
            NDLanguage.HI to "जन्म विवरण"
        ),
        "hs_birthchart" to mapOf(
            NDLanguage.EN to "Birth Chart",
            NDLanguage.TA to "ஜாதகம்",
            NDLanguage.KA to "ಜನ್ಮ ಕುಂಡಲಿ",
            NDLanguage.HI to "जन्म कुंडली"
        ),
        "hs_dba" to mapOf(
            NDLanguage.EN to "Dasha Bukthi",
            NDLanguage.TA to "தசா புக்தி",
            NDLanguage.KA to "ದಶಾ ಭುಕ್ತಿ",
            NDLanguage.HI to "दशा-भुक्ति"
        ),
        "ct_inter" to mapOf(
            NDLanguage.EN to "Step",
            NDLanguage.TA to "படி",
            NDLanguage.KA to "ಹಂತ",
            NDLanguage.HI to "कदम"
        ),
        "ct_target" to mapOf(
            NDLanguage.EN to "Target",
            NDLanguage.TA to "இலக்கு",
            NDLanguage.KA to "ಗುರಿ",
            NDLanguage.HI to "लक्ष्य"
        ),
        "ct_reset" to mapOf(
            NDLanguage.EN to "Reset",
            NDLanguage.TA to "மீள்",
            NDLanguage.KA to "ಮರುಹೊಂದಿಸಿ",
            NDLanguage.HI to "रीसेट करें"
        ),
        "ct_motto" to mapOf(
            NDLanguage.EN to "Keep count of your favorite manthram",
            NDLanguage.TA to "உங்களுக்கு பிடித்த மந்திரத்தை எண்ணிக்கொள்ளுங்கள்",
            NDLanguage.KA to "ನಿಮ್ಮ ಮೆಚ್ಚಿನ ಮಂತ್ರದ ಎಣಿಕೆಯನ್ನು ಇಟ್ಟುಕೊಳ್ಳಿ",
            NDLanguage.HI to "अपने पसंदीदा मंत्र की गिनती रखें"
        ),
        "ll_bottom" to mapOf(
            NDLanguage.EN to "Lighting the lamp illuminates the home!",
            NDLanguage.TA to "கோயில் தீபம் வீட்டில் ஒளி!",
            NDLanguage.KA to "ದೀಪ ಬೆಳಗಿಸಿದರೆ ಮನೆ ಬೆಳಗುತ್ತದೆ!",
            NDLanguage.HI to "दीपक जलाने से घर रोशन होता है!"
        ),
        "ll_title" to mapOf(
            NDLanguage.EN to "Light a Lamp",
            NDLanguage.TA to "ஆலய தீபம் ஏற்றுவோம்",
            NDLanguage.KA to "ದೀಪ ಬೆಳಗಿಸಿ",
            NDLanguage.HI to "दीपक जलाएं"
        ),
        "ll_msg1" to mapOf(
            NDLanguage.EN to "These ancient temples once thrived with life. Today they stand neglected and in ruins with no funds even for oil. We begin with small acts of support to revive them and draw people back.",
            NDLanguage.TA to "இது போன்ற பழமையான கோயில்கள் ஒருகாலத்தில் உயிர்ப்புடன் விளங்கின. இன்று அவை புறக்கணிக்கப்பட்டு சிதைந்துள்ளன. எண்ணெய் வாங்குவதற்கு கூட நிதி இல்லை. அவற்றில் மீண்டும் உயிர் ஊட்ட நாங்கள் சிறிய ஆதரவுச் செயல்களுடன் தொடங்குகிறோம். கவனிப்பாரற்று விடப்பட்ட கோயில்களுக்கு உங்கள் பங்களிப்பு தலைமுறைகளுக்கான புண்ணியத்தை கொண்டு சேர்க்கும்.",
            NDLanguage.KA to "ಈ ಪ್ರಾಚೀನ ದೇವಾಲಯಗಳು ಒಮ್ಮೆ ಜನಜೀವನದಿಂದ ಕಂಗೊಳಿಸುತ್ತಿದ್ದವು. ಇಂದು ಅವು ನಿರ್ಲಕ್ಷ್ಯದಿಂದ ಜೀರ್ಣಾವಸ್ಥೆಗೆ ತಲುಪಿವೆ; ಎಣ್ಣೆ ಖರೀದಿಸಲು ಸಹ ಹಣವಿಲ್ಲ. ಅವುಗಳನ್ನು ಪುನರುಜ್ಜೀವನಗೊಳಿಸಿ ಜನರನ್ನು ಮತ್ತೆ ಸೆಳೆಯಲು ನಾವು ಸಣ್ಣ ಸಣ್ಣ ಸಹಾಯ ಕಾರ್ಯಗಳಿಂದ ಪ್ರಾರಂಭಿಸುತ್ತಿದ್ದೇವೆ.",
            NDLanguage.HI to "ये प्राचीन मंदिर कभी जीवन से लबरेज़ थे। आज वे उपेक्षित और खंडहर हो गए हैं, तेल के लिए भी धन नहीं है। हम उन्हें पुनर्जीवित करने और लोगों को वापस लाने के लिए छोटे सहायता कार्यों से शुरुआत कर रहे हैं।"
        ),
        "ll_contrib" to mapOf(
            NDLanguage.EN to "If you would like to contribute, scan the qr code provided above from any UPI app ( NithyaDharma Charitable Trust ) & whatsapp %1s with details so we know it is you.",
            NDLanguage.TA to "நீங்கள் தீபம் ஏற்ற விரும்பினால், மேற்காணும் க்யூ ஆர் கோடை ஸ்கேன் செய்துவிட்டு (நித்ய தர்மா அறக்கட்டளை) எனும் முகவரிக்கு பணம் செலுத்திவிட்டு %1s எனும் எண்ணிற்கு வாட்ஸ்அப் அனுப்பினால் உங்கள் பங்களிப்பு பயன்படுத்தப்படும் போது தகவல் அனுப்பப்படும்.",
            NDLanguage.KA to "ನೀವು ಕೊಡುಗೆ ನೀಡಲು ಬಯಸಿದರೆ, ಮೇಲಿರುವ QR ಕೋಡ್ ಅನ್ನು ಯಾವುದೇ UPI ಆಪ್‌ನಿಂದ ಸ್ಕ್ಯಾನ್ ಮಾಡಿ (ನಿತ್ಯಧರ್ಮ ಚಾರಿಟೇಬಲ್ ಟ್ರಸ್ಟ್) ಮತ್ತು ವಿವರಗಳೊಂದಿಗೆ %1s ಗೆ ವಾಟ್ಸಾಪ್ ಮಾಡಿ, ಅದು ನಿಮ್ಮ ಕೊಡುಗೆ ಎಂದು ನಮಗೆ ತಿಳಿಯುತ್ತದೆ.",
            NDLanguage.HI to "यदि आप योगदान देना चाहते हैं, तो ऊपर दिए गए QR कोड को किसी भी UPI ऐप से स्कैन करें (नित्यधर्म चैरिटेबल ट्रस्ट) और विवरण के साथ %1s पर व्हाट्सएप करें ताकि हमें पता चले कि यह आप हैं।"
        ),
        "ll_trustname" to mapOf(
            NDLanguage.EN to "NithyaDharma Charitable Trust",
            NDLanguage.TA to "நித்ய தர்மா அறக்கட்டளை",
            NDLanguage.KA to "ನಿತ್ಯಧರ್ಮ ಚಾರಿಟೇಬಲ್ ಟ್ರಸ್ಟ್",
            NDLanguage.HI to "नित्यधर्म चैरिटेबल ट्रस्ट"
        ),
        "ll_bankremit" to mapOf(
            NDLanguage.EN to "Alternately you may do old style remittance to our bank account %1s",
            NDLanguage.TA to "நீங்கள் வங்கிக் கணக்கில் பணம் செலுத்த விரும்பினால் \n%1s \nஎனும் எண்ணிற்கு பணம் அனுப்பலாம்.",
            NDLanguage.KA to "ಪರ್ಯಾಯವಾಗಿ ನೀವು ಹಳೆಯ ರೀತಿಯಲ್ಲಿ ನಮ್ಮ ಬ್ಯಾಂಕ್ ಖಾತೆಗೆ ಹಣ ವರ್ಗಾಯಿಸಬಹುದು\n%1s",
            NDLanguage.HI to "वैकल्पिक रूप से आप हमारे बैंक खाते %1s में पारंपरिक तरीके से राशि भेज सकते हैं"
        ),
        "ll_usage" to mapOf(
            NDLanguage.EN to "Your contribution provides for one of \n  a) Puja essentials like Oil, Vibhuthi, Kungumam, Rice etc\n  b) Services like Pradosham, Kruthikai, Shankatahara Chathurthi",
            NDLanguage.TA to "உங்கள் பங்களிப்பு பின்வருவனவற்றில் ஒன்றுக்கு உதவுகிறது:\n அ) பூஜைக்கு தேவையான பொருட்கள் — எண்ணெய், விபூதி, அரிசி போன்றவை\n ஆ) சிறப்பு சேவைகள் — பிரதோஷம், கிருத்திகை, சங்கடஹர சதுர்த்தி ...",
            NDLanguage.KA to "ನಿಮ್ಮ ಕೊಡುಗೆ ಕೆಳಗಿನ ಯಾವುದಾದರೂ ಒಂದಕ್ಕೆ ಸಹಾಯ ಮಾಡುತ್ತದೆ:\n  ಅ) ಪೂಜಾ ಸಾಮಗ್ರಿಗಳು — ಎಣ್ಣೆ, ವಿಭೂತಿ, ಕುಂಕುಮ, ಅಕ್ಕಿ ಇತ್ಯಾದಿ\n  ಆ) ಪ್ರದೋಷ, ಕೃತಿಕಾ, ಸಂಕಟಹರ ಚತುರ್ಥಿ ಮುಂತಾದ ಸೇವೆಗಳು",
            NDLanguage.HI to "आपका योगदान निम्न में से एक के लिए सहायता करता है:\n  क) पूजा सामग्री — तेल, विभूति, कुमकुम, चावल आदि\n  ख) विशेष सेवाएँ — प्रदोष, कृत्तिका, संकटहर चतुर्थी"
        ),
        "mp_bottom" to mapOf(
            NDLanguage.EN to "Keep playing your favorite manthram",
            NDLanguage.TA to "விருப்பமானதைக் கேளுங்கள்",
            NDLanguage.KA to "ನಿಮ್ಮ ಮೆಚ್ಚಿನ ಮಂತ್ರವನ್ನು ಕೇಳುತ್ತಿರಿ",
            NDLanguage.HI to "अपना पसंदीदा मंत्र सुनते रहें"
        ),
        "mp_stop" to mapOf(
            NDLanguage.EN to "Stop",
            NDLanguage.TA to "நிறுத்த",
            NDLanguage.KA to "ನಿಲ್ಲಿಸಿ",
            NDLanguage.HI to "रोकें"
        ),
        "mp_ganesha" to mapOf(
            NDLanguage.EN to "Ganesha Manthra",
            NDLanguage.TA to "விநாயகர் துதி",
            NDLanguage.KA to "ಗಣೇಶ ಮಂತ್ರ",
            NDLanguage.HI to "गणेश मंत्र"
        ),
        "mp_murugan" to mapOf(
            NDLanguage.EN to "Shanmuga Manthra",
            NDLanguage.TA to "சண்முகர் துதி",
            NDLanguage.KA to "ಷಣ್ಮುಖ ಮಂತ್ರ",
            NDLanguage.HI to "षण्मुख मंत्र"
        ),
        "mp_vishnu" to mapOf(
            NDLanguage.EN to "Vishnu Manthra",
            NDLanguage.TA to "விஷ்ணு துதி",
            NDLanguage.KA to "ವಿಷ್ಣು ಮಂತ್ರ",
            NDLanguage.HI to "विष्णु मंत्र"
        ),
        "mp_ddtrya" to mapOf(
            NDLanguage.EN to "Dhatrathreya Manthra",
            NDLanguage.TA to "தத்தாத்ரேயர் துதி",
            NDLanguage.KA to "ದತ್ತಾತ್ರೇಯ ಮಂತ್ರ",
            NDLanguage.HI to "दत्तात्रेय मंत्र"
        ),
        "mp_shiva" to mapOf(
            NDLanguage.EN to "Shiva Manthra",
            NDLanguage.TA to "சிவன் துதி",
            NDLanguage.KA to "ಶಿವ ಮಂತ್ರ",
            NDLanguage.HI to "शिव मंत्र"
        ),
        "mp_devi" to mapOf(
            NDLanguage.EN to "Devi Manthra",
            NDLanguage.TA to "அம்பிகை துதி",
            NDLanguage.KA to "ದೇವಿ ಮಂತ್ರ",
            NDLanguage.HI to "देवी मंत्र"
        ),
        "mp_tara" to mapOf(
            NDLanguage.EN to "Tara Manthra",
            NDLanguage.TA to "தாரா துதி",
            NDLanguage.KA to "ತಾರಾ ಮಂತ್ರ",
            NDLanguage.HI to "तारा मंत्र"
        ),
        "tm_title" to mapOf(
            NDLanguage.EN to "Temple Locator",
            NDLanguage.TA to "ஆலய வழிகாட்டி",
            NDLanguage.KA to "ದೇವಾಲಯ ಹುಡುಕಾಟ",
            NDLanguage.HI to "मंदिर खोजक"
        ),
        "tm_bottom" to mapOf(
            NDLanguage.EN to "Click anywhere on map and press [icon] on top right to see temples there",
            NDLanguage.TA to "படத்தில் எங்கு வேண்டுமானாலும் கிளிக் செய்துவிட்டு [icon] ஐ அழுத்தவும்",
            NDLanguage.KA to "ನಕ್ಷೆಯಲ್ಲಿ ಎಲ್ಲಿಯಾದರೂ ಕ್ಲಿಕ್ ಮಾಡಿ ಮತ್ತು ಅಲ್ಲಿನ ದೇವಾಲಯಗಳನ್ನು ನೋಡಲು ಮೇಲಿನ ಬಲಭಾಗದಲ್ಲಿರುವ [icon] ಒತ್ತಿರಿ",
            NDLanguage.HI to "मानचित्र पर कहीं भी क्लिक करें और वहाँ के मंदिर देखने के लिए ऊपर दाईं ओर [icon] दबाएँ"
        ),
        "pp_title" to mapOf(
            NDLanguage.EN to "Puja Store",
            NDLanguage.TA to "பூஜைப் பொருட்கள்",
            NDLanguage.KA to "ಪೂಜಾ ಅಂಗಡಿ",
            NDLanguage.HI to "पूजा स्टोर"
        ),
        "pp_discount" to mapOf(
            NDLanguage.EN to "10%% discount for app orders",
            NDLanguage.TA to "இங்கு வாங்கினால் 10%% தள்ளுபடி",
            NDLanguage.KA to "ಆಪ್ ಮೂಲಕ ಆರ್ಡರ್ ಮಾಡಿದರೆ 10%% ರಿಯಾಯಿತಿ",
            NDLanguage.HI to "ऐप ऑर्डर पर 10%% की छूट"
        ),
        "pp_custdtls" to mapOf(
            NDLanguage.EN to "Personal Details",
            NDLanguage.TA to "சுய விவரங்கள்",
            NDLanguage.KA to "ವೈಯಕ್ತಿಕ ವಿವರಗಳು",
            NDLanguage.HI to "व्यक्तिगत विवरण"
        ),
        "pf_lang" to mapOf(
            NDLanguage.EN to "Language",
            NDLanguage.TA to "மொழி",
            NDLanguage.KA to "ಭಾಷೆ",
            NDLanguage.HI to "भाषा"
        ),
        "pf_rasi" to mapOf(
            NDLanguage.EN to "Rasi",
            NDLanguage.TA to "ராசி",
            NDLanguage.KA to "ರಾಶಿ",
            NDLanguage.HI to "राशि"
        ),
        "pf_finalcnt" to mapOf(
            NDLanguage.EN to "Final Count: %1s",
            NDLanguage.TA to "இறுதி எண்ணிக்கை: %1s",
            NDLanguage.KA to "ಅಂತಿಮ ಎಣಿಕೆ: %1s",
            NDLanguage.HI to "अंतिम गणना: %1s"
        ),
        "rh_title" to mapOf(
            NDLanguage.EN to "Submit Request for Oil+",
            NDLanguage.TA to "உதவி கோருங்கள்",
            NDLanguage.KA to "ಎಣ್ಣೆಗಾಗಿ ವಿನಂತಿ ಸಲ್ಲಿಸಿ+",
            NDLanguage.HI to "तेल+ के लिए अनुरोध सबमिट करें"
        ),
        "rh_bottom" to mapOf(
            NDLanguage.EN to "All fields must be entered and also Name in Preferences",
            NDLanguage.TA to "அனைத்து தகவல்களையும் நிரப்புக மற்றும் விருப்பங்களில் பெயரை நிரப்பவும்",
            NDLanguage.KA to "ಎಲ್ಲಾ ಮಾಹಿತಿಯನ್ನು ನಮೂದಿಸಬೇಕು ಮತ್ತು ಆದ್ಯತೆಗಳಲ್ಲಿ ಹೆಸರನ್ನೂ ನಮೂದಿಸಬೇಕು",
            NDLanguage.HI to "सभी फ़ील्ड भरें और प्राथमिकताओं में नाम भी दर्ज करें"
        ),
        "fb_title" to mapOf(
            NDLanguage.EN to "Customer",
            NDLanguage.TA to "வாடிக்கையாளர்",
            NDLanguage.KA to "ಗ್ರಾಹಕ",
            NDLanguage.HI to "ग्राहक"
        ),
        "fb_bottom" to mapOf(
            NDLanguage.EN to "Name must be set in preferences",
            NDLanguage.TA to "விருப்பங்களில் வாடிக்கையாளர் பெயரை நிரப்பவும்",
            NDLanguage.KA to "ಆದ್ಯತೆಗಳಲ್ಲಿ ಹೆಸರು ನಮೂದಿಸಬೇಕು",
            NDLanguage.HI to "प्राथमिकताओं में नाम सेट करना अनिवार्य है"
        ),
        "hr_bottom" to mapOf(
            NDLanguage.EN to "Set Name,Date and Time of birth, lat and long in preferences",
            NDLanguage.TA to "விருப்பங்களில் பெயர், பிறந்த தேதி/நேரம், அட்சரேகை, தீர்க்க ரேகை நிரப்பவும்",
            NDLanguage.KA to "ಆದ್ಯತೆಗಳಲ್ಲಿ ಹೆಸರು, ಜನ್ಮ ದಿನಾಂಕ ಮತ್ತು ಸಮಯ, ಅಕ್ಷಾಂಶ ಮತ್ತು ರೇಖಾಂಶವನ್ನು ನಮೂದಿಸಿ",
            NDLanguage.HI to "प्राथमिकताओं में नाम, जन्म तिथि और समय, अक्षांश और देशांतर सेट करें"
        ),
        "cmn_products" to mapOf(
            NDLanguage.EN to "Products",
            NDLanguage.TA to "பொருட்கள்",
            NDLanguage.KA to "ಉತ್ಪನ್ನಗಳು",
            NDLanguage.HI to "उत्पाद"
        ),
        "cmn_submit" to mapOf(
            NDLanguage.EN to "Submit",
            NDLanguage.TA to "அனுப்ப",
            NDLanguage.KA to "ಸಲ್ಲಿಸಿ",
            NDLanguage.HI to "सबमिट करें"
        ),
        "str_crdtl" to mapOf(
            NDLanguage.EN to "Its best to be cautious about big decisions today",
            NDLanguage.TA to "இன்று சந்திராஷ்டமத்தை அனுசரித்து முக்கிய முடிவுகள் எடுக்கவும்",
            NDLanguage.KA to "ಇಂದು ದೊಡ್ಡ ನಿರ್ಧಾರಗಳ ವಿಷಯದಲ್ಲಿ ಎಚ್ಚರಿಕೆಯಿಂದಿರುವುದು ಉತ್ತಮ",
            NDLanguage.HI to "आज बड़े निर्णयों में सावधानी बरतना उचित है"
        ),
        "pa_shukla" to mapOf(
            NDLanguage.EN to "Shukla",
            NDLanguage.TA to "சுக்ல",
            NDLanguage.KA to "ಶುಕ್ಲ",
            NDLanguage.HI to "शुक्ल"
        ),
        "pa_krishna" to mapOf(
            NDLanguage.EN to "Krishna",
            NDLanguage.TA to "கிருஷ்ண",
            NDLanguage.KA to "ಕೃಷ್ಣ",
            NDLanguage.HI to "कृष्ण"
        ),
        "rp_health_vitality" to mapOf(
            NDLanguage.EN to "Health & Vitality",
            NDLanguage.TA to "ஆரோக்கியம் மற்றும் புத்துணர்ச்சி",
            NDLanguage.KA to "ಆರೋಗ್ಯ ಮತ್ತು ಚೈತನ್ಯ",
            NDLanguage.HI to "स्वास्थ्य और स्फूर्ति"
        ),
        "rp_pom_mood" to mapOf(
            NDLanguage.EN to "Peace of Mind & Mood",
            NDLanguage.TA to "மன அமைதி மற்றும் மனநிலை",
            NDLanguage.KA to "ಮನಃಶಾಂತಿ ಮತ್ತು ಮನೋಭಾವ",
            NDLanguage.HI to "मानसिक शांति और मनोदशा"
        ),
        "rp_property_courage" to mapOf(
            NDLanguage.EN to "Property & Courage",
            NDLanguage.TA to "சொத்து மற்றும் தைரியம்",
            NDLanguage.KA to "ಆಸ್ತಿ ಮತ್ತು ಧೈರ್ಯ",
            NDLanguage.HI to "संपत्ति और साहस"
        ),
        "rp_edu_commerce" to mapOf(
            NDLanguage.EN to "Education & Commerce",
            NDLanguage.TA to "கல்வி மற்றும் வியாபாரம்",
            NDLanguage.KA to "ಶಿಕ್ಷಣ ಮತ್ತು ವಾಣಿಜ್ಯ",
            NDLanguage.HI to "शिक्षा और वाणिज्य"
        ),
        "rp_finance_luck" to mapOf(
            NDLanguage.EN to "Finance & Luck",
            NDLanguage.TA to "தனயோகம் மற்றும் அதிர்ஷ்டம்",
            NDLanguage.KA to "ಹಣಕಾಸು ಮತ್ತು ಅದೃಷ್ಟ",
            NDLanguage.HI to "वित्त और भाग्य"
        ),
        "rp_reltn_comfort" to mapOf(
            NDLanguage.EN to "Relationships & Comforts",
            NDLanguage.TA to "உறவுகள் மற்றும் சுகபோகம்",
            NDLanguage.KA to "ಸಂಬಂಧಗಳು ಮತ್ತು ಸುಖ-ಸಂತೋಷ",
            NDLanguage.HI to "रिश्ते और सुख-सुविधाएँ"
        ),
        "rp_empl_career" to mapOf(
            NDLanguage.EN to "Employment & Career",
            NDLanguage.TA to "உத்தியோகம் மற்றும் தொழில்",
            NDLanguage.KA to "ಉದ್ಯೋಗ ಮತ್ತು ವೃತ್ತಿ",
            NDLanguage.HI to "रोज़गार और करियर"
        ),
        "chandrashtama_warning" to mapOf(
            NDLanguage.EN to "Chandrashtama effect active. Exercise patience and avoid high-risk actions.",
            NDLanguage.TA to "சந்திராஷ்டம காலம். பொறுமையைக் கடைப்பிடிக்கவும், ஆபத்தான செயல்களைத் தவிர்க்கவும்.",
            NDLanguage.KA to "ಚಂದ್ರಾಷ್ಟಮ ಪ್ರಭಾವವಿದೆ. ತಾಳ್ಮೆಯಿಂದಿರಿ ಮತ್ತು ಅಪಾಯಕಾರಿ ಕೆಲಸಗಳನ್ನು ತಪ್ಪಿಸಿ.",
            NDLanguage.HI to "चंद्राष्टम का प्रभाव सक्रिय है। धैर्य रखें और उच्च जोखिम वाले कार्यों से बचें।"
        ),
        "rating_high" to mapOf(
            NDLanguage.EN to "Strong positive influences. Progress and favorable developments expected.",
            NDLanguage.TA to "நன்மையான சூழ்நிலைகள் நிலவும். தொட்டதெல்லாம் துலங்கும், வளர்ச்சி கூடும்.",
            NDLanguage.KA to "ಉತ್ತಮ ಅನುಕೂಲಕರ ವಾತಾವರಣವಿದೆ. ಅಭಿವೃದ್ಧಿ ಮತ್ತು ಯಶಸ್ಸು ಸಿಗಲಿದೆ.",
            NDLanguage.HI to "मजबूत सकारात्मक प्रभाव। प्रगति और अनुकूल विकास की संभावना है।"
        ),
        "rating_moderate" to mapOf(
            NDLanguage.EN to "Moderate and stable conditions. Maintain routine efforts.",
            NDLanguage.TA to "சாதாரண நன்மைகள் உண்டாகும். வழக்கமான பணிகளில் கவனம் தேவை.",
            NDLanguage.KA to "ಸಾಧಾರಣವಾದ ದಿನ. ದಿನನಿತ್ಯದ ಕೆಲಸಗಳಲ್ಲಿ ನಿರತರಾಗಿರಿ.",
            NDLanguage.HI to "सामान्य और स्थिर स्थितियाँ। नियमित प्रयास जारी रखें।"
        ),
        "rating_low" to mapOf(
            NDLanguage.EN to "Minor delays or friction indicated. Exercise caution today.",
            NDLanguage.TA to "காரியங்களில் தடைகள் ஏற்படலாம். எச்சரிக்கையுடன் செயல்படவும்.",
            NDLanguage.KA to "ಕೆಲಸಗಳಲ್ಲಿ ಸಣ್ಣಪುಟ್ಟ ಅಡೆತಡೆಗಳಿರಬಹುದು. ಜಾಗರೂಕರಾಗಿರಿ.",
            NDLanguage.HI to "मामूली देरी या घर्षण की संभावना। आज सावधानी बरतें।"
        ),
        // --- Tamil Month ---
        "pa_chithirai" to mapOf(
            NDLanguage.EN to "Chithirai",
            NDLanguage.TA to "சித்திரை",
            NDLanguage.KA to "ಚೈತ್ರ",        // ✅ Corrected
            NDLanguage.HI to "चैत्र"
        ),
        "pa_vaikasi" to mapOf(
            NDLanguage.EN to "Vaikasi",
            NDLanguage.TA to "வைகாசி",
            NDLanguage.KA to "ವೈಶಾಖ",        // ✅ Corrected
            NDLanguage.HI to "वैशाख"
        ),
        "pa_aani" to mapOf(
            NDLanguage.EN to "Aani",
            NDLanguage.TA to "ஆனி",
            NDLanguage.KA to "ಜ್ಯೇಷ್ಠ",       // ✅ Corrected
            NDLanguage.HI to "ज्येष्ठ"
        ),
        "pa_aadi" to mapOf(
            NDLanguage.EN to "Aadi",
            NDLanguage.TA to "ஆடி",
            NDLanguage.KA to "ಆಷಾಢ",         // ✅ Corrected
            NDLanguage.HI to "आषाढ"
        ),
        "pa_avani" to mapOf(
            NDLanguage.EN to "Avani",
            NDLanguage.TA to "ஆவணி",
            NDLanguage.KA to "ಶ್ರಾವಣ",        // ✅ Corrected
            NDLanguage.HI to "श्रावण"
        ),
        "pa_purattasi" to mapOf(
            NDLanguage.EN to "Purattasi",
            NDLanguage.TA to "புரட்டாசி",
            NDLanguage.KA to "ಭಾದ್ರಪದ",       // ✅ Corrected
            NDLanguage.HI to "भाद्रपद"
        ),
        "pa_iyppasi" to mapOf(
            NDLanguage.EN to "Aippasi",
            NDLanguage.TA to "ஐப்பசி",
            NDLanguage.KA to "ಆಶ್ವಿನ",        // ✅ Corrected
            NDLanguage.HI to "आश्विन"
        ),
        "pa_karthigai" to mapOf(
            NDLanguage.EN to "Karthigai",
            NDLanguage.TA to "கார்த்திகை",
            NDLanguage.KA to "ಕಾರ್ತಿಕ",       // ✅ Corrected
            NDLanguage.HI to "कार्तिक"
        ),
        "pa_margazhi" to mapOf(
            NDLanguage.EN to "Margazhi",
            NDLanguage.TA to "மார்கழி",
            NDLanguage.KA to "ಮಾರ್ಗಶಿರ",      // ✅ Corrected
            NDLanguage.HI to "मार्गशीर्ष"
        ),
        "pa_thai" to mapOf(
            NDLanguage.EN to "Thai",
            NDLanguage.TA to "தை",
            NDLanguage.KA to "ಪೌಷ",           // ✅ Corrected
            NDLanguage.HI to "पौष"
        ),
        "pa_maasi" to mapOf(
            NDLanguage.EN to "Maasi",
            NDLanguage.TA to "மாசி",
            NDLanguage.KA to "ಮಾಘ",           // ✅ Corrected
            NDLanguage.HI to "माघ"
        ),
        "pa_panguni" to mapOf(
            NDLanguage.EN to "Panguni",
            NDLanguage.TA to "பங்குனி",
            NDLanguage.KA to "ಫಾಲ್ಗುಣ",       // ✅ Corrected
            NDLanguage.HI to "फाल्गुन"
        ),
        // --- Rasi ---
        "pa_mesha" to mapOf(
            NDLanguage.EN to "Mesha",
            NDLanguage.TA to "மேஷம்",
            NDLanguage.KA to "ಮೇಷ",
            NDLanguage.HI to "मेष"
        ),
        "pa_vrishabha" to mapOf(
            NDLanguage.EN to "Vrishabha",
            NDLanguage.TA to "ரிஷபம்",
            NDLanguage.KA to "ವೃಷಭ",
            NDLanguage.HI to "वृषभ"
        ),
        "pa_mithuna" to mapOf(
            NDLanguage.EN to "Mithuna",
            NDLanguage.TA to "மிதுனம்",
            NDLanguage.KA to "ಮಿಥುನ",
            NDLanguage.HI to "मिथुन"
        ),
        "pa_karkataka" to mapOf(
            NDLanguage.EN to "Karkataka",
            NDLanguage.TA to "கடகம்",
            NDLanguage.KA to "ಕರ್ಕಾಟಕ",
            NDLanguage.HI to "कर्क"
        ),
        "pa_simha" to mapOf(
            NDLanguage.EN to "Simha",
            NDLanguage.TA to "சிம்மம்",
            NDLanguage.KA to "ಸಿಂಹ",
            NDLanguage.HI to "सिंह"
        ),
        "pa_kanya" to mapOf(
            NDLanguage.EN to "Kanya",
            NDLanguage.TA to "கன்னி",
            NDLanguage.KA to "ಕನ್ಯಾ",
            NDLanguage.HI to "कन्या"
        ),
        "pa_thulaa" to mapOf(
            NDLanguage.EN to "Thulaa",
            NDLanguage.TA to "துலாம்",
            NDLanguage.KA to "ತುಲಾ",
            NDLanguage.HI to "तुला"
        ),
        "pa_vrishchika" to mapOf(
            NDLanguage.EN to "Vrishchika",
            NDLanguage.TA to "விருச்சிகம்",
            NDLanguage.KA to "ವೃಶ್ಚಿಕ",
            NDLanguage.HI to "वृश्चिक"
        ),
        "pa_dhanus" to mapOf(
            NDLanguage.EN to "Dhanus",
            NDLanguage.TA to "தனுசு",
            NDLanguage.KA to "ಧನು",
            NDLanguage.HI to "धनु"
        ),
        "pa_makara" to mapOf(
            NDLanguage.EN to "Makara",
            NDLanguage.TA to "மகரம்",
            NDLanguage.KA to "ಮಕರ",
            NDLanguage.HI to "मकर"
        ),
        "pa_kumbha" to mapOf(
            NDLanguage.EN to "Kumbha",
            NDLanguage.TA to "கும்பம்",
            NDLanguage.KA to "ಕುಂಭ",
            NDLanguage.HI to "कुंभ"
        ),
        "pa_meena" to mapOf(
            NDLanguage.EN to "Meena",
            NDLanguage.TA to "மீனம்",
            NDLanguage.KA to "ಮೀನ",
            NDLanguage.HI to "मीन"
        ),

        // --- Nakshatra ---
        "pa_ashwini" to mapOf(
            NDLanguage.EN to "Ashwini",
            NDLanguage.TA to "அஸ்வினி",
            NDLanguage.KA to "ಅಶ್ವಿನಿ",
            NDLanguage.HI to "अश्विनी"
        ),
        "pa_bharani" to mapOf(
            NDLanguage.EN to "Bharani",
            NDLanguage.TA to "பரணி",
            NDLanguage.KA to "ಭರಣಿ",
            NDLanguage.HI to "भरणी"
        ),
        "pa_kriththika" to mapOf(
            NDLanguage.EN to "Kriththika",
            NDLanguage.TA to "கிருத்திகை",
            NDLanguage.KA to "ಕೃತ್ತಿಕಾ",
            NDLanguage.HI to "कृत्तिका"
        ),
        "pa_rohini" to mapOf(
            NDLanguage.EN to "Rohini",
            NDLanguage.TA to "ரோகிணி",
            NDLanguage.KA to "ರೋಹಿಣಿ",
            NDLanguage.HI to "रोहिणी"
        ),
        "pa_mrigashirsha" to mapOf(
            NDLanguage.EN to "Mrigashirsha",
            NDLanguage.TA to "மிருகசீரிஷம்",
            NDLanguage.KA to "ಮೃಗಶಿರ",
            NDLanguage.HI to "मृगशिरा"
        ),
        "pa_aardhraa" to mapOf(
            NDLanguage.EN to "Ardra",
            NDLanguage.TA to "திருவாதிரை",
            NDLanguage.KA to "ಆದ್ರಾ",
            NDLanguage.HI to "आर्द्रा"
        ),
        "pa_punarvasu" to mapOf(
            NDLanguage.EN to "Punarvasu",
            NDLanguage.TA to "புனர்பூசம்",
            NDLanguage.KA to "ಪುನರ್ವಸು",
            NDLanguage.HI to "पुनर्वसु"
        ),
        "pa_pushya" to mapOf(
            NDLanguage.EN to "Pushya",
            NDLanguage.TA to "பூசம்",
            NDLanguage.KA to "ಪುಷ್ಯ",
            NDLanguage.HI to "पुष्य"
        ),
        "pa_ashlesha" to mapOf(
            NDLanguage.EN to "Ashlesha",
            NDLanguage.TA to "ஆயில்யம்",
            NDLanguage.KA to "ಆಶ್ಲೇಷಾ",
            NDLanguage.HI to "आश्लेषा"
        ),
        "pa_magha" to mapOf(
            NDLanguage.EN to "Magha",
            NDLanguage.TA to "மகம்",
            NDLanguage.KA to "ಮಘಾ",
            NDLanguage.HI to "मघा"
        ),
        "pa_purva_phalguni" to mapOf(
            NDLanguage.EN to "Purva Phalguni",
            NDLanguage.TA to "பூரம்",
            NDLanguage.KA to "ಪೂರ್ವ ಫಲ್ಗುಣಿ",
            NDLanguage.HI to "पूर्वा फाल्गुनी"
        ),
        "pa_uttara_phalguni" to mapOf(
            NDLanguage.EN to "Uttara Phalguni",
            NDLanguage.TA to "உத்திரம்",
            NDLanguage.KA to "ಉತ್ತರ ಫಲ್ಗುಣಿ",
            NDLanguage.HI to "उत्तरा फाल्गुनी"
        ),
        "pa_hastha" to mapOf(
            NDLanguage.EN to "Hasta",
            NDLanguage.TA to "ஹஸ்தம்",
            NDLanguage.KA to "ಹಸ್ತ",
            NDLanguage.HI to "हस्त"
        ),
        "pa_chitra" to mapOf(
            NDLanguage.EN to "Chitra",
            NDLanguage.TA to "சித்திரை",
            NDLanguage.KA to "ಚಿತ್ರಾ",
            NDLanguage.HI to "चित्रा"
        ),
        "pa_swaathi" to mapOf(
            NDLanguage.EN to "Swati",
            NDLanguage.TA to "ஸ்வாதி",
            NDLanguage.KA to "ಸ್ವಾತಿ",
            NDLanguage.HI to "स्वाति"
        ),
        "pa_vishakha" to mapOf(
            NDLanguage.EN to "Vishakha",
            NDLanguage.TA to "விசாகம்",
            NDLanguage.KA to "ವಿಶಾಖಾ",
            NDLanguage.HI to "विशाखा"
        ),
        "pa_anuradha" to mapOf(
            NDLanguage.EN to "Anuradha",
            NDLanguage.TA to "அனுஷம்",
            NDLanguage.KA to "ಅನುರಾಧಾ",
            NDLanguage.HI to "अनुराधा"
        ),
        "pa_jyeshtha" to mapOf(
            NDLanguage.EN to "Jyeshtha",
            NDLanguage.TA to "கேட்டை",
            NDLanguage.KA to "ಜ್ಯೇಷ್ಠಾ",
            NDLanguage.HI to "ज्येष्ठा"
        ),
        "pa_mula" to mapOf(
            NDLanguage.EN to "Mula",
            NDLanguage.TA to "மூலம்",
            NDLanguage.KA to "ಮೂಲ",
            NDLanguage.HI to "मूल"
        ),
        "pa_purva_ashada" to mapOf(
            NDLanguage.EN to "Purva Ashada",
            NDLanguage.TA to "பூராடம்",
            NDLanguage.KA to "ಪೂರ್ವಾಷಾಢಾ",
            NDLanguage.HI to "पूर्वाषाढा"
        ),
        "pa_uttara_ashada" to mapOf(
            NDLanguage.EN to "Uttara Ashada",
            NDLanguage.TA to "உத்திராடம்",
            NDLanguage.KA to "ಉತ್ತರಾಷಾಢಾ",
            NDLanguage.HI to "उत्तराषाढा"
        ),
        "pa_shravana" to mapOf(
            NDLanguage.EN to "Shravana",
            NDLanguage.TA to "திருவோணம்",
            NDLanguage.KA to "ಶ್ರವಣ",
            NDLanguage.HI to "श्रवण"
        ),
        "pa_dhanishta" to mapOf(
            NDLanguage.EN to "Dhanishta",
            NDLanguage.TA to "அவிட்டம்",
            NDLanguage.KA to "ಧನಿಷ್ಠಾ",
            NDLanguage.HI to "धनिष्ठा"
        ),
        "pa_shatabhisha" to mapOf(
            NDLanguage.EN to "Shatabhisha",
            NDLanguage.TA to "சதயம்",
            NDLanguage.KA to "ಶತಭಿಷಾ",
            NDLanguage.HI to "शतभिषा"
        ),
        "pa_purva_bhadrapada" to mapOf(
            NDLanguage.EN to "Purva Bhadrapada",
            NDLanguage.TA to "பூரட்டாதி",
            NDLanguage.KA to "ಪೂರ್ವಭಾದ್ರಪದ",
            NDLanguage.HI to "पूर्वा भाद्रपद"
        ),
        "pa_uttara_bhadrapada" to mapOf(
            NDLanguage.EN to "Uttara Bhadrapada",
            NDLanguage.TA to "உத்திரட்டாதி",
            NDLanguage.KA to "ಉತ್ತರಭಾದ್ರಪದ",
            NDLanguage.HI to "उत्तरा भाद्रपद"
        ),
        "pa_revathi" to mapOf(
            NDLanguage.EN to "Revathi",
            NDLanguage.TA to "ரேவதி",
            NDLanguage.KA to "ರೇವತಿ",
            NDLanguage.HI to "रेवती"
        ),

        // --- Thithi ---
        "pa_prathama" to mapOf(
            NDLanguage.EN to "Prathama",
            NDLanguage.TA to "பிரதமை",
            NDLanguage.KA to "ಪ್ರತಿಪದೆ",
            NDLanguage.HI to "प्रतिपदा"
        ),
        "pa_dwitiya" to mapOf(
            NDLanguage.EN to "Dwitiya",
            NDLanguage.TA to "த்விதியை",
            NDLanguage.KA to "ದ್ವಿತೀಯಾ",
            NDLanguage.HI to "द्वितीया"
        ),
        "pa_tritiya" to mapOf(
            NDLanguage.EN to "Tritiya",
            NDLanguage.TA to "த்ரிதியை",
            NDLanguage.KA to "ತೃತೀಯಾ",
            NDLanguage.HI to "तृतीया"
        ),
        "pa_chaturthi" to mapOf(
            NDLanguage.EN to "Chaturthi",
            NDLanguage.TA to "சதுர்த்தி",
            NDLanguage.KA to "ಚತುರ್ಥಿ",
            NDLanguage.HI to "चतुर्थी"
        ),
        "pa_panchami" to mapOf(
            NDLanguage.EN to "Panchami",
            NDLanguage.TA to "பஞ்சமி",
            NDLanguage.KA to "ಪಂಚಮಿ",
            NDLanguage.HI to "पंचमी"
        ),
        "pa_shashti" to mapOf(
            NDLanguage.EN to "Shashti",
            NDLanguage.TA to "சஷ்டி",
            NDLanguage.KA to "ಷಷ್ಠಿ",
            NDLanguage.HI to "षष्ठी"
        ),
        "pa_saptami" to mapOf(
            NDLanguage.EN to "Saptami",
            NDLanguage.TA to "சப்தமி",
            NDLanguage.KA to "ಸಪ್ತಮಿ",
            NDLanguage.HI to "सप्तमी"
        ),
        "pa_ashtami" to mapOf(
            NDLanguage.EN to "Ashtami",
            NDLanguage.TA to "அஷ்டமி",
            NDLanguage.KA to "ಅಷ್ಟಮಿ",
            NDLanguage.HI to "अष्टमी"
        ),
        "pa_navami" to mapOf(
            NDLanguage.EN to "Navami",
            NDLanguage.TA to "நவமி",
            NDLanguage.KA to "ನವಮಿ",
            NDLanguage.HI to "नवमी"
        ),
        "pa_dashami" to mapOf(
            NDLanguage.EN to "Dasami",
            NDLanguage.TA to "தசமி",
            NDLanguage.KA to "ದಶಮಿ",
            NDLanguage.HI to "दशमी"
        ),
        "pa_ekadashi" to mapOf(
            NDLanguage.EN to "Ekadasi",
            NDLanguage.TA to "ஏகாதசி",
            NDLanguage.KA to "ಏಕಾದಶಿ",
            NDLanguage.HI to "एकादशी"
        ),
        "pa_dwadashi" to mapOf(
            NDLanguage.EN to "Dwadasi",
            NDLanguage.TA to "துவாதசி",
            NDLanguage.KA to "ದ್ವಾದಶಿ",
            NDLanguage.HI to "द्वादशी"
        ),
        "pa_trayodashi" to mapOf(
            NDLanguage.EN to "Trayodasi",
            NDLanguage.TA to "திரயோதசி",
            NDLanguage.KA to "ತ್ರಯೋದಶಿ",
            NDLanguage.HI to "त्रयोदशी"
        ),
        "pa_chaturdashi" to mapOf(
            NDLanguage.EN to "Chaturdasi",
            NDLanguage.TA to "சதுர்த்தசி",
            NDLanguage.KA to "ಚತುರ್ದಶಿ",
            NDLanguage.HI to "चतुर्दशी"
        ),
        "pa_purnima" to mapOf(
            NDLanguage.EN to "Purnima",
            NDLanguage.TA to "பௌர்ணமி",
            NDLanguage.KA to "ಪೌರ್ಣಮಿ",
            NDLanguage.HI to "पूर्णिमा"
        ),
        "pa_amavasya" to mapOf(
            NDLanguage.EN to "Amavasya",
            NDLanguage.TA to "அமாவாசை",
            NDLanguage.KA to "ಅಮಾವಾಸ್ಯೆ",
            NDLanguage.HI to "अमावस्या"
        ),

        // --- Yoga ---
        "pa_vishkambha" to mapOf(
            NDLanguage.EN to "Vishkambha",
            NDLanguage.TA to "விஷ்கம்ப",
            NDLanguage.KA to "ವಿಷ್ಕಂಭ",
            NDLanguage.HI to "विष्कंभ"
        ),
        "pa_preeti" to mapOf(
            NDLanguage.EN to "Preethi",
            NDLanguage.TA to "ப்ரீதி",
            NDLanguage.KA to "ಪ್ರೀತಿ",
            NDLanguage.HI to "प्रीति"
        ),
        "pa_ayushman" to mapOf(
            NDLanguage.EN to "Ayushman",
            NDLanguage.TA to "ஆயுஷ்மான்",
            NDLanguage.KA to "ಆಯುಷ್ಮಾನ್",
            NDLanguage.HI to "आयुष्मान"
        ),
        "pa_saubhagya" to mapOf(
            NDLanguage.EN to "Saubhagya",
            NDLanguage.TA to "சௌபாக்ய",
            NDLanguage.KA to "ಸೌಭಾಗ್ಯ",
            NDLanguage.HI to "सौभाग्य"
        ),
        "pa_shobhana" to mapOf(
            NDLanguage.EN to "Shobhana",
            NDLanguage.TA to "சோபன",
            NDLanguage.KA to "ಶೋಭನ",
            NDLanguage.HI to "शोभन"
        ),
        "pa_atiganda" to mapOf(
            NDLanguage.EN to "Athiganda",
            NDLanguage.TA to "அதிகண்ட",
            NDLanguage.KA to "ಅತಿಗಂಡ",
            NDLanguage.HI to "अतिगंड"
        ),
        "pa_sukarma" to mapOf(
            NDLanguage.EN to "Sukarma",
            NDLanguage.TA to "சுகர்மா",
            NDLanguage.KA to "ಸುಕರ್ಮ",
            NDLanguage.HI to "सुकर्मा"
        ),
        "pa_dhriti" to mapOf(
            NDLanguage.EN to "Dhriti",
            NDLanguage.TA to "த்ரிதி",
            NDLanguage.KA to "ಧೃತಿ",
            NDLanguage.HI to "धृति"
        ),
        "pa_shula" to mapOf(
            NDLanguage.EN to "Shula",
            NDLanguage.TA to "சூல",
            NDLanguage.KA to "ಶೂಲ",
            NDLanguage.HI to "शूल"
        ),
        "pa_ganda" to mapOf(
            NDLanguage.EN to "Ganda",
            NDLanguage.TA to "கண்ட",
            NDLanguage.KA to "ಗಂಡ",
            NDLanguage.HI to "गंड"
        ),
        "pa_vriddhi" to mapOf(
            NDLanguage.EN to "Vriddhi",
            NDLanguage.TA to "வ்ருத்தி",
            NDLanguage.KA to "ವೃದ್ಧಿ",
            NDLanguage.HI to "वृद्धि"
        ),
        "pa_dhruva" to mapOf(
            NDLanguage.EN to "Dhruva",
            NDLanguage.TA to "துருவ",
            NDLanguage.KA to "ಧ್ರುವ",
            NDLanguage.HI to "ध्रुव"
        ),
        "pa_vyaghata" to mapOf(
            NDLanguage.EN to "Vyaghata",
            NDLanguage.TA to "வ்யாகத",
            NDLanguage.KA to "ವ್ಯಾಘಾತ",
            NDLanguage.HI to "व्याघात"
        ),
        "pa_harshana" to mapOf(
            NDLanguage.EN to "Harshana",
            NDLanguage.TA to "ஹர்ஷண",
            NDLanguage.KA to "ಹರ್ಷಣ",
            NDLanguage.HI to "हर्षण"
        ),
        "pa_vajra" to mapOf(
            NDLanguage.EN to "Vajra",
            NDLanguage.TA to "வஜ்ர",
            NDLanguage.KA to "ವಜ್ರ",
            NDLanguage.HI to "वज्र"
        ),
        "pa_siddhi" to mapOf(
            NDLanguage.EN to "Siddhi",
            NDLanguage.TA to "சித்தி",
            NDLanguage.KA to "ಸಿದ್ಧಿ",
            NDLanguage.HI to "सिद्धि"
        ),
        "pa_vyatipata" to mapOf(
            NDLanguage.EN to "Vyatipata",
            NDLanguage.TA to "வ்யதிபாத",
            NDLanguage.KA to "ವ್ಯತೀಪಾತ",
            NDLanguage.HI to "व्यतीपात"
        ),
        "pa_variyan" to mapOf(
            NDLanguage.EN to "Variyan",
            NDLanguage.TA to "வரியான்",
            NDLanguage.KA to "ವರೀಯಾನ್",
            NDLanguage.HI to "वरियान"
        ),
        "pa_parigha" to mapOf(
            NDLanguage.EN to "Parigha",
            NDLanguage.TA to "பரிக",
            NDLanguage.KA to "ಪರಿಘ",
            NDLanguage.HI to "परिघ"
        ),
        "pa_shiva" to mapOf(
            NDLanguage.EN to "Shiva",
            NDLanguage.TA to "சிவ",
            NDLanguage.KA to "ಶಿವ",
            NDLanguage.HI to "शिव"
        ),
        "pa_siddha" to mapOf(
            NDLanguage.EN to "Siddha",
            NDLanguage.TA to "சித்த",
            NDLanguage.KA to "ಸಿದ್ಧ",
            NDLanguage.HI to "सिद्ध"
        ),
        "pa_sadhya" to mapOf(
            NDLanguage.EN to "Sadhya",
            NDLanguage.TA to "சாத்ய",
            NDLanguage.KA to "ಸಾಧ್ಯ",
            NDLanguage.HI to "साध्य"
        ),
        "pa_shubha" to mapOf(
            NDLanguage.EN to "Shubha",
            NDLanguage.TA to "சுப",
            NDLanguage.KA to "ಶುಭ",
            NDLanguage.HI to "शुभ"
        ),
        "pa_brahma" to mapOf(
            NDLanguage.EN to "Brahma",
            NDLanguage.TA to "பிரம்ம",
            NDLanguage.KA to "ಬ್ರಹ್ಮ",
            NDLanguage.HI to "ब्रह्म"
        ),
        "pa_indra" to mapOf(
            NDLanguage.EN to "Indra",
            NDLanguage.TA to "இந்திர",
            NDLanguage.KA to "ಇಂದ್ರ",
            NDLanguage.HI to "इंद्र"
        ),
        "pa_vaidhriti" to mapOf(
            NDLanguage.EN to "Vaidhriti",
            NDLanguage.TA to "வைத்ரிதி",
            NDLanguage.KA to "ವೈಧೃತಿ",
            NDLanguage.HI to "वैधृति"
        ),

        // --- Karana ---
        "pa_bava" to mapOf(
            NDLanguage.EN to "Bava",
            NDLanguage.TA to "பவ",
            NDLanguage.KA to "ಬವ",
            NDLanguage.HI to "बव"
        ),
        "pa_balava" to mapOf(
            NDLanguage.EN to "Balava",
            NDLanguage.TA to "பாலவ",
            NDLanguage.KA to "ಬಾಲವ",
            NDLanguage.HI to "बालव"
        ),
        "pa_kaulava" to mapOf(
            NDLanguage.EN to "Kaulava",
            NDLanguage.TA to "கௌலவ",
            NDLanguage.KA to "ಕೌಲವ",
            NDLanguage.HI to "कौलव"
        ),
        "pa_taitila" to mapOf(
            NDLanguage.EN to "Taitila",
            NDLanguage.TA to "தைதில",
            NDLanguage.KA to "ತೈತಿಲ",
            NDLanguage.HI to "तैतिल"
        ),
        "pa_garaja" to mapOf(
            NDLanguage.EN to "Garaja",
            NDLanguage.TA to "கரஜ",
            NDLanguage.KA to "ಗರಜ",
            NDLanguage.HI to "गरज"
        ),
        "pa_vanija" to mapOf(
            NDLanguage.EN to "Vanija",
            NDLanguage.TA to "வணிஜ",
            NDLanguage.KA to "ವಣಿಜ",
            NDLanguage.HI to "वणिज"
        ),
        "pa_vishti" to mapOf(
            NDLanguage.EN to "Vishti",
            NDLanguage.TA to "விஷ்டி",
            NDLanguage.KA to "ವಿಷ್ಟಿ",
            NDLanguage.HI to "विष्टि"
        ),
        "pa_shakuni" to mapOf(
            NDLanguage.EN to "Shakuni",
            NDLanguage.TA to "சாகுனி",
            NDLanguage.KA to "ಶಕುನಿ",
            NDLanguage.HI to "शकुनि"
        ),
        "pa_chatushpada" to mapOf(
            NDLanguage.EN to "Chatushpada",
            NDLanguage.TA to "சதுஷ்பாத",
            NDLanguage.KA to "ಚತುಷ್ಪಾದ",
            NDLanguage.HI to "चतुष्पाद"
        ),
        "pa_naga" to mapOf(
            NDLanguage.EN to "Naaga",
            NDLanguage.TA to "நாக",
            NDLanguage.KA to "ನಾಗ",
            NDLanguage.HI to "नाग"
        ),
        "pa_kimsthugna" to mapOf(
            NDLanguage.EN to "Kimsthugna",
            NDLanguage.TA to "கிம்ஸ்துக்ன",
            NDLanguage.KA to "ಕಿಂಸ್ತುಘ್ನ",
            NDLanguage.HI to "किम्स्तुघ्न"
        )
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