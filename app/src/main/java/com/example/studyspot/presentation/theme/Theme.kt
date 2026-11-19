package com.example.studyspot.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.example.studyspot.R

@Composable
private fun studySpotDarkColorScheme() = darkColorScheme(
    primary = colorResource(R.color.theme_dark_primary),
    secondary = colorResource(R.color.theme_dark_secondary),
    tertiary = colorResource(R.color.theme_dark_tertiary),
)

@Composable
private fun studySpotLightColorScheme() = lightColorScheme(
    primary = colorResource(R.color.theme_light_primary),
    secondary = colorResource(R.color.theme_light_secondary),
    tertiary = colorResource(R.color.theme_light_tertiary),
)

@Composable
fun StudySpotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> studySpotDarkColorScheme()
        else -> studySpotLightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
