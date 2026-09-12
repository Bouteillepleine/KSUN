package com.rifsxd.ksunext.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Accent presets, taken from the Catppuccin Latte (light) and Macchiato (dark)
 * accent ramps so every option stays in the same family as the rest of the palette.
 */
enum class ThemeAccent(
    val key: String,
    val light: Color,
    val dark: Color
) {
    Blue("blue", Color(0xFF1E66F5), Color(0xFF8AADF4)),
    Sky("sky", Color(0xFF0797C9), Color(0xFF91D7E3)),
    Teal("teal", Color(0xFF179299), Color(0xFF8BD5CA)),
    Green("green", Color(0xFF40A02B), Color(0xFFA6DA95)),
    Yellow("yellow", Color(0xFFCB8410), Color(0xFFEED49F)),
    Peach("peach", Color(0xFFE85D04), Color(0xFFF5A97F)),
    Maroon("maroon", Color(0xFFE64553), Color(0xFFEE99A0)),
    Red("red", Color(0xFFD20F39), Color(0xFFED8796)),
    Pink("pink", Color(0xFFEA76CB), Color(0xFFF5BDE6)),
    Mauve("mauve", Color(0xFF8839EF), Color(0xFFC6A0F6)),
    Lavender("lavender", Color(0xFF5A6FE8), Color(0xFFB7BDF8));

    fun swatch(darkTheme: Boolean): Color = if (darkTheme) dark else light

    companion object {
        val Default = Blue
        const val CUSTOM_KEY = "custom"

        fun fromKey(key: String?): ThemeAccent =
            entries.firstOrNull { it.key == key } ?: Default
    }
}

/** The light/dark pair actually handed to the theme. */
data class AccentPair(val light: Color, val dark: Color) {
    fun swatch(darkTheme: Boolean): Color = if (darkTheme) dark else light
}

private fun clamp(v: Float, min: Float, max: Float) = v.coerceIn(min, max)

/**
 * Derive a light/dark accent pair from any picked colour.
 *
 * The hue is kept exactly as chosen; saturation and value are clamped into the
 * ranges the Catppuccin ramps occupy (deep and saturated for Latte, pastel and
 * bright for Macchiato). Without this a fully saturated pick is unreadable in one
 * theme or the other — e.g. pure yellow on a white surface.
 */
fun customAccentPair(argb: Int): AccentPair {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    val h = hsv[0]
    val s = hsv[1]
    val v = hsv[2]

    val light = Color.hsv(h, clamp(s, 0.55f, 0.95f), clamp(v, 0.62f, 0.92f))
    val dark = Color.hsv(h, clamp(s * 0.6f, 0.25f, 0.60f), clamp(maxOf(v, 0.88f), 0.88f, 1.0f))
    return AccentPair(light, dark)
}

/** Resolve the stored preference into the pair the theme uses. */
fun resolveAccent(key: String?, customArgb: Int): AccentPair =
    if (key == ThemeAccent.CUSTOM_KEY) {
        customAccentPair(customArgb)
    } else {
        val preset = ThemeAccent.fromKey(key)
        AccentPair(preset.light, preset.dark)
    }

val DEFAULT_CUSTOM_ARGB: Int = ThemeAccent.Default.light.toArgb()

val DefaultAccentPair = AccentPair(ThemeAccent.Default.light, ThemeAccent.Default.dark)
