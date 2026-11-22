package moc.itivo.officeworks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    primaryContainer = BrandBlueContainer,
    secondary = Accent,
    tertiary = Accent,
    background = SurfaceLight,
    surface = SurfaceLight,
)

private val DarkColors = darkColorScheme(
    primary = BrandBlueLight,
    primaryContainer = BrandBlueContainerDark,
    secondary = AccentLight,
    tertiary = AccentLight,
    background = SurfaceDark,
    surface = SurfaceDark,
)

/**
 * The single place the look and feel is defined. Dynamic (wallpaper) colour is deliberately
 * off: the client is investing in a brand look, and Android 12+ would otherwise replace it.
 */
@Composable
fun OfficeworksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
