package io.github.ilievi187.recurra.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.Subscription
import io.github.ilievi187.recurra.ui.state.RecurraUiState
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@Composable
fun CalendarScreen(
    state: RecurraUiState,
    onEditSubscription: (Subscription) -> Unit,
) {
    val today = FinanceCalculator.today()
    var currentMonth by remember { mutableStateOf(LocalDate(today.year, today.monthNumber, 1)) }
    var selectedDay by remember { mutableStateOf<Int?>(today.dayOfMonth) }

    val base = state.vault.settings.baseCurrency
    val rates = state.vault.settings.ratesToUsd
    val renewalsByDay = remember(state.vault.subscriptions, currentMonth) {
        FinanceCalculator.renewalsInMonth(state.vault.subscriptions, currentMonth.year, currentMonth.monthNumber)
    }
    val monthTotal = renewalsByDay.values.flatten().sumOf {
        FinanceCalculator.convertAmount(it.amount, it.currency, base, rates)
    }

    val nextMonth = currentMonth.plus(DatePeriod(months = 1))
    val daysInMonth = currentMonth.daysUntil(nextMonth)
    val firstDayOffset = (currentMonth.dayOfWeek.ordinal) // Monday = 0
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Renewal Calendar", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "Projected total for ${monthNames[currentMonth.monthNumber - 1]} ${currentMonth.year}: ${FinanceCalculator.formatCurrency(monthTotal, base)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        currentMonth = currentMonth.minus(DatePeriod(months = 1))
                        selectedDay = null
                    }) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous month")
                    }
                    Text(
                        text = "${monthNames[currentMonth.monthNumber - 1].take(3)} ${currentMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(onClick = {
                        currentMonth = currentMonth.plus(DatePeriod(months = 1))
                        selectedDay = null
                    }) {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = "Next month")
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
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
                                    val isSelected = selectedDay == dayNum
                                    val isToday = currentMonth.year == today.year &&
                                        currentMonth.monthNumber == today.monthNumber &&
                                        dayNum == today.dayOfMonth
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                                            daySubs.isNotEmpty() -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                            else -> Color.Transparent
                                        },
                                        border = if (isToday) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp)
                                            .clickable { selectedDay = dayNum },
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = dayNum.toString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                            )
                                            if (daySubs.isNotEmpty()) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                    daySubs.take(4).forEach { s ->
                                                        Box(
                                                            modifier = Modifier
                                                                .size(7.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(s.category.colorHex)),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).height(54.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            val day = selectedDay
            val daySubs = if (day != null) renewalsByDay[day].orEmpty() else emptyList()
            SectionCard(
                title = if (day != null) {
                    "Renewals on ${monthNames[currentMonth.monthNumber - 1]} $day, ${currentMonth.year}"
                } else {
                    "Select a day to inspect renewals"
                },
            ) {
                if (daySubs.isEmpty()) {
                    Text(
                        text = "No renewals scheduled for this day.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        daySubs.forEach { sub ->
                            Surface(
                                onClick = { onEditSubscription(sub) },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(sub.category.colorHex)),
                                    )
                                    Spacer(Modifier.width(10.dp))
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
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
