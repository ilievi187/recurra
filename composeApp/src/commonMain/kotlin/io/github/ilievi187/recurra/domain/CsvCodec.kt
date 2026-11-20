package io.github.ilievi187.recurra.domain

object CsvCodec {

    private val header = listOf(
        "id",
        "name",
        "amount",
        "currency",
        "cycle",
        "customCycleDays",
        "nextRenewalDate",
        "category",
        "status",
        "isFreeTrial",
        "trialEndDate",
        "autoRenew",
        "paymentMethod",
        "websiteUrl",
        "notes",
    )

    fun exportSubscriptions(subscriptions: List<Subscription>): String {
        val sb = StringBuilder()
        sb.appendLine(header.joinToString(","))
        for (s in subscriptions) {
            val row = listOf(
                s.id,
                s.name,
                s.amount.toString(),
                s.currency,
                s.cycle.name,
                s.customCycleDays.toString(),
                s.nextRenewalDate,
                s.category.name,
                s.status.name,
                s.isFreeTrial.toString(),
                s.trialEndDate.orEmpty(),
                s.autoRenew.toString(),
                s.paymentMethod,
                s.websiteUrl,
                s.notes,
            ).joinToString(",") { escapeField(it) }
            sb.appendLine(row)
        }
        return sb.toString()
    }

    fun importSubscriptions(csvText: String): List<Subscription> {
        val rows = parseCsvRows(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows.first().map { it.trim().lowercase() }
        val colIndex = headerRow.withIndex().associate { it.value to it.index }

        fun List<String>.col(name: String): String {
            val idx = colIndex[name.lowercase()] ?: return ""
            return getOrNull(idx)?.trim().orEmpty()
        }

        val imported = mutableListOf<Subscription>()
        for ((rowIdx, row) in rows.drop(1).withIndex()) {
            if (row.all { it.isBlank() }) continue
            val name = row.col("name")
            val amount = row.col("amount").toDoubleOrNull() ?: continue
            if (name.isBlank()) continue

            val id = row.col("id").ifBlank { "csv-import-${rowIdx + 1}-${name.hashCode()}" }
            val currency = row.col("currency").ifBlank { "USD" }.uppercase()
            val cycle = BillingCycle.entries.firstOrNull {
                it.name.equals(row.col("cycle"), ignoreCase = true)
            } ?: BillingCycle.MONTHLY
            val customDays = row.col("customcycledays").toIntOrNull()?.coerceAtLeast(1) ?: 30
            val nextDate = row.col("nextrenewaldate").ifBlank { FinanceCalculator.today().toString() }
            val category = SubscriptionCategory.entries.firstOrNull {
                it.name.equals(row.col("category"), ignoreCase = true)
            } ?: SubscriptionCategory.OTHER
            val status = SubscriptionStatus.entries.firstOrNull {
                it.name.equals(row.col("status"), ignoreCase = true)
            } ?: SubscriptionStatus.ACTIVE
            val isTrial = row.col("isfreetrial").equals("true", ignoreCase = true)
            val trialEnd = row.col("trialenddate").ifBlank { null }
            val autoRenew = !row.col("autorenew").equals("false", ignoreCase = true)

            imported.add(
                Subscription(
                    id = id,
                    name = name,
                    amount = amount,
                    currency = currency,
                    cycle = cycle,
                    customCycleDays = customDays,
                    nextRenewalDate = nextDate,
                    category = category,
                    status = status,
                    isFreeTrial = isTrial,
                    trialEndDate = trialEnd,
                    autoRenew = autoRenew,
                    paymentMethod = row.col("paymentmethod"),
                    websiteUrl = row.col("websiteurl"),
                    notes = row.col("notes"),
                ),
            )
        }
        return imported
    }

    private fun escapeField(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        if (!needsQuotes) return value
        return "\"${value.replace("\"", "\"\"")}\""
    }

    private fun parseCsvRows(input: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < input.length) {
            val c = input[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < input.length && input[i + 1] == '"') {
                        field.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    field.append(c)
                }
            } else {
                when (c) {
                    '"' -> inQuotes = true
                    ',' -> {
                        currentRow.add(field.toString())
                        field.clear()
                    }
                    '\r' -> {
                        // ignore CR in CRLF
                    }
                    '\n' -> {
                        currentRow.add(field.toString())
                        field.clear()
                        rows.add(currentRow.toList())
                        currentRow.clear()
                    }
                    else -> field.append(c)
                }
            }
            i++
        }
        if (field.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(field.toString())
            rows.add(currentRow.toList())
        }
        return rows
    }
}
