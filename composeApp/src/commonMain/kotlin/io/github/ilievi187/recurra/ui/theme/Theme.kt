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
    val Emerald = Color(0xFF10B981)
    val EmeraldDeep = Color(0xFF059669)
    val Violet = Color(0xFF8B5CF6)
    val VioletDeep = Color(0xFF6D28D9)
    val Amber = Color(0xFFF59E0B)
    val Rose = Color(0xFFF43F5E)
    val Cyan = Color(0xFF06B6D4)

    fun heroGradient(isDark: Boolean): Brush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF132A2E),
                Color(0xFF1B1E34),
                Color(0xFF231934),
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF065F46),
                Color(0xFF1E3A5F),
                Color(0xFF4C1D95),
            ),
        )
    }

    fun calendarBannerGradient(monthNumber: Int, isDark: Boolean): Brush {
        val pair = when (monthNumber) {
            12, 1, 2 -> if (isDark) Color(0xFF172554) to Color(0xFF1E1B4B) else Color(0xFF1D4ED8) to Color(0xFF4338CA)
            3, 4, 5 -> if (isDark) Color(0xFF064E3B) to Color(0xFF134E4A) else Color(0xFF059669) to Color(0xFF0D9488)
            6, 7, 8 -> if (isDark) Color(0xFF78350F) to Color(0xFF581C87) else Color(0xFFD97706) to Color(0xFF9333EA)
            else -> if (isDark) Color(0xFF4C1D95) to Color(0xFF7C2D12) else Color(0xFF7C3AED) to Color(0xFFEA580C)
        }
        return Brush.linearGradient(listOf(pair.first, pair.second))
    }
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF6D28D9),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF3B0764),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFEEF0F6),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
    error = Color(0xFFE11D48),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF10B981),
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = Color(0xFFA78BFA),
    onSecondary = Color(0xFF2E1065),
    secondaryContainer = Color(0xFF3B246C),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF451A03),
    background = Color(0xFF0D0F14),
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF151923),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF1E2433),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF252C3D),
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519),
)

private val RecurraTypography = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
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
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = RecurraTypography,
        content = content,
    )
}
