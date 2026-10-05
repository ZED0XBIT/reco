package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium iOS Light Color Scheme (Primary Experience)
private val LightColorScheme = lightColorScheme(
    primary = MonoBlack,
    onPrimary = MonoWhite,
    primaryContainer = Neutral100,
    onPrimaryContainer = MonoBlack,
    secondary = Neutral800,
    onSecondary = MonoWhite,
    secondaryContainer = Neutral150,
    onSecondaryContainer = MonoBlack,
    tertiary = RecordRed,
    onTertiary = MonoWhite,
    tertiaryContainer = RecordRedLight,
    onTertiaryContainer = RecordRed,
    background = Neutral50,
    onBackground = MonoBlack,
    surface = MonoWhite,
    onSurface = MonoBlack,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral600,
    outline = Neutral200,
    outlineVariant = Neutral300
)

// Premium iOS Dark Color Scheme (Subtle Charcoal / OLED)
private val DarkColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = Neutral850,
    onPrimaryContainer = MonoWhite,
    secondary = Neutral400,
    onSecondary = MonoBlack,
    secondaryContainer = Neutral800,
    onSecondaryContainer = MonoWhite,
    tertiary = RecordRed,
    onTertiary = MonoWhite,
    tertiaryContainer = RecordRedDark,
    onTertiaryContainer = MonoWhite,
    background = Neutral950,
    onBackground = MonoWhite,
    surface = Neutral900,
    onSurface = MonoWhite,
    surfaceVariant = Neutral850,
    onSurfaceVariant = Neutral400,
    outline = Neutral800,
    outlineVariant = Neutral700
)

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Composable
fun ProtectYourselfTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT, // Default to pristine luxury Light mode
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
