package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CosmicPrimary,
    secondary = CosmicSecondary,
    tertiary = CosmicAccentPurple,
    background = CosmicBlack,
    surface = CosmicDarkGray,
    onPrimary = CosmicOnPrimary,
    onSecondary = CosmicOnPrimary,
    onBackground = CosmicOnPrimary,
    onSurface = CosmicOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = SolidLightPrimary,
    secondary = SolidLightSecondary,
    tertiary = CosmicAccentPurple,
    background = SolidLightBg,
    surface = SolidLightSurface,
    onPrimary = CosmicOnPrimary,
    onSecondary = SolidLightText,
    onBackground = SolidLightText,
    onSurface = SolidLightText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled dynamic system color to preserve our premium hand-crafted brand theme
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
