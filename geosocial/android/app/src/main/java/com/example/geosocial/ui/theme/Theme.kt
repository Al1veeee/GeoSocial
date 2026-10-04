package com.example.geosocial.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Teal = Color(0xFF00696D)
private val TealLight = Color(0xFF6FF6FC)
private val Coral = Color(0xFFB3261E)

private val LightColors = lightColorScheme(
    primary = Teal,
    secondary = Color(0xFF4A6365),
    tertiary = Color(0xFF4B607C),
    error = Coral
)

private val DarkColors = darkColorScheme(
    primary = TealLight,
    secondary = Color(0xFFB1CBCD),
    tertiary = Color(0xFFB3C8E8)
)

@Composable
fun GeoSocialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
