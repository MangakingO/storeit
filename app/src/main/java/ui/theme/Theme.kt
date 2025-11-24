package com.example.storeit.ui.theme

import android.app.Activity
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
    primary = StoreItBlue_Primary_Dark,
    secondary = StoreItBlue_Secondary_Dark,
    background = StoreItGrey_Background_Dark,
    surface = StoreItGrey_Surface_Dark,
    onPrimary = StoreItText_Primary_Dark,
    onSecondary = StoreItText_Secondary_Dark,
    onBackground = StoreItText_Primary_Dark,
    onSurface = StoreItText_Primary_Dark,
    error = StoreItRed_Error
)

private val LightColorScheme = lightColorScheme(
    primary = StoreItBlue_Primary,
    secondary = StoreItBlue_Secondary,
    background = StoreItGrey_Background,
    surface = StoreItGrey_Surface,
    onPrimary = StoreItText_Primary,
    onSecondary = StoreItText_Secondary,
    onBackground = StoreItText_Primary,
    onSurface = StoreItText_Primary,
    error = StoreItRed_Error
)

@Composable
fun StoreitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
