package pl.domain.application.mixology.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    primary = DarkPrimary,
    onPrimary = White,
    secondary = DarkSecondary,
    onSecondary = White,
    tertiary = White
)

private val LightColorScheme = lightColorScheme(
    background = LightLavender,
    onBackground = Black,
    surface = DeepPurple,
    onSurface = White,
    primary = MediumPurple,
    onPrimary = White,
    secondary = Lavender,
    onSecondary = Black,
    tertiary = StrongPurple
    //tertiary = Pink40,
    //background = Purple80

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MixologyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}