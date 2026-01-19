package com.sd.nithyadharma.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.sd.nithyadharma.model.NDLanguage

/* ------------------ */
/* Core App Colors    */
/* ------------------ */

// Your ivory background
val IvoryBackground = Color(0xFFF1E1B4)
val IvoryLightBackground = Color(0xFFF5F5DC)
// Text colors chosen for ivory
val IvoryTextLight = Color(0xFF412121)   // Dark brown-black
val IvoryTextDark  = Color(0xFF3D1E1E)   // Even stronger contrast

// Accent (used for buttons, highlights, icons)
val Saffron = Color(0xFF41260F)

// Card surface colors
val IvoryCardLight = Color(0xFFF7ECD1)
val IvoryCardDark  = Color(0xFFE8D8A8)

val LightGreenBackground = Color(0xFFBDE1DE)

/* ------------------ */
/* Light Color Scheme */
/* ------------------ */

private val LightColors = lightColorScheme(
    background = IvoryLightBackground,
    onBackground = IvoryTextLight,

    surface = IvoryBackground,
    onSurface = IvoryTextLight,

    primary = Saffron,
    onPrimary = Color.White,

    primaryContainer = IvoryBackground,     // ⭐ YOUR IVORY
    onPrimaryContainer = IvoryTextLight,

    secondary = Saffron,
    onSecondary = Color.White,

    secondaryContainer = LightGreenBackground,
    onSecondaryContainer = IvoryTextLight,

    surfaceVariant = IvoryCardLight,
    onSurfaceVariant = IvoryTextLight
)

/* Dark Color Scheme  */
/*
Important: We STILL keep ivory.
Dark theme here means:
- darker text
- slightly deeper card color
NOT black background
*/

private val DarkColors = darkColorScheme(
    background = IvoryLightBackground,
    onBackground = IvoryTextDark,

    surface = IvoryBackground,
    onSurface = IvoryTextLight,

    primary = Saffron,
    onPrimary = Color.Black,

    primaryContainer = IvoryBackground,     // ⭐ YOUR IVORY
    onPrimaryContainer = IvoryTextLight,

    secondary = Saffron,
    onSecondary = Color.Black,

    secondaryContainer = LightGreenBackground,     // ⭐ YOUR IVORY
    onSecondaryContainer = IvoryTextLight,

    surfaceVariant = IvoryCardDark,
    onSurfaceVariant = IvoryTextDark
)

/* ------------------ */
/* App Theme Wrapper  */
/* ------------------ */

@Composable
fun NithyaDharmaTheme(
    currentLang: NDLanguage,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Base typography (default Material3)
    val baseTypography = Typography()

    // Create Tamil-adjusted typography (reduce sizes by 10-20%)
    val tamilTypography = baseTypography.copy(
        displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * 0.80f),
        displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * 0.80f),
        displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * 0.80f),
        headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * 0.80f),
        headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * 0.80f),
        headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * 0.80f),
        titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * 0.80f),
        titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * 0.80f),
        titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * 0.80f),
        bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * 0.80f),
        bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * 0.80f),
        bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * 0.80f),
        labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * 0.80f),
        labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * 0.80f),
        labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * 0.80f)
    )

    // Choose typography based on language
    val typography = if (currentLang == NDLanguage.TA) tamilTypography else baseTypography
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,  // ← Use adjusted typography
        content = content
    )

}
