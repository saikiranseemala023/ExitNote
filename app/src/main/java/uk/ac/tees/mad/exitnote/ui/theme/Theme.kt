package uk.ac.tees.mad.exitnote.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = SoftBlue,
    onPrimary = Color.White,
    primaryContainer = SoftBlueLight,
    onPrimaryContainer = DarkGray,

    secondary = MediumGray,
    onSecondary = Color.White,
    secondaryContainer = VeryLightGray,
    onSecondaryContainer = DarkGray,

    tertiary = LightGray,
    onTertiary = Color.White,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = BackgroundGray,
    onBackground = DarkGray,

    surface = Color.White,
    onSurface = DarkGray,
    surfaceVariant = VeryLightGray,
    onSurfaceVariant = MediumGray,

    outline = LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8B9FE8),
    onPrimary = DarkBackground,
    primaryContainer = SoftBlueDark,
    onPrimaryContainer = SoftBlueLight,

    secondary = LightGray,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = VeryLightGray,

    tertiary = LightGray,
    onTertiary = DarkBackground,

    error = ErrorRed,
    onError = DarkBackground,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = DarkBackground,
    onBackground = VeryLightGray,

    surface = DarkSurface,
    onSurface = VeryLightGray,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = LightGray,

    outline = DarkSurfaceVariant
)

@Composable
fun ExitNoteTheme(
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
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}