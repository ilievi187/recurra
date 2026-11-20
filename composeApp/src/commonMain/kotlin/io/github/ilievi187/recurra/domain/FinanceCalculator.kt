package io.github.ilievi187.recurra.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.roundToLong

data class CategorySpend(
    val category: SubscriptionCategory,
    val monthlyAmount: Double,
    val share: Double,
    val count: Int,
)

data class MonthlyProjection(
    val year: Int,
    val monthNumber: Int,
    val label: String,
    val totalAmount: Double,
    val renewalCount: Int,
)

data class UpcomingRenewal(
    val subscription: Subscription,
    val dueDate: LocalDate,
    val daysUntilDue: Int,
    val convertedAmount: Double,
    val isTrialEnding: Boolean,
)

data class DashboardSummary(
    val monthlyTotal: Double,
    val yearlyTotal: Double,
    val activeCount: Int,
    val pausedCount: Int,
    val trialCount: Int,
    val dueNext7DaysTotal: Double,
    val dueNext30DaysTotal: Double,
    val activeWarrantiesCount: Int,
    val expiringWarrantiesSoonCount: Int,
    val categoryBreakdown: List<CategorySpend>,
    val upcomingRenewals: List<UpcomingRenewal>,
)

object FinanceCalculator {

    fun today(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    fun parseDateOrNull(iso: String?): LocalDate? {
        if (iso.isNullOrBlank()) return null
        return runCatching { LocalDate.parse(iso.trim()) }.getOrNull()
    }

    fun convertAmount(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        ratesToUsd: Map<String, Double>,
    ): Double {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) return amount
        val fromRate = ratesToUsd[fromCurrency.uppercase()] ?: DefaultCurrencies.defaultRatesToUsd[fromCurrency.uppercase()] ?: 1.0
        val toRate = ratesToUsd[toCurrency.uppercase()] ?: DefaultCurrencies.defaultRatesToUsd[toCurrency.uppercase()] ?: 1.0
        if (toRate <= 0.0) return amount
        val inUsd = amount * fromRate
        return inUsd / toRate
    }

    fun normalizedMonthlyAmount(subscription: Subscription): Double {
        if (subscription.amount <= 0.0) return 0.0
        return when (subscription.cycle) {
            BillingCycle.WEEKLY -> subscription.amount * 52.0 / 12.0
            BillingCycle.BIWEEKLY -> subscription.amount * 26.0 / 12.0
            BillingCycle.MONTHLY -> subscription.amount
            BillingCycle.QUARTERLY -> subscription.amount / 3.0
            BillingCycle.SEMI_ANNUAL -> subscription.amount / 6.0
            BillingCycle.YEARLY -> subscription.amount / 12.0
            BillingCycle.CUSTOM_DAYS -> {
                val days = subscription.customCycleDays.coerceAtLeast(1)
                subscription.amount * (30.4375 / days.toDouble())
            }
        }
    }

    fun normalizedYearlyAmount(subscription: Subscription): Double =
        normalizedMonthlyAmount(subscription) * 12.0

    fun advanceDateByCycle(
        date: LocalDate,
        cycle: BillingCycle,
        customCycleDays: Int = 30,
    ): LocalDate = when (cycle) {
        BillingCycle.WEEKLY -> date.plus(DatePeriod(days = 7))
        BillingCycle.BIWEEKLY -> date.plus(DatePeriod(days = 14))
        BillingCycle.MONTHLY -> date.plus(DatePeriod(months = 1))
        BillingCycle.QUARTERLY -> date.plus(DatePeriod(months = 3))
        BillingCycle.SEMI_ANNUAL -> date.plus(DatePeriod(months = 6))
        BillingCycle.YEARLY -> date.plus(DatePeriod(years = 1))
        BillingCycle.CUSTOM_DAYS -> date.plus(DatePeriod(days = customCycleDays.coerceAtLeast(1)))
    }

    fun effectiveDueDate(subscription: Subscription): LocalDate? {
        if (subscription.isFreeTrial && !subscription.trialEndDate.isNullOrBlank()) {
            return parseDateOrNull(subscription.trialEndDate) ?: parseDateOrNull(subscription.nextRenewalDate)
        }
        return parseDateOrNull(subscription.nextRenewalDate)
    }

