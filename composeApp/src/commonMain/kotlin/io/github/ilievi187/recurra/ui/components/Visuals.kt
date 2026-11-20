package io.github.ilievi187.recurra.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.SubscriptionCategory
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun categoryIcon(category: SubscriptionCategory): ImageVector = when (category) {
    SubscriptionCategory.STREAMING -> Icons.Rounded.Movie
    SubscriptionCategory.CLOUD_DEV -> Icons.Rounded.Cloud
    SubscriptionCategory.PRODUCTIVITY -> Icons.Rounded.AutoAwesome
    SubscriptionCategory.UTILITIES -> Icons.Rounded.Bolt
    SubscriptionCategory.GAMING -> Icons.Rounded.SportsEsports
    SubscriptionCategory.HEALTH_FITNESS -> Icons.Rounded.FitnessCenter
    SubscriptionCategory.INSURANCE -> Icons.Rounded.AccountBalance
    SubscriptionCategory.DOMAINS_HOSTING -> Icons.Rounded.Language
    SubscriptionCategory.EDUCATION -> Icons.Rounded.School
    SubscriptionCategory.OTHER -> Icons.Rounded.Category
}

fun seasonIcon(monthNumber: Int): ImageVector = when (monthNumber) {
    12, 1, 2 -> Icons.Rounded.AcUnit
    3, 4, 5 -> Icons.Rounded.LocalFlorist
    6, 7, 8 -> Icons.Rounded.WbSunny
    else -> Icons.Rounded.Park
}

@Composable
fun CategoryAvatar(
    category: SubscriptionCategory,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
) {
    val accent = Color(category.colorHex)
    val shape = RoundedCornerShape((size.value * 0.34f).dp)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.18f),
                        accent.copy(alpha = 0.08f),
                    ),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.25f), shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = category.displayName,
            tint = accent,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun AnimatedCurrencyText(
    amount: Double,
    currencyCode: String,
    style: TextStyle,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
) {
    val animatedValue by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "currencyCounter",
    )
    Text(
        text = FinanceCalculator.formatCurrency(animatedValue.toDouble(), currencyCode),
        style = style,
        color = color,
        fontWeight = fontWeight,
    )
}

/**
 * Google Calendar–inspired warm botanical & seasonal header illustration
 * drawn natively in Compose Canvas (rolling sunlit hills, soft sun, clouds,
 * leaves, and blooming flowers).
 */
