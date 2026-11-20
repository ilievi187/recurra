package io.github.ilievi187.recurra.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.Subscription
import io.github.ilievi187.recurra.domain.ThemePreference
import io.github.ilievi187.recurra.ui.components.AmbientOrbsBackdrop
import io.github.ilievi187.recurra.ui.components.AnimatedCurrencyText
import io.github.ilievi187.recurra.ui.components.CategoryAvatar
import io.github.ilievi187.recurra.ui.components.categoryIcon
import io.github.ilievi187.recurra.ui.components.seasonIcon
import io.github.ilievi187.recurra.ui.state.RecurraUiState
import io.github.ilievi187.recurra.ui.theme.RecurraPalette
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@Composable
fun CalendarScreen(
    state: RecurraUiState,
    onEditSubscription: (Subscription) -> Unit,
    onMarkPaid: (String) -> Unit,
) {
    val today = FinanceCalculator.today()
    var currentMonth by remember { mutableStateOf(LocalDate(today.year, today.monthNumber, 1)) }
    var selectedDay by remember { mutableStateOf<Int?>(today.dayOfMonth) }

    val isDark = when (state.vault.settings.themePreference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    val base = state.vault.settings.baseCurrency
    val rates = state.vault.settings.ratesToUsd
    val renewalsByDay = remember(state.vault.subscriptions, currentMonth) {
        FinanceCalculator.renewalsInMonth(state.vault.subscriptions, currentMonth.year, currentMonth.monthNumber)
    }
    val allMonthSubs = renewalsByDay.values.flatten()
    val monthTotal = allMonthSubs.sumOf {
        FinanceCalculator.convertAmount(it.amount, it.currency, base, rates)
    }

    val nextMonth = currentMonth.plus(DatePeriod(months = 1))
    val daysInMonth = currentMonth.daysUntil(nextMonth)
    val firstDayOffset = currentMonth.dayOfWeek.ordinal // Monday = 0
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Seasonal Visual Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RecurraPalette.calendarBannerGradient(currentMonth.monthNumber, isDark)),
                ) {
                    AmbientOrbsBackdrop(
                        primaryGlow = Color.White,
                        secondaryGlow = RecurraPalette.Amber,
                        modifier = Modifier.matchParentSize(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = seasonIcon(currentMonth.monthNumber),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = "${monthNames[currentMonth.monthNumber - 1]} ${currentMonth.year}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    AnimatedCurrencyText(
                                        amount = monthTotal,
                                        currencyCode = base,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.92f),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = "· ${allMonthSubs.size} renewal(s)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.78f),
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.14f),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        currentMonth = currentMonth.minus(DatePeriod(months = 1))
                                        selectedDay = null
                                    }) {
                                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous month", tint = Color.White)
                                    }
                                    TextButton(onClick = {
                                        currentMonth = LocalDate(today.year, today.monthNumber, 1)
                                        selectedDay = today.dayOfMonth
                                    }) {
                                        Text("Today", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                    IconButton(onClick = {
                                        currentMonth = currentMonth.plus(DatePeriod(months = 1))
                                        selectedDay = null
                                    }) {
                                        Icon(Icons.Rounded.ChevronRight, contentDescription = "Next month", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Visual Calendar Grid with Category Icons & Daily Totals
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { d ->
                            Text(
                                text = d,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    val totalCells = firstDayOffset + daysInMonth
                    val rows = (totalCells + 6) / 7
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            for (c in 0 until 7) {
                                val dayNum = r * 7 + c - firstDayOffset + 1
                                if (dayNum in 1..daysInMonth) {
                                    val daySubs = renewalsByDay[dayNum].orEmpty()
                                    val dayTotal = daySubs.sumOf {
                                        FinanceCalculator.convertAmount(it.amount, it.currency, base, rates)
                                    }
                                    val isSelected = selectedDay == dayNum
                                    val isToday = currentMonth.year == today.year &&
                                        currentMonth.monthNumber == today.monthNumber &&
                                        dayNum == today.dayOfMonth

                                    val cellBg by animateColorAsState(
                                        targetValue = when {
                                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                                            daySubs.isNotEmpty() -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                        },
                                        animationSpec = tween(180),
                                        label = "dayBg",
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = cellBg,
                                        border = when {
                                            isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                            isToday -> BorderStroke(1.5.dp, RecurraPalette.Emerald)
                                            daySubs.isNotEmpty() -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                            else -> null
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(74.dp)
                                            .clickable { selectedDay = dayNum },
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(6.dp),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isToday || isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = if (isToday) RecurraPalette.Emerald else MaterialTheme.colorScheme.onSurface,
                                                )
                                                if (daySubs.size > 2) {
                                                    Text(
                                                        text = "+${daySubs.size - 2}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                    )
                                                }
                                            }

                                            if (daySubs.isNotEmpty()) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    daySubs.take(2).forEach { sub ->
                                                        val accent = Color(sub.category.colorHex)
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(accent.copy(alpha = 0.22f)),
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Icon(
                                                                imageVector = categoryIcon(sub.category),
                                                                contentDescription = sub.name,
                                                                tint = accent,
                                                                modifier = Modifier.size(12.dp),
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = FinanceCalculator.formatCurrency(dayTotal, base),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).height(74.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Animated Selected Day Detail Card
        item {
            AnimatedContent(
                targetState = selectedDay,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "calendarDayDetail",
            ) { day ->
                val daySubs = if (day != null) renewalsByDay[day].orEmpty() else emptyList()
                SectionCard(
                    title = if (day != null) {
                        "Renewals on ${monthNames[currentMonth.monthNumber - 1]} $day, ${currentMonth.year}"
                    } else {
                        "Select a day to inspect renewals"
                    },
                ) {
                    if (daySubs.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                Icons.Rounded.EventAvailable,
                                contentDescription = null,
                                tint = RecurraPalette.Emerald,
                                modifier = Modifier.size(22.dp),
                            )
                            Text(
                                text = "All clear! No renewals scheduled for this day.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            daySubs.forEach { sub ->
                                Surface(
                                    onClick = { onEditSubscription(sub) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        CategoryAvatar(category = sub.category, size = 38.dp, iconSize = 19.dp)
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(sub.name, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                text = "${sub.category.displayName} · ${sub.cycle.displayName}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Text(
                                            text = FinanceCalculator.formatCurrency(sub.amount, sub.currency),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        IconButton(onClick = { onMarkPaid(sub.id) }) {
                                            Icon(
                                                Icons.Rounded.CheckCircle,
                                                contentDescription = "Mark paid",
                                                tint = RecurraPalette.Emerald,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
