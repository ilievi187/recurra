package io.github.ilievi187.recurra.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.CategorySpend
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.MonthlyProjection

@Composable
fun CategoryDonutChart(
    breakdown: List<CategorySpend>,
    baseCurrency: String,
    monthlyTotal: Double,
    modifier: Modifier = Modifier,
) {
    if (breakdown.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Add active subscriptions to see category spend.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(
            modifier = Modifier.size(148.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(148.dp)) {
                val strokeWidth = 22.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth),
                )

                var startAngle = -90f
                breakdown.forEach { item ->
                    val sweep = (item.share * 360.0).toFloat().coerceAtLeast(2f)
                    drawArc(
                        color = Color(item.category.colorHex),
                        startAngle = startAngle,
                        sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    )
                    startAngle += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Monthly",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = FinanceCalculator.formatCurrency(monthlyTotal, baseCurrency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            breakdown.take(6).forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(item.category.colorHex)),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.category.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${(item.share * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = FinanceCalculator.formatCurrency(item.monthlyAmount, baseCurrency),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun CashflowBarChart(
    projections: List<MonthlyProjection>,
    baseCurrency: String,
    modifier: Modifier = Modifier,
) {
    val maxAmount = (projections.maxOfOrNull { it.totalAmount } ?: 0.0).coerceAtLeast(1.0)
    val primary = MaterialTheme.colorScheme.primary
    val accent = MaterialTheme.colorScheme.secondary
    val surfaceVar = MaterialTheme.colorScheme.surfaceVariant
    val avg = if (projections.isNotEmpty()) projections.map { it.totalAmount }.average() else 0.0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Peak: ${FinanceCalculator.formatCurrency(maxAmount, baseCurrency)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Avg/mo: ${FinanceCalculator.formatCurrency(avg, baseCurrency)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(12.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
        ) {
            if (projections.isEmpty()) return@Canvas
            val barSpacing = 10.dp.toPx()
            val totalSpacing = barSpacing * (projections.size - 1)
            val barWidth = ((size.width - totalSpacing) / projections.size).coerceAtLeast(8f)
            val chartHeight = size.height - 8.dp.toPx()

            projections.forEachIndexed { index, month ->
                val x = index * (barWidth + barSpacing)
                // Background track
                drawRoundRect(
                    color = surfaceVar,
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, chartHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                )
                val ratio = (month.totalAmount / maxAmount).toFloat().coerceIn(0f, 1f)
                val barHeight = (chartHeight * ratio).coerceAtLeast(if (month.totalAmount > 0) 6f else 0f)
                val isSpike = month.totalAmount > avg * 1.35 && avg > 0
                drawRoundRect(
                    color = if (isSpike) accent else primary,
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            projections.forEach { month ->
                Text(
                    text = month.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
