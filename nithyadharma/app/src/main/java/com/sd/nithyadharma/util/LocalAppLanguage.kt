package com.sd.nithyadharma.util

import androidx.compose.runtime.staticCompositionLocalOf
import com.sd.nithyadharma.model.NDLanguage

val LocalAppLanguage = staticCompositionLocalOf<NDLanguage> {
    error("No NDLanguage provided!")
}