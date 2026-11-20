package io.github.ilievi187.recurra

import io.github.ilievi187.recurra.domain.AppSettings
import io.github.ilievi187.recurra.domain.BillingCycle
import io.github.ilievi187.recurra.domain.FinanceCalculator
import io.github.ilievi187.recurra.domain.RecurraVault
import io.github.ilievi187.recurra.domain.Subscription
import io.github.ilievi187.recurra.domain.SubscriptionCategory
import io.github.ilievi187.recurra.domain.SubscriptionStatus
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FinanceCalculatorTest {

    @Test
    fun normalizesBillingCyclesAccurately() {
        val monthly = Subscription(
            id = "1",
            name = "Monthly",
            amount = 12.0,
            cycle = BillingCycle.MONTHLY,
            nextRenewalDate = "2026-12-01",
        )
        val yearly = monthly.copy(id = "2", amount = 120.0, cycle = BillingCycle.YEARLY)
        val quarterly = monthly.copy(id = "3", amount = 30.0, cycle = BillingCycle.QUARTERLY)
        val weekly = monthly.copy(id = "4", amount = 10.0, cycle = BillingCycle.WEEKLY)

        assertEquals(12.0, FinanceCalculator.normalizedMonthlyAmount(monthly), 0.001)
        assertEquals(10.0, FinanceCalculator.normalizedMonthlyAmount(yearly), 0.001)
        assertEquals(10.0, FinanceCalculator.normalizedMonthlyAmount(quarterly), 0.001)
        assertTrue(abs(FinanceCalculator.normalizedMonthlyAmount(weekly) - 43.333) < 0.01)
    }

    @Test
    fun convertsMultiCurrencyAndComputesDashboardSummary() {
        val ref = LocalDate(2026, 11, 20)
        val vault = RecurraVault(
            subscriptions = listOf(
                Subscription(
                    id = "s1",
                    name = "Cloud",
                    amount = 10.0,
                    currency = "EUR",
                    cycle = BillingCycle.MONTHLY,
                    nextRenewalDate = "2026-11-25",
                    category = SubscriptionCategory.CLOUD_DEV,
                ),
                Subscription(
                    id = "s2",
                    name = "Music",
                    amount = 120.0,
                    currency = "USD",
                    cycle = BillingCycle.YEARLY,
                    nextRenewalDate = "2026-12-10",
                    category = SubscriptionCategory.STREAMING,
                ),
                Subscription(
                    id = "s3",
                    name = "Paused",
                    amount = 99.0,
                    currency = "USD",
                    cycle = BillingCycle.MONTHLY,
                    nextRenewalDate = "2026-11-21",
                    status = SubscriptionStatus.PAUSED,
                ),
            ),
            settings = AppSettings(
                baseCurrency = "USD",
                ratesToUsd = mapOf("USD" to 1.0, "EUR" to 1.1),
            ),
        )

        val summary = FinanceCalculator.computeDashboardSummary(vault, ref)
        // EUR 10 * 1.1 = 11.0 USD/mo + USD 120/12 = 10.0 USD/mo => 21.0 USD/mo
        assertEquals(21.0, summary.monthlyTotal, 0.001)
        assertEquals(252.0, summary.yearlyTotal, 0.001)
        assertEquals(2, summary.activeCount)
        assertEquals(1, summary.pausedCount)
        assertEquals(11.0, summary.dueNext7DaysTotal, 0.001)
        assertEquals(131.0, summary.dueNext30DaysTotal, 0.001)
    }

    @Test
    fun advancesDateCorrectlyAcrossCycles() {
        val base = LocalDate(2026, 1, 15)
        assertEquals(LocalDate(2026, 1, 22), FinanceCalculator.advanceDateByCycle(base, BillingCycle.WEEKLY))
        assertEquals(LocalDate(2026, 2, 15), FinanceCalculator.advanceDateByCycle(base, BillingCycle.MONTHLY))
        assertEquals(LocalDate(2026, 4, 15), FinanceCalculator.advanceDateByCycle(base, BillingCycle.QUARTERLY))
        assertEquals(LocalDate(2027, 1, 15), FinanceCalculator.advanceDateByCycle(base, BillingCycle.YEARLY))
        assertEquals(LocalDate(2026, 1, 25), FinanceCalculator.advanceDateByCycle(base, BillingCycle.CUSTOM_DAYS, 10))
    }

    @Test
    fun projects12MonthsCashflow() {
        val ref = LocalDate(2026, 11, 20)
        val vault = RecurraVault(
            subscriptions = listOf(
                Subscription(
                    id = "m1",
                    name = "Monthly Plan",
                    amount = 15.0,
                    currency = "USD",
                    cycle = BillingCycle.MONTHLY,
                    nextRenewalDate = "2026-11-25",
                ),
            ),
        )
        val projection = FinanceCalculator.project12Months(vault, ref)
        assertEquals(12, projection.size)
        assertTrue(projection.all { it.totalAmount >= 15.0 })
    }
}
