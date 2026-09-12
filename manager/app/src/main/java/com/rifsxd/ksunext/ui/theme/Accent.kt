package com.rifsxd.ksunext.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Accent choices for the built-in theme, taken from the Catppuccin Latte (light)
 * and Macchiato (dark) accent ramps so every option stays in the same family as
 * the rest of the palette.
 *
 * Containers are derived from the accent rather than hand-picked, which keeps the
 * ten options consistent with each other and avoids eighty hand-tuned constants.
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

    /** Colour shown in the picker for the theme currently in use. */
    fun swatch(darkTheme: Boolean): Color = if (darkTheme) dark else light

    companion object {
        val Default = Blue

        fun fromKey(key: String?): ThemeAccent =
            entries.firstOrNull { it.key == key } ?: Default
    }
}
