package io.github.ilievi187.recurra.domain

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

enum class DetectedDocumentType {
    WARRANTY,
    SUBSCRIPTION,
}

data class ParsedDocument(
    val detectedType: DetectedDocumentType,
    val title: String,
    val vendor: String,
    val amount: Double?,
    val currency: String,
    val purchaseDate: String,
    val warrantyMonths: Int,
    val expiryDate: String,
    val invoiceNumber: String,
    val serialNumber: String,
    val billingCycle: BillingCycle,
    val rawText: String,
    val summary: String,
)

/**
 * Offline, privacy-preserving parser for invoices, receipts, and warranty cards
 * supporting both English and Bulgarian (Cyrillic) documents.
 */
object DocumentParser {

    fun parse(
        rawText: String,
        defaultCurrency: String = "USD",
        referenceDate: LocalDate = FinanceCalculator.today(),
    ): ParsedDocument {
        val cleaned = rawText.replace("\r\n", "\n").replace("\r", "\n")
        val lines = cleaned.lines().map { it.trim() }.filter { it.isNotBlank() }
        val lower = cleaned.lowercase()

        val isSubscription = detectSubscription(lower)
        val billingCycle = detectCycle(lower)
        val currency = detectCurrency(cleaned, defaultCurrency)
        val amount = detectTotalAmount(lines, cleaned)
        val purchaseDate = detectDate(cleaned) ?: referenceDate
        val warrantyMonths = detectWarrantyMonths(lower) ?: 24
        val expiryDate = purchaseDate.plus(DatePeriod(months = warrantyMonths))
        val invoiceNumber = detectInvoiceNumber(cleaned)
        val serialNumber = detectSerialNumber(cleaned)
        val vendor = detectVendor(lines)
        val title = detectProductOrServiceTitle(lines, vendor)

        val parts = mutableListOf<String>()
        if (title.isNotBlank()) parts.add("Item: $title")
        if (amount != null) parts.add("Amount: ${FinanceCalculator.formatCurrency(amount, currency)}")
        parts.add("Date: $purchaseDate")
        if (!isSubscription) parts.add("Warranty: ${warrantyMonths}m")
        if (invoiceNumber.isNotBlank()) parts.add("Ref: $invoiceNumber")

        return ParsedDocument(
            detectedType = if (isSubscription) DetectedDocumentType.SUBSCRIPTION else DetectedDocumentType.WARRANTY,
            title = title,
            vendor = vendor,
            amount = amount,
            currency = currency,
            purchaseDate = purchaseDate.toString(),
            warrantyMonths = warrantyMonths,
            expiryDate = expiryDate.toString(),
            invoiceNumber = invoiceNumber,
            serialNumber = serialNumber,
            billingCycle = billingCycle,
            rawText = cleaned,
            summary = parts.joinToString(" · "),
        )
    }

    private fun detectSubscription(lower: String): Boolean {
        val subKeywords = listOf(
            "subscription", "monthly plan", "annual plan", "auto-renew", "recurring",
            "billing period", "next billing", "абонамент", "месечна такса", "месечен план",
        )
        val warrantyKeywords = listOf(
            "warranty", "гаранция", "гаранционна карта", "serial", "s/n", "сериен", "imei",
        )
        val subHits = subKeywords.count { lower.contains(it) }
        val warHits = warrantyKeywords.count { lower.contains(it) }
        return subHits > warHits && subHits > 0
    }

    private fun detectCycle(lower: String): BillingCycle = when {
        lower.contains("yearly") || lower.contains("annual") || lower.contains("годиш") -> BillingCycle.YEARLY
        lower.contains("quarterly") || lower.contains("тримесеч") -> BillingCycle.QUARTERLY
        lower.contains("weekly") || lower.contains("седмич") -> BillingCycle.WEEKLY
        else -> BillingCycle.MONTHLY
    }

