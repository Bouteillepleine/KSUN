package com.rifsxd.ksunext.ui.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MACCHIATO_BLUE,
    onPrimary = MACCHIATO_ON_BLUE,
    primaryContainer = MACCHIATO_BLUE_CONTAINER,
    onPrimaryContainer = MACCHIATO_ON_BLUE_CONTAINER,
    inversePrimary = LATTE_BLUE,
    secondary = MACCHIATO_LAVENDER,
    onSecondary = MACCHIATO_ON_LAVENDER,
    secondaryContainer = MACCHIATO_LAVENDER_CONTAINER,
    onSecondaryContainer = MACCHIATO_ON_LAVENDER_CONTAINER,
    tertiary = MACCHIATO_TEAL,
    onTertiary = MACCHIATO_ON_TEAL,
    tertiaryContainer = MACCHIATO_TEAL_CONTAINER,
    onTertiaryContainer = MACCHIATO_ON_TEAL_CONTAINER,
    error = MACCHIATO_RED,
    onError = MACCHIATO_ON_RED,
    errorContainer = MACCHIATO_RED_CONTAINER,
    onErrorContainer = MACCHIATO_ON_RED_CONTAINER,
    background = MACCHIATO_BASE,
    onBackground = MACCHIATO_TEXT,
    surface = MACCHIATO_BASE,
    onSurface = MACCHIATO_TEXT,
    surfaceVariant = DARK_GREY,
    onSurfaceVariant = MACCHIATO_SUBTEXT,
    surfaceTint = MACCHIATO_BLUE,
    inverseSurface = MACCHIATO_TEXT,
    inverseOnSurface = MACCHIATO_BASE,
    outline = MACCHIATO_OVERLAY,
    outlineVariant = MACCHIATO_SURFACE1,
    surfaceContainerLowest = MACCHIATO_CRUST,
    surfaceContainerLow = MACCHIATO_SURFACE_LOW,
    surfaceContainer = MACCHIATO_SURFACE_MID,
    surfaceContainerHigh = MACCHIATO_SURFACE_HIGH,
    surfaceContainerHighest = DARK_GREY,
    scrim = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = LATTE_BLUE,
    onPrimary = LATTE_ON_BLUE,
    primaryContainer = LATTE_BLUE_CONTAINER,
    onPrimaryContainer = LATTE_ON_BLUE_CONTAINER,
    inversePrimary = MACCHIATO_BLUE,
    secondary = LATTE_LAVENDER,
    onSecondary = LATTE_ON_LAVENDER,
    secondaryContainer = LATTE_LAVENDER_CONTAINER,
    onSecondaryContainer = LATTE_ON_LAVENDER_CONTAINER,
    tertiary = LATTE_TEAL,
    onTertiary = LATTE_ON_TEAL,
    tertiaryContainer = LATTE_TEAL_CONTAINER,
    onTertiaryContainer = LATTE_ON_TEAL_CONTAINER,
    error = LATTE_RED,
    onError = LATTE_ON_RED,
    errorContainer = LATTE_RED_CONTAINER,
    onErrorContainer = LATTE_ON_RED_CONTAINER,
    background = LATTE_BASE,
    onBackground = LATTE_TEXT,
    surface = LATTE_BASE,
    onSurface = LATTE_TEXT,
    surfaceVariant = LATTE_MANTLE,
    onSurfaceVariant = LATTE_SUBTEXT,
    surfaceTint = LATTE_BLUE,
    inverseSurface = LATTE_TEXT,
    inverseOnSurface = LATTE_BASE,
    outline = LATTE_OVERLAY,
    outlineVariant = LATTE_SURFACE0,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F7FA),
    surfaceContainer = Color(0xFFE9ECF1),
    surfaceContainerHigh = LATTE_MANTLE,
    surfaceContainerHighest = LATTE_CRUST,
    scrim = Color.Black
)

fun Color.blend(other: Color, ratio: Float): Color {
    val inverse = 1f - ratio
    return Color(
        red = red * inverse + other.red * ratio,
        green = green * inverse + other.green * ratio,
        blue = blue * inverse + other.blue * ratio,
        alpha = alpha
    )
}

@Composable
fun KernelSUTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    amoledMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        amoledMode && darkTheme && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            val dynamicScheme = dynamicDarkColorScheme(context)
            dynamicScheme.copy(
                background = AMOLED_BLACK,
                surface = AMOLED_BLACK,
                surfaceVariant = dynamicScheme.surfaceVariant.blend(AMOLED_BLACK, 0.6f),
                surfaceContainer = dynamicScheme.surfaceContainer.blend(AMOLED_BLACK, 0.6f),
                surfaceContainerLow = dynamicScheme.surfaceContainerLow.blend(AMOLED_BLACK, 0.6f),
                surfaceContainerLowest = dynamicScheme.surfaceContainerLowest.blend(AMOLED_BLACK, 0.6f),
                surfaceContainerHigh = dynamicScheme.surfaceContainerHigh.blend(AMOLED_BLACK, 0.6f),
                surfaceContainerHighest = dynamicScheme.surfaceContainerHighest.blend(AMOLED_BLACK, 0.6f)
            )
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        amoledMode && darkTheme -> {
            DarkColorScheme.copy(
                background = AMOLED_BLACK,
                surface = AMOLED_BLACK,
                surfaceVariant = DARK_GREY.blend(AMOLED_BLACK, 0.8f),
                surfaceContainer = DARK_GREY.blend(AMOLED_BLACK, 0.8f),
                surfaceContainerLow = DARK_GREY.blend(AMOLED_BLACK, 0.8f),
                surfaceContainerLowest = DARK_GREY.blend(AMOLED_BLACK, 0.8f),
                surfaceContainerHigh = DARK_GREY.blend(AMOLED_BLACK, 0.8f),
                surfaceContainerHighest = DARK_GREY.blend(AMOLED_BLACK, 0.8f),
            )
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    SystemBarStyle(
        darkMode = darkTheme
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
private fun SystemBarStyle(
    darkMode: Boolean,
    statusBarScrim: Color = Color.Transparent,
    navigationBarScrim: Color = Color.Transparent,
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    SideEffect {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                statusBarScrim.toArgb(),
                statusBarScrim.toArgb(),
            ) { darkMode },
            navigationBarStyle = when {
                darkMode -> SystemBarStyle.dark(
                    navigationBarScrim.toArgb()
                )

                else -> SystemBarStyle.light(
                    navigationBarScrim.toArgb(),
                    navigationBarScrim.toArgb(),
                )
            }
        )
    }
}