@Composable
fun BotanicalIllustrationCanvas(
    monthNumber: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Warm sun + soft halo in the upper right
        val sunCenter = Offset(w * 0.86f, h * 0.26f)
        val sunColor = if (isDark) Color(0xFFF6C85F) else Color(0xFFF8CB58)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(sunColor.copy(alpha = if (isDark) 0.28f else 0.45f), Color.Transparent),
                center = sunCenter,
                radius = h * 0.72f,
            ),
            radius = h * 0.72f,
            center = sunCenter,
        )
        drawCircle(
            color = sunColor.copy(alpha = if (isDark) 0.65f else 0.85f),
            radius = h * 0.16f,
            center = sunCenter,
        )

        // Soft drifting clouds
        val cloudColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.75f)
        drawSoftCloud(Offset(w * 0.64f, h * 0.25f), h * 0.14f, cloudColor)
        drawSoftCloud(Offset(w * 0.36f, h * 0.18f), h * 0.10f, cloudColor.copy(alpha = cloudColor.alpha * 0.7f))

        // Back rolling hill
        val backHillColor = when (monthNumber) {
            12, 1, 2 -> if (isDark) Color(0xFF28464B) else Color(0xFFC5E3DA)
            6, 7, 8 -> if (isDark) Color(0xFF394B2B) else Color(0xFFD5E9B8)
            9, 10, 11 -> if (isDark) Color(0xFF4A3B28) else Color(0xFFF4D9B0)
            else -> if (isDark) Color(0xFF264A39) else Color(0xFFC4E7D0)
        }
        val backHill = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.76f)
            cubicTo(w * 0.28f, h * 0.56f, w * 0.56f, h * 0.90f, w, h * 0.62f)
            lineTo(w, h)
            close()
        }
        drawPath(backHill, color = backHillColor.copy(alpha = 0.85f))

        // Foreground gentle meadow hill
        val frontHillColor = when (monthNumber) {
            12, 1, 2 -> if (isDark) Color(0xFF1F383C) else Color(0xFFABD8CA)
            6, 7, 8 -> if (isDark) Color(0xFF2C4225) else Color(0xFFB5DFA8)
            9, 10, 11 -> if (isDark) Color(0xFF3A3022) else Color(0xFFECC694)
            else -> if (isDark) Color(0xFF1F3F2F) else Color(0xFFA7DAB9)
        }
        val frontHill = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.88f)
            cubicTo(w * 0.35f, h * 0.72f, w * 0.68f, h * 0.64f, w, h * 0.82f)
            lineTo(w, h)
            close()
        }
        drawPath(frontHill, color = frontHillColor.copy(alpha = 0.9f))

        // Botanical flowers & stems blooming along the right & bottom meadow
        val stemColor = if (isDark) Color(0xFF66BB8A) else Color(0xFF2E7D5B)
        val petalPalette = when (monthNumber) {
            12, 1, 2 -> listOf(Color(0xFFFFFFFF), Color(0xFFBAE6FD), Color(0xFFFDE68A))
            6, 7, 8 -> listOf(Color(0xFFFBBF24), Color(0xFFFB7185), Color(0xFFFFFFFF))
            9, 10, 11 -> listOf(Color(0xFFF59E0B), Color(0xFFEA580C), Color(0xFFFDE68A))
            else -> listOf(Color(0xFFFDA4AF), Color(0xFFFFFFFF), Color(0xFFFDE047))
        }

        drawFlower(
            base = Offset(w * 0.74f, h * 0.96f),
            stemHeight = h * 0.34f,
            petalRadius = h * 0.065f,
            stemColor = stemColor,
            petalColor = petalPalette[0],
            centerColor = Color(0xFFF59E0B),
        )
        drawFlower(
            base = Offset(w * 0.82f, h * 0.98f),
            stemHeight = h * 0.25f,
            petalRadius = h * 0.055f,
            stemColor = stemColor,
            petalColor = petalPalette[1],
            centerColor = Color(0xFFFBBF24),
        )
        drawFlower(
            base = Offset(w * 0.91f, h * 0.96f),
            stemHeight = h * 0.38f,
            petalRadius = h * 0.072f,
            stemColor = stemColor,
            petalColor = petalPalette[2],
            centerColor = Color(0xFFE07A5F),
        )
        drawFlower(
            base = Offset(w * 0.66f, h * 0.98f),
            stemHeight = h * 0.20f,
            petalRadius = h * 0.048f,
            stemColor = stemColor,
            petalColor = petalPalette[1],
            centerColor = Color(0xFFF59E0B),
        )
    }
}

private fun DrawScope.drawSoftCloud(center: Offset, radius: Float, color: Color) {
    drawCircle(color = color, radius = radius, center = center)
    drawCircle(color = color, radius = radius * 0.8f, center = Offset(center.x - radius * 0.9f, center.y + radius * 0.18f))
    drawCircle(color = color, radius = radius * 0.75f, center = Offset(center.x + radius * 0.9f, center.y + radius * 0.22f))
}

private fun DrawScope.drawFlower(
    base: Offset,
    stemHeight: Float,
    petalRadius: Float,
    stemColor: Color,
    petalColor: Color,
    centerColor: Color,
) {
    val head = Offset(base.x, base.y - stemHeight)
    // Stem
    drawLine(
        color = stemColor.copy(alpha = 0.85f),
        start = base,
        end = head,
        strokeWidth = 2.6f,
        cap = StrokeCap.Round,
    )
    // Two soft leaves
    drawLine(
        color = stemColor.copy(alpha = 0.75f),
        start = Offset(base.x, base.y - stemHeight * 0.42f),
        end = Offset(base.x - petalRadius * 1.4f, base.y - stemHeight * 0.56f),
        strokeWidth = 3.2f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = stemColor.copy(alpha = 0.75f),
        start = Offset(base.x, base.y - stemHeight * 0.30f),
        end = Offset(base.x + petalRadius * 1.3f, base.y - stemHeight * 0.44f),
        strokeWidth = 3.2f,
        cap = StrokeCap.Round,
    )
    // 5 blooming petals
    val petalCount = 5
    for (i in 0 until petalCount) {
        val angle = (2.0 * PI * i / petalCount) - PI / 2.0
        val px = head.x + (cos(angle) * petalRadius * 0.95f).toFloat()
        val py = head.y + (sin(angle) * petalRadius * 0.95f).toFloat()
        drawCircle(
            color = petalColor.copy(alpha = 0.92f),
            radius = petalRadius * 0.72f,
            center = Offset(px, py),
        )
    }
    // Warm golden flower center
    drawCircle(
        color = centerColor,
        radius = petalRadius * 0.52f,
        center = head,
    )
}