    private fun detectCurrency(text: String, defaultCurrency: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("bgn") || lower.contains("лв") || lower.contains("лева") -> "BGN"
            text.contains("€") || lower.contains("eur") || lower.contains("евро") -> "EUR"
            text.contains("£") || lower.contains("gbp") -> "GBP"
            text.contains("$") || lower.contains("usd") -> "USD"
            lower.contains("chf") -> "CHF"
            else -> defaultCurrency.uppercase()
        }
    }

    private fun detectTotalAmount(lines: List<String>, fullText: String): Double? {
        val totalKeywords = listOf(
            "обща сума", "сума за плащане", "всичко", "общо", "тотал",
            "grand total", "total due", "amount due", "balance due", "total", "amount paid",
        )
        val amountRegex = Regex("""(\d{1,3}(?:[ ,\u00A0]\d{3})*(?:[.,]\d{2})|\d+[.,]\d{2})""")

        // Pass 1: Look for lines containing a total keyword
        for (line in lines.reversed()) {
            val l = line.lowercase()
            if (totalKeywords.any { l.contains(it) }) {
                val matches = amountRegex.findAll(line).mapNotNull { parseMoney(it.value) }.toList()
                if (matches.isNotEmpty()) return matches.last()
            }
        }

        // Pass 2: Fallback to the largest realistic currency-formatted number
        val allNumbers = amountRegex.findAll(fullText)
            .mapNotNull { parseMoney(it.value) }
            .filter { it in 0.50..100_000.0 }
            .toList()
        return allNumbers.maxOrNull()
    }

    private fun parseMoney(raw: String): Double? {
        val compact = raw.replace("\u00A0", "").replace(" ", "")
        val normalized = if (compact.contains(',') && compact.contains('.')) {
            if (compact.lastIndexOf(',') > compact.lastIndexOf('.')) {
                compact.replace(".", "").replace(",", ".")
            } else {
                compact.replace(",", "")
            }
        } else if (compact.contains(',')) {
            compact.replace(",", ".")
        } else {
            compact
        }
        return normalized.toDoubleOrNull()
    }

    private fun detectDate(text: String): LocalDate? {
        // ISO: 2026-11-15
        val isoRegex = Regex("""\b(20\d{2})[-./](0?[1-9]|1[0-2])[-./](0?[1-9]|[12]\d|3[01])\b""")
        isoRegex.find(text)?.let { m ->
            val y = m.groupValues[1].toInt()
            val mo = m.groupValues[2].toInt()
            val d = m.groupValues[3].toInt()
            runCatching { LocalDate(y, mo, d) }.getOrNull()?.let { return it }
        }

        // European / Bulgarian: 15.11.2026 or 15/11/2026 or 15-11-2026
        val euRegex = Regex("""\b(0?[1-9]|[12]\d|3[01])[./-](0?[1-9]|1[0-2])[./-](20\d{2})\b""")
        euRegex.find(text)?.let { m ->
            val d = m.groupValues[1].toInt()
            val mo = m.groupValues[2].toInt()
            val y = m.groupValues[3].toInt()
            runCatching { LocalDate(y, mo, d) }.getOrNull()?.let { return it }
        }

        return null
    }

    private fun detectWarrantyMonths(lower: String): Int? {
        val monthsRegex = Regex("""(\d{1,2})\s*(?:months?|mos?\.?|месец(?:а|и)?|мес\.?)""")
        monthsRegex.find(lower)?.groupValues?.get(1)?.toIntOrNull()?.let { m ->
            if (m in 1..120) return m
        }
        val yearsRegex = Regex("""(\d{1,2})\s*(?:years?|yrs?\.?|годин(?:а|и)?|год\.?)""")
        yearsRegex.find(lower)?.groupValues?.get(1)?.toIntOrNull()?.let { y ->
            if (y in 1..15) return y * 12
        }
        return null
    }

    private fun detectInvoiceNumber(text: String): String {
        val regex = Regex(
            """(?i)(?:фактура\s*(?:№|no\.?|n|#)?|invoice\s*(?:no\.?|#|:)?|order\s*(?:no\.?|#|:)?|поръчка\s*(?:№|#)?|receipt\s*(?:no\.?|#|:)?)\s*[:#№]?\s*([A-Z0-9][A-Z0-9\-/]{3,24})""",
        )
        return regex.find(text)?.groupValues?.get(1)?.trim().orEmpty()
    }

    private fun detectSerialNumber(text: String): String {
        val regex = Regex(
            """(?i)(?:s/n|serial(?:\s*(?:no\.?|number|#))?|сериен\s*(?:№|номер)?|imei)\s*[:#№]?\s*([A-Z0-9][A-Z0-9\-]{4,28})""",
        )
        return regex.find(text)?.groupValues?.get(1)?.trim().orEmpty()
    }

    private fun detectVendor(lines: List<String>): String {
        val companyHint = Regex("""(?i)\b(eood|ood|ad|ltd|llc|inc|gmbh|еоод|оод|ад|еад)\b""")
        lines.take(8).firstOrNull { companyHint.containsMatchIn(it) }?.let { return it.take(60) }

        val skipHeader = listOf("invoice", "фактура", "receipt", "касова", "гаранция", "warranty", "оригинал", "original")
        return lines.take(5).firstOrNull { line ->
            val l = line.lowercase()
            line.length in 3..55 && skipHeader.none { l.startsWith(it) }
        }.orEmpty()
    }

    private fun detectProductOrServiceTitle(lines: List<String>, vendor: String): String {
        val itemLabelRegex = Regex("""(?i)(?:продукт|артикул|изделие|модел|услуга|product|item|model|description|plan)\s*[:\-]\s*(.+)""")
        for (line in lines) {
            itemLabelRegex.find(line)?.groupValues?.get(1)?.trim()?.takeIf { it.length >= 3 }?.let {
                return it.take(80)
            }
        }
        // Fallback: pick first descriptive line that is not vendor, date, or metadata
        val noise = listOf("invoice", "фактура", "date", "дата", "total", "общо", "eik", "еик", "vat", "ддс", "s/n", "serial", "сериен")
        return lines.firstOrNull { line ->
            val l = line.lowercase()
            line != vendor && line.length in 5..70 && noise.none { l.contains(it) }
        } ?: vendor.ifBlank { "Scanned Item" }
    }
}
