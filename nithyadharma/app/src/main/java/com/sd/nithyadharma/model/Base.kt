package com.sd.nithyadharma.model

enum class NDLanguage {
    EN, TA
}

fun languageName(lang: NDLanguage): String =
    when (lang) {
        NDLanguage.EN -> "English"
        NDLanguage.TA -> "தமிழ் (Tamil)"
    }

//class Base {
//}