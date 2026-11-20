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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.SubscriptionCategory

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
    val shape = RoundedCornerShape((size.value * 0.3f).dp)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.24f),
                        accent.copy(alpha = 0.10f),
                    ),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.35f), shape),
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

@Composable
fun AmbientOrbsBackdrop(
    primaryGlow: Color,
    secondaryGlow: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryGlow.copy(alpha = 0.28f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.2f),
                radius = size.maxDimension * 0.45f,
            ),
            radius = size.maxDimension * 0.45f,
            center = Offset(size.width * 0.85f, size.height * 0.2f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryGlow.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(size.width * 0.12f, size.height * 0.85f),
                radius = size.maxDimension * 0.4f,
            ),
            radius = size.maxDimension * 0.4f,
            center = Offset(size.width * 0.12f, size.height * 0.85f),
        )
    }
}
