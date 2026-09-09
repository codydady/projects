package com.sd.nithyadharma.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import com.sd.nithyadharma.model.NDLanguage
import com.sd.nithyadharma.util.LocalAppLanguage

/* Core App Colors & Schemes ... (Keep your color definitions) */

// 🔑 Helper to safely scale TextStyle font size and line height
private fun TextStyle.scale(factor: Float): TextStyle {
    return this.copy(
        fontSize = if (fontSize.isSpecified) fontSize * factor else TextUnit.Unspecified,
        lineHeight = if (lineHeight.isSpecified) lineHeight * factor else TextUnit.Unspecified
    )
}

// 🔑 Helper to scale an entire Typography system
private fun Typography.scaleAll(factor: Float): Typography {
    return Typography(
        displayLarge = displayLarge.scale(factor),
        displayMedium = displayMedium.scale(factor),
        displaySmall = displaySmall.scale(factor),
        headlineLarge = headlineLarge.scale(factor),
        headlineMedium = headlineMedium.scale(factor),
        headlineSmall = headlineSmall.scale(factor),
        titleLarge = titleLarge.scale(factor),
        titleMedium = titleMedium.scale(factor),
        titleSmall = titleSmall.scale(factor),
        bodyLarge = bodyLarge.scale(factor),
        bodyMedium = bodyMedium.scale(factor),
        bodySmall = bodySmall.scale(factor),
        labelLarge = labelLarge.scale(factor),
        labelMedium = labelMedium.scale(factor),
        labelSmall = labelSmall.scale(factor)
    )
}

@Composable
fun NithyaDharmaTheme(
    content: @Composable () -> Unit
) {
    val currentLang = LocalAppLanguage.current // 🔑 Read inside the theme!
    val baseTypography = Typography()

    // 🔑 Tamil fonts often render ~15-20% visually larger than Latin fonts.
    // Scaling by 0.85f gives a clean, comfortable match.
//    val typography = if (currentLang == NDLanguage.TA) {
//        baseTypography.scaleAll(0.8f)
//    } else {
//        baseTypography.scaleAll(1.0f)
//    }
    // scale everything to 80% of fontsize. sep 7, 2026
    val typography = baseTypography.scaleAll(0.9f)
//    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
//        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}