    fun computeDashboardSummary(
        vault: RecurraVault,
        referenceDate: LocalDate = today(),
    ): DashboardSummary {
        val base = vault.settings.baseCurrency
        val rates = vault.settings.ratesToUsd

        val active = vault.subscriptions.filter { it.status == SubscriptionStatus.ACTIVE }
        val paused = vault.subscriptions.filter { it.status == SubscriptionStatus.PAUSED }
        val trials = active.filter { it.isFreeTrial }

        var monthlyTotal = 0.0
        val categoryTotals = mutableMapOf<SubscriptionCategory, Double>()
        val categoryCounts = mutableMapOf<SubscriptionCategory, Int>()

        for (sub in active) {
            val monthlyInBase = convertAmount(
                amount = normalizedMonthlyAmount(sub),
                fromCurrency = sub.currency,
                toCurrency = base,
                ratesToUsd = rates,
            )
            monthlyTotal += monthlyInBase
            categoryTotals[sub.category] = (categoryTotals[sub.category] ?: 0.0) + monthlyInBase
            categoryCounts[sub.category] = (categoryCounts[sub.category] ?: 0) + 1
        }

        val breakdown = categoryTotals.entries
            .sortedByDescending { it.value }
            .map { (cat, amt) ->
                CategorySpend(
                    category = cat,
                    monthlyAmount = amt,
                    share = if (monthlyTotal > 0.0) amt / monthlyTotal else 0.0,
                    count = categoryCounts[cat] ?: 0,
                )
            }

        val upcoming = active.mapNotNull { sub ->
            val due = effectiveDueDate(sub) ?: return@mapNotNull null
            val days = referenceDate.daysUntil(due)
            val converted = convertAmount(sub.amount, sub.currency, base, rates)
            UpcomingRenewal(
                subscription = sub,
                dueDate = due,
                daysUntilDue = days,
                convertedAmount = converted,
                isTrialEnding = sub.isFreeTrial,
            )
        }.sortedBy { it.daysUntilDue }

        val due7 = upcoming.filter { it.daysUntilDue in 0..7 }.sumOf { it.convertedAmount }
        val due30 = upcoming.filter { it.daysUntilDue in 0..30 }.sumOf { it.convertedAmount }

        val activeWarranties = vault.warranties.count { w ->
            val exp = parseDateOrNull(w.expiryDate)
            exp != null && referenceDate.daysUntil(exp) >= 0
        }
        val expiringSoonWarranties = vault.warranties.count { w ->
            val exp = parseDateOrNull(w.expiryDate)
            exp != null && referenceDate.daysUntil(exp) in 0..60
        }

        return DashboardSummary(
            monthlyTotal = monthlyTotal,
            yearlyTotal = monthlyTotal * 12.0,
            activeCount = active.size,
            pausedCount = paused.size,
            trialCount = trials.size,
            dueNext7DaysTotal = due7,
            dueNext30DaysTotal = due30,
            activeWarrantiesCount = activeWarranties,
            expiringWarrantiesSoonCount = expiringSoonWarranties,
            categoryBreakdown = breakdown,
            upcomingRenewals = upcoming,
        )
    }

    /**
     * Returns all renewal occurrences for active subscriptions falling in a specific year & month.
     */
    fun renewalsInMonth(
        subscriptions: List<Subscription>,
        year: Int,
        monthNumber: Int,
    ): Map<Int, List<Subscription>> {
        val monthStart = LocalDate(year, monthNumber, 1)
        val nextMonthStart = monthStart.plus(DatePeriod(months = 1))
        val result = mutableMapOf<Int, MutableList<Subscription>>()

        for (sub in subscriptions) {
            if (sub.status != SubscriptionStatus.ACTIVE) continue
            var cursor = effectiveDueDate(sub) ?: continue

            // If the renewal date is before this month, step forward until >= monthStart
            var guard = 0
            while (cursor < monthStart && guard < 200) {
                cursor = advanceDateByCycle(cursor, sub.cycle, sub.customCycleDays)
                guard++
            }

            // Collect all occurrences within [monthStart, nextMonthStart)
            var innerGuard = 0
            while (cursor >= monthStart && cursor < nextMonthStart && innerGuard < 35) {
                result.getOrPut(cursor.dayOfMonth) { mutableListOf() }.add(sub)
                cursor = advanceDateByCycle(cursor, sub.cycle, sub.customCycleDays)
                innerGuard++
            }
        }
        return result
    }

    /**
     * Computes a 12-month cashflow projection starting from referenceDate's month.
     */
    fun project12Months(
        vault: RecurraVault,
        referenceDate: LocalDate = today(),
    ): List<MonthlyProjection> {
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val base = vault.settings.baseCurrency
        val rates = vault.settings.ratesToUsd
        val startMonth = LocalDate(referenceDate.year, referenceDate.monthNumber, 1)

        return (0 until 12).map { offset ->
            val m = startMonth.plus(DatePeriod(months = offset))
            val occ = renewalsInMonth(vault.subscriptions, m.year, m.monthNumber)
            val allSubs = occ.values.flatten()
            val total = allSubs.sumOf { sub ->
                convertAmount(sub.amount, sub.currency, base, rates)
            }
            MonthlyProjection(
                year = m.year,
                monthNumber = m.monthNumber,
                label = monthNames[(m.monthNumber - 1).coerceIn(0, 11)],
                totalAmount = total,
                renewalCount = allSubs.size,
            )
        }
    }

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val symbol = DefaultCurrencies.symbolFor(currencyCode)
        val negative = amount < 0.0
        val roundedCents = (abs(amount) * 100.0).roundToLong()
        val whole = roundedCents / 100
        val cents = (roundedCents % 100).toString().padStart(2, '0')
        val wholeFormatted = whole.toString()
            .reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
        val sign = if (negative) "-" else ""
        return if (currencyCode.equals("BGN", ignoreCase = true) ||
            currencyCode.equals("PLN", ignoreCase = true) ||
            currencyCode.equals("SEK", ignoreCase = true)
        ) {
            "$sign$wholeFormatted.$cents $symbol"
        } else {
            "$sign$symbol$wholeFormatted.$cents"
        }
    }
}
