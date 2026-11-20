package io.github.ilievi187.recurra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.ui.components.CashflowBarChart
import io.github.ilievi187.recurra.ui.components.CategoryDonutChart
import io.github.ilievi187.recurra.ui.state.RecurraUiState

@Composable
fun AnalyticsScreen(state: RecurraUiState) {
    val base = state.vault.settings.baseCurrency

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text("Analytics & Cashflow", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "12-month renewal forecast (annual spikes highlighted in indigo) & payment log",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SectionCard(title = "12-Month Projected Cashflow ($base)") {
                CashflowBarChart(
                    projections = state.projections,
                    baseCurrency = base,
                )
            }
        }

        item {
            SectionCard(title = "Category Distribution") {
                CategoryDonutChart(
                    breakdown = state.summary.categoryBreakdown,
                    baseCurrency = base,
                    monthlyTotal = state.summary.monthlyTotal,
                )
            }
        }

        item {
            SectionCard(title = "Payment History (${state.vault.paymentHistory.size})") {
                if (state.vault.paymentHistory.isEmpty()) {
                    Text(
                        text = "No payments recorded yet. Click the green checkmark on any subscription to record a payment and advance its cycle.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.vault.paymentHistory.take(25).forEachIndexed { idx, rec ->
                            if (idx > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(rec.subscriptionName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "${rec.paidDate}${if (rec.note.isNotBlank()) " · ${rec.note}" else ""}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    text = FinanceCalculator.formatCurrency(rec.amount, rec.currency),
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
