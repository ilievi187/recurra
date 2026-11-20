package io.github.ilievi187.recurra.domain

import kotlinx.serialization.Serializable

@Serializable
enum class BillingCycle(val displayName: String, val shortLabel: String) {
    WEEKLY("Weekly", "/wk"),
    BIWEEKLY("Bi-weekly", "/2wk"),
    MONTHLY("Monthly", "/mo"),
    QUARTERLY("Quarterly", "/qtr"),
    SEMI_ANNUAL("Semi-annual", "/6mo"),
    YEARLY("Yearly", "/yr"),
    CUSTOM_DAYS("Custom (days)", "/cust"),
}

@Serializable
enum class SubscriptionCategory(val displayName: String, val colorHex: Long) {
    STREAMING("Streaming & Media", 0xFFE11D48),
    CLOUD_DEV("Cloud & Dev", 0xFF2563EB),
    PRODUCTIVITY("Productivity", 0xFF7C3AED),
    UTILITIES("Utilities & Telecom", 0xFF0891B2),
    GAMING("Gaming", 0xFF16A34A),
    HEALTH_FITNESS("Health & Fitness", 0xFFEA580C),
    INSURANCE("Insurance & Finance", 0xFFCA8A04),
    DOMAINS_HOSTING("Domains & Hosting", 0xFF4F46E5),
    EDUCATION("Education", 0xFF0D9488),
    OTHER("Other", 0xFF64748B),
}

@Serializable
enum class SubscriptionStatus(val displayName: String) {
    ACTIVE("Active"),
    PAUSED("Paused"),
    ARCHIVED("Archived"),
}

@Serializable
enum class ThemePreference(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
}

@Serializable
data class Subscription(
    val id: String,
    val name: String,
    val amount: Double,
    val currency: String = "USD",
    val cycle: BillingCycle = BillingCycle.MONTHLY,
    val customCycleDays: Int = 30,
    val nextRenewalDate: String, // ISO-8601 yyyy-MM-dd
    val category: SubscriptionCategory = SubscriptionCategory.OTHER,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val isFreeTrial: Boolean = false,
    val trialEndDate: String? = null, // ISO-8601 yyyy-MM-dd
    val autoRenew: Boolean = true,
    val paymentMethod: String = "",
    val websiteUrl: String = "",
    val notes: String = "",
    val reminderDaysBefore: Int = 3,
    val createdAt: String = "",
)

@Serializable
data class WarrantyItem(
    val id: String,
    val productName: String,
    val brand: String = "",
    val purchaseDate: String, // ISO-8601 yyyy-MM-dd
    val expiryDate: String, // ISO-8601 yyyy-MM-dd
    val purchasePrice: Double = 0.0,
    val currency: String = "USD",
    val vendor: String = "",
    val serialNumber: String = "",
    val receiptReference: String = "",
    val notes: String = "",
)

@Serializable
data class PaymentRecord(
    val id: String,
    val subscriptionId: String,
    val subscriptionName: String,
    val amount: Double,
    val currency: String,
    val paidDate: String, // ISO-8601 yyyy-MM-dd
    val note: String = "",
)

@Serializable
data class AppSettings(
    val baseCurrency: String = "USD",
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val defaultReminderDays: Int = 3,
    /**
     * Exchange rates expressed as: 1 unit of key currency = value in USD.
     * Editable by the user completely offline in Settings.
     */
    val ratesToUsd: Map<String, Double> = DefaultCurrencies.defaultRatesToUsd,
)

@Serializable
data class RecurraVault(
    val schemaVersion: Int = 1,
    val subscriptions: List<Subscription> = emptyList(),
    val warranties: List<WarrantyItem> = emptyList(),
    val paymentHistory: List<PaymentRecord> = emptyList(),
    val settings: AppSettings = AppSettings(),
)

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String,
)

object DefaultCurrencies {
    val supported: List<CurrencyInfo> = listOf(
        CurrencyInfo("USD", "$", "US Dollar"),
        CurrencyInfo("EUR", "€", "Euro"),
        CurrencyInfo("GBP", "£", "British Pound"),
        CurrencyInfo("BGN", "лв", "Bulgarian Lev"),
        CurrencyInfo("CHF", "CHF", "Swiss Franc"),
        CurrencyInfo("CAD", "C$", "Canadian Dollar"),
        CurrencyInfo("AUD", "A$", "Australian Dollar"),
        CurrencyInfo("JPY", "¥", "Japanese Yen"),
        CurrencyInfo("PLN", "zł", "Polish Zloty"),
        CurrencyInfo("SEK", "kr", "Swedish Krona"),
        CurrencyInfo("INR", "₹", "Indian Rupee"),
        CurrencyInfo("BRL", "R$", "Brazilian Real"),
    )

    val defaultRatesToUsd: Map<String, Double> = mapOf(
        "USD" to 1.0,
        "EUR" to 1.08,
        "GBP" to 1.27,
        "BGN" to 0.55,
        "CHF" to 1.13,
        "CAD" to 0.72,
        "AUD" to 0.65,
        "JPY" to 0.0066,
        "PLN" to 0.25,
        "SEK" to 0.095,
        "INR" to 0.012,
        "BRL" to 0.18,
    )

    fun symbolFor(code: String): String =
        supported.firstOrNull { it.code.equals(code, ignoreCase = true) }?.symbol ?: "$code "
}
