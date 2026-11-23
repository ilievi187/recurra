package io.github.ilievi187.recurra.domain

/**
 * Offline, user-maintained exchange rates.
 *
 * Settings persist rates as USD per one currency unit, while the editor displays
 * one unit of the selected currency expressed in the user's base currency.
 * There are no network requests, background updates or rate-provider dependencies.
 */
object OfflineExchangeRates {
    private val supportedCodes = DefaultCurrencies.supported.map { it.code }.toSet()

    private fun requireSupported(code: String): String {
        val normalized = code.uppercase()
        require(normalized in supportedCodes) { "Unsupported currency: $code" }
        return normalized
    }

    private fun usdRate(settings: AppSettings, currency: String): Double {
        if (currency == "USD") return 1.0
        val custom = settings.ratesToUsd[currency]
        return custom?.takeIf { it.isFinite() && it > 0.0 }
            ?: DefaultCurrencies.defaultRatesToUsd.getValue(currency)
    }

    /** Returns how many units of the base currency equal one unit of [currency]. */
    fun rateInBase(settings: AppSettings, currency: String): Double {
        val base = requireSupported(settings.baseCurrency)
        val target = requireSupported(currency)
        if (target == base) return 1.0
        return usdRate(settings, target) / usdRate(settings, base)
    }

    /**
     * Saves a base-currency quote into the existing USD-anchored vault format.
     *
     * When the USD quote itself changes (with a non-USD base), all non-USD
     * stored values are rescaled so their quotes relative to the base remain
     * unchanged. The USD anchor always stays exactly 1.0.
     */
    fun withRate(
        settings: AppSettings,
        currency: String,
        unitsOfBasePerUnit: Double,
    ): AppSettings {
        val base = requireSupported(settings.baseCurrency)
        val target = requireSupported(currency)
        require(target != base) { "The base currency always has a rate of 1." }
        require(unitsOfBasePerUnit.isFinite() && unitsOfBasePerUnit > 0.0) {
            "Enter a positive, finite exchange rate."
        }

        val rates = DefaultCurrencies.defaultRatesToUsd.keys.associateWith {
            usdRate(settings, it)
        }
        val baseInUsd = usdRate(settings, base)
        val updated = if (target == "USD") {
            val newBaseInUsd = 1.0 / unitsOfBasePerUnit
            val multiplier = newBaseInUsd / baseInUsd
            require(multiplier.isFinite() && multiplier > 0.0) { "Exchange rate is out of range." }
            rates.mapValues { (code, rate) ->
                if (code == "USD") 1.0 else rate * multiplier
            }
        } else {
            rates + (target to (unitsOfBasePerUnit * baseInUsd))
        }
        require(updated.values.all { it.isFinite() && it > 0.0 }) { "Exchange rate is out of range." }
        return settings.copy(ratesToUsd = updated + ("USD" to 1.0))
    }
}
