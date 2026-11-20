package io.github.ilievi187.recurra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.ilievi187.recurra.domain.ThemePreference

object RecurraPalette {
    // Warm Botanical & Google Calendar–inspired Material You accents
    val Sage = Color(0xFF2E7D5B)
    val SageLight = Color(0xFF52A67E)
    val MintSoft = Color(0xFFE2F2E9)
    val CreamCanvas = Color(0xFFFAF9F4)
    val WarmYellow = Color(0xFFF4B942)
    val ButterYellow = Color(0xFFFEF3C7)
    val WarmPeach = Color(0xFFE88D67)
    val SoftCoral = Color(0xFFE06D53)
    val Lavender = Color(0xFF8B7EC8)
    val SkySoft = Color(0xFF5FA8D3)

    // Compatibility aliases
    val Emerald = Sage
    val Violet = Lavender
    val Amber = WarmYellow
    val Rose = SoftCoral
    val Cyan = SkySoft

    fun heroGradient(isDark: Boolean): Brush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF1F3329),
                Color(0xFF2B3E2E),
                Color(0xFF3A3623),
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFE7F5EC),
                Color(0xFFF3F7E4),
                Color(0xFFFEF6DA),
            ),
        )
    }

    fun calendarSkyGradient(monthNumber: Int, isDark: Boolean): Brush {
        val colors = when (monthNumber) {
            12, 1, 2 -> if (isDark) {
                listOf(Color(0xFF1C2D36), Color(0xFF243B40))
            } else {
                listOf(Color(0xFFE4F2F7), Color(0xFFEBF5EE), Color(0xFFFDF8E6))
            }
            3, 4, 5 -> if (isDark) {
                listOf(Color(0xFF1D3529), Color(0xFF2B412C))
            } else {
                listOf(Color(0xFFE3F5E8), Color(0xFFF2F9DF), Color(0xFFFFF4D6))
            }
            6, 7, 8 -> if (isDark) {
                listOf(Color(0xFF2F341D), Color(0xFF3D341E))
            } else {
                listOf(Color(0xFFFFF6D6), Color(0xFFF3F8E0), Color(0xFFE4F5EA))
            }
            else -> if (isDark) {
                listOf(Color(0xFF362B20), Color(0xFF293324))
            } else {
                listOf(Color(0xFFFEF1DD), Color(0xFFF9F4E1), Color(0xFFE9F3E6))
            }
        }
        return Brush.linearGradient(colors)
    }
}

private val WarmLightColors = lightColorScheme(
    primary = Color(0xFF2E7D5B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF3E6),
    onPrimaryContainer = Color(0xFF14422E),
    secondary = Color(0xFFD48C1C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF2CF),
    onSecondaryContainer = Color(0xFF523505),
    tertiary = Color(0xFFD96B4F),
    onTertiary = Color.White,
    background = Color(0xFFFAF9F4),
    onBackground = Color(0xFF1F2A24),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F2A24),
    surfaceVariant = Color(0xFFF2F1E8),
    onSurfaceVariant = Color(0xFF5A655E),
    outline = Color(0xFFD6D5C9),
    outlineVariant = Color(0xFFE8E6DA),
    error = Color(0xFFD1495B),
    onError = Color.White,
)

private val WarmDarkColors = darkColorScheme(
    primary = Color(0xFF6CC99C),
    onPrimary = Color(0xFF0B2E1E),
    primaryContainer = Color(0xFF1E4A36),
    onPrimaryContainer = Color(0xFFDDF3E6),
    secondary = Color(0xFFF5C969),
    onSecondary = Color(0xFF3D2804),
    secondaryContainer = Color(0xFF4C3A16),
    onSecondaryContainer = Color(0xFFFEF2CF),
    tertiary = Color(0xFFF29C7E),
    onTertiary = Color(0xFF471C0D),
    background = Color(0xFF141815),
    onBackground = Color(0xFFF3F2EC),
    surface = Color(0xFF1C221E),
    onSurface = Color(0xFFF3F2EC),
    surfaceVariant = Color(0xFF262E28),
    onSurfaceVariant = Color(0xFFAAB4AC),
    outline = Color(0xFF3D4740),
    outlineVariant = Color(0xFF2D3630),
    error = Color(0xFFF28482),
    onError = Color(0xFF471216),
)

private val RecurraTypography = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun RecurraTheme(
    preference: ThemePreference,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (preference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) WarmDarkColors else WarmLightColors,
        typography = RecurraTypography,
        content = content,
    )
}
