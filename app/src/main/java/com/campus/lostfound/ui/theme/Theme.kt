package com.campus.lostfound.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color(0xFF000000),
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = TealLight,
    onSecondary = Color(0xFF000000),
    secondaryContainer = TealDark,
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = PrimaryCyan,
    onTertiary = Color(0xFF000000),
    error = LostRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = LostRedDark,
    onErrorContainer = Color(0xFFFFFFFF),
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = PrimaryBlueDark,
    secondary = Teal,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDark,
    tertiary = PrimaryCyan,
    onTertiary = Color(0xFFFFFFFF),
    error = LostRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = LostRedLight,
    onErrorContainer = LostRedDark,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFF7F2FA),
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFE7E0EC)
)

@Composable
fun CampusLostFoundTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